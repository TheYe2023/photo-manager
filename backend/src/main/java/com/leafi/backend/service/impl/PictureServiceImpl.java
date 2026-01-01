package com.leafi.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leafi.backend.api.aliyunai.model.CreateAiAnalysisRequest;
import com.leafi.backend.api.aliyunai.model.CreateAiAnalysisResponse;
import com.leafi.backend.api.aliyunai.AliYunAiApi;
import com.leafi.backend.common.PageRequest;
import com.leafi.backend.exception.BusinessException;
import com.leafi.backend.exception.ErrorCode;
import com.leafi.backend.exception.ThrowUtils;
import com.leafi.backend.manager.CosManager;
import com.leafi.backend.manager.FileManager;
import com.leafi.backend.manager.upload.FilePictureUpload;
import com.leafi.backend.manager.upload.PictureUploadTemplate;
import com.leafi.backend.manager.upload.UrlPictureUpload;
import com.leafi.backend.mapper.PictureMapper;
import com.leafi.backend.model.dto.file.UploadPictureResult;
import com.leafi.backend.model.dto.picture.*;
import com.leafi.backend.model.entity.Picture;
import com.leafi.backend.model.entity.Space;
import com.leafi.backend.model.entity.User;
import com.leafi.backend.model.enums.PictureReviewStatusEnum;
import com.leafi.backend.model.vo.PictureVO;
import com.leafi.backend.model.vo.UserVO;
import com.leafi.backend.service.PictureService;
import com.leafi.backend.service.UserService;
import com.leafi.backend.service.SpaceService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Autowired
    private CosManager cosManager;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private AliYunAiApi aliYunAiApi;
    
    /**
     * 校验图片参数
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        // 如果传递了 url，才校验
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    /**
     * 上传图片
     */
    @Override  
    public PictureVO uploadPicture(Object multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {  
    ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
    // 空间权限校验
    Long spaceId = pictureUploadRequest.getSpaceId();
    if (spaceId != null) {
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        // 必须空间创建人（管理员）才能上传
        if (!loginUser.getId().equals(space.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
        }
        // 校验额度
        if (space.getTotalCount() >= space.getMaxCount()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
        }
        if (space.getTotalSize() >= space.getMaxSize()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
        }
    }
        if (multipartFile == null) {  
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片不能为空");  
        }
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);  
        // 用于判断是新增还是更新图片  
        Long pictureId = null;  
        if (pictureUploadRequest != null) {  
            pictureId = pictureUploadRequest.getId();  
        }  
        // 如果是更新图片，需要校验图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人或管理员可编辑
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 校验空间是否一致
            // 没传 spaceId，则复用原有图片的 spaceId
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                // 传了 spaceId，必须和原有图片一致
                if (ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间 id 不一致");
                }
            }
        }
        // 按照用户 id 划分目录 => 按照空间划分目录
        String uploadPathPrefix;
        if (spaceId == null) {
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            uploadPathPrefix = String.format("space/%s", spaceId);
        }
        // 根据上传类型选择不同的上传模版 
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (multipartFile instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(multipartFile, uploadPathPrefix);  
        // 构造要入库的图片信息  
        Picture picture = new Picture();  
        picture.setUrl(uploadPictureResult.getUrl());  
        picture.setName(uploadPictureResult.getPicName());  
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        picture.setPicSize(uploadPictureResult.getPicSize());  
        picture.setPicWidth(uploadPictureResult.getPicWidth());  
        picture.setPicHeight(uploadPictureResult.getPicHeight());  
        picture.setPicScale(uploadPictureResult.getPicScale());  
        picture.setPicFormat(uploadPictureResult.getPicFormat());  
        picture.setUserId(loginUser.getId());  
        picture.setSpaceId(spaceId);
        // 补充审核参数
        this.fillReviewParamsPlus(picture, loginUser);
        // 如果 pictureId 不为空，表示更新，否则是新增  
        if (pictureId != null) {  
            // 如果是更新，需要补充 id 和编辑时间  
            picture.setId(pictureId);  
            picture.setEditTime(new Date());  
        }  
        // 开启事务
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            // 插入数据
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败，数据库操作失败");
            if (finalSpaceId != null) {
                // 更新空间的使用额度
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize + " + picture.getPicSize())
                        .setSql("totalCount = totalCount + 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return picture;
        });
        return PictureVO.objToVo(picture);  
    }

    @Override  
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {  
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();  
        if (pictureQueryRequest == null) {  
            return queryWrapper;  
        }  
        // 从对象中取值  
        Long id = pictureQueryRequest.getId();  
        String name = pictureQueryRequest.getName();  
        String introduction = pictureQueryRequest.getIntroduction();  
        String category = pictureQueryRequest.getCategory();  
        List<String> tags = pictureQueryRequest.getTags();  
        Long picSize = pictureQueryRequest.getPicSize();  
        Integer picWidth = pictureQueryRequest.getPicWidth();  
        Integer picHeight = pictureQueryRequest.getPicHeight();  
        Double picScale = pictureQueryRequest.getPicScale();  
        String picFormat = pictureQueryRequest.getPicFormat();  
        String searchText = pictureQueryRequest.getSearchText();  
        Long userId = pictureQueryRequest.getUserId();  
        String sortField = pictureQueryRequest.getSortField();  
        String sortOrder = pictureQueryRequest.getSortOrder();  
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();  
        String reviewMessage = pictureQueryRequest.getReviewMessage();  
        Long reviewerId = pictureQueryRequest.getReviewerId();  
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();


        // 从多字段中搜索  
        if (StrUtil.isNotBlank(searchText)) {  
            // 需要拼接查询条件  
            queryWrapper.and(qw -> qw.like("name", searchText)  
                    .or()  
                    .like("introduction", searchText)  
            );  
        }  
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);  
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);  
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);  
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);  
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);  
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);  
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);  
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);  
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);  
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);  
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);  
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);  
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);
        // JSON 数组查询  
        if (CollUtil.isNotEmpty(tags)) {  
            for (String tag : tags) {  
                queryWrapper.like("tags", "\"" + tag + "\"");  
            }  
        }  
        // 排序  
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);  
        return queryWrapper;  
    }

    @Override  
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {  
        // 对象转封装类  
        PictureVO pictureVO = PictureVO.objToVo(picture);  
        // 关联查询用户信息  
        Long userId = picture.getUserId();  
        if (userId != null && userId > 0) {  
            User user = userService.getById(userId);  
            UserVO userVO = userService.getUserVO(user);  
            pictureVO.setUser(userVO);  
        }  
        return pictureVO;  
    }

    /**  
     * 分页获取图片封装  
     */  
    @Override  
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {  
        List<Picture> pictureList = picturePage.getRecords();  
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());  
        if (CollUtil.isEmpty(pictureList)) {  
            return pictureVOPage;  
        }  
        // 对象列表 => 封装对象列表  
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());  
        // 1. 关联查询用户信息  
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());  
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()  
                .collect(Collectors.groupingBy(User::getId));  
        // 2. 填充信息  
        pictureVOList.forEach(pictureVO -> {  
            Long userId = pictureVO.getUserId();  
            User user = null;  
            if (userIdUserListMap.containsKey(userId)) {  
                user = userIdUserListMap.get(userId).get(0);  
            }  
            pictureVO.setUser(userService.getUserVO(user));  
        });  
        pictureVOPage.setRecords(pictureVOList);  
        return pictureVOPage;  
    }

    /**
     * 图片审核  
     */
    @Override  
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {  
        Long id = pictureReviewRequest.getId();  
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();  
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);  
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {  
            throw new BusinessException(ErrorCode.PARAMS_ERROR);  
        }  
        // 判断是否存在  
        Picture oldPicture = this.getById(id);  
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);  
        // 已是该状态  
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {  
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");  
        }  
        // 更新审核状态  
        Picture updatePicture = new Picture();  
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);  
        updatePicture.setReviewerId(loginUser.getId());  
        updatePicture.setReviewTime(new Date());  
        boolean result = this.updateById(updatePicture);  
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);  
    }

    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(new Date());
        } else {
            // 非管理员，无论是编辑还是创建默认都是待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    @Override
    public void fillReviewParamsPlus(Picture picture, User loginUser) {
        boolean isPrivateAndOwned = picture.getSpaceId() != null &&
            picture.getUserId().equals(loginUser.getId());
        // 检查是否为私有空间且属于当前用户，或者用户是管理员
        if (userService.isAdmin(loginUser) || isPrivateAndOwned) {
            // 管理员或私有空间所有者自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage(isPrivateAndOwned ? "私有空间所有者自动过审" : "管理员自动过审");
            picture.setReviewTime(new Date());
        } else {
            // 非管理员,或非私有空间所有者的图片需要审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    /**
     * 校验图片操作权限
     *
     * @param loginUser
     * @param picture
     */
    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        Long loginUserId = loginUser.getId();
        if (spaceId == null) {
            // 公共图库，仅本人或管理员可操作
            if (!picture.getUserId().equals(loginUserId) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            // 私有空间，仅空间管理员可操作
            if (!picture.getUserId().equals(loginUserId)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        // 判断改图片是否被多条记录使用
        String pictureUrl = oldPicture.getUrl();
        long count = this.lambdaQuery()
                .eq(Picture::getUrl, pictureUrl)
                .count();
        // 有不止一条记录用到了该图片，不清理
        if (count > 1) {
            return;
        }
        // 删除图片
        cosManager.deleteObject(pictureUrl);
    }


    @Override
    public void deletePicture(long pictureId, User loginUser) {
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 判断是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验权限
        checkPictureAuth(loginUser, oldPicture);
        // 开启事务
        transactionTemplate.execute(status -> {
            // 操作数据库
            boolean result = this.removeById(pictureId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            // 释放额度
            Long spaceId = oldPicture.getSpaceId();
            if (spaceId != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, spaceId)
                        .setSql("totalSize = totalSize - " + oldPicture.getPicSize())
                        .setSql("totalCount = totalCount - 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return true;
        });
        // 异步清理文件
        this.clearPictureFile(oldPicture);
    }

    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        this.validPicture(picture);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 将老数据的关键字段补偿给新对象，用于权限和审核逻辑判断
        picture.setUserId(oldPicture.getUserId());
        picture.setSpaceId(oldPicture.getSpaceId());
        // 校验权限
        checkPictureAuth(loginUser, oldPicture);
        // 补充审核参数
        this.fillReviewParamsPlus(picture, loginUser);
        // 操作数据库
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public List<String> createPictureAnalysisTask(CreatePictureAnalysisTaskRequest createPictrueAnalysisRequest, User loginUser) {
        // 获取图片信息
        Long pictureId = createPictrueAnalysisRequest.getPictureId();
        Picture picture = Optional.ofNullable(this.getById(pictureId))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在"));
        // 权限校验
        checkPictureAuth(loginUser, picture);
        // 构造请求参数
        CreateAiAnalysisRequest taskRequest = new CreateAiAnalysisRequest();
        CreateAiAnalysisRequest.Input input = new CreateAiAnalysisRequest.Input();
        input.setImageUrl(picture.getUrl());
        taskRequest.setInput(input);
        return aliYunAiApi.getPictureTags(taskRequest);
    }

    /**
     * 根据关键词搜索图片（供 AI 调用）
     */
    @Tool(description = "根据关键词搜索图库中的图片") // 确保 AI 识别
    public String callPictureSearch(String searchText) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "系统异常：无法获取当前登录信息";
        }
        HttpServletRequest request = attributes.getRequest();
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            return "通知用户：请先登录后再搜索图片。";
        }
        Long currentUserId = loginUser.getId();

        log.info("AI 正在触发图片搜索，用户ID：{}，关键词：{}", currentUserId, searchText);
        try {
            // 构造查询请求
            PictureQueryRequest queryRequest = new PictureQueryRequest();
            // 基础模糊搜索：匹配名称和简介
            queryRequest.setSearchText(searchText);
            // 扩展匹配：尝试将关键词直接作为分类进行匹配
            queryRequest.setCategory(searchText);
            // 扩展匹配：尝试将关键词作为标签列表中的一项进行匹配（支持 JSON 数组查询）
            queryRequest.setTags(Collections.singletonList(searchText));
            
            queryRequest.setPageSize(5); 

            // 获取初始 QueryWrapper
            QueryWrapper<Picture> queryWrapper = this.getQueryWrapper(queryRequest);

            // 因为 getQueryWrapper 默认是将所有条件用 AND 连接，我们需要手动构造 OR 逻辑
            QueryWrapper<Picture> orWrapper = new QueryWrapper<>();
            // ( (空间内照片) OR (公共照片) ) AND (审核通过)
            orWrapper.and(allQw -> allQw.and(qw -> qw
                    .like("name", searchText)
                    .or().like("introduction", searchText)
                    .or().eq("category", searchText)
                    .or().like("tags", "\"" + searchText + "\"")
            ));

            Space mySpace = spaceService.lambdaQuery()
                    .eq(Space::getUserId, currentUserId)
                    .one();
            // 构造权限 Wrapper
            orWrapper.and(permQw -> {
                permQw.eq("spaceId", 0); // 公共空间
                if (mySpace != null) {
                    permQw.or().eq("spaceId", mySpace.getId()); // 用户的私有空间
                } else {
                    // 如果用户还没创建空间，退而求其次找 userId 匹配且无空间的照片
                    permQw.or().eq("userId", currentUserId); 
                }
            });
                
            // 仅搜索审核通过的图片
            orWrapper.eq("reviewStatus", PictureReviewStatusEnum.PASS.getValue());
            // 限制返回数量
            orWrapper.last("LIMIT 5");

            List<Picture> pictureList = this.list(orWrapper);
            
            if (CollUtil.isEmpty(pictureList)) {
                return "通知用户：库中目前没有找到与 '" + searchText + "' 相关的图片（已检索名称、简介、分类和标签）。";
            }

            // 4. 转换为精简结果
            List<Map<String, Object>> resultList = pictureList.stream().map(pic -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", pic.getId());
                map.put("title", pic.getName());
                map.put("category", pic.getCategory());
                map.put("tags", pic.getTags());
                map.put("description", pic.getIntroduction());
                map.put("detail_link_tag", String.format("<img src=\"%s\" data-id=\"%s\" title=\"点击查看详情\" />", pic.getUrl(), pic.getId()));
                return map;
            }).collect(Collectors.toList());

            return "请直接使用数据中的 detail_link_tag 来展示图片，以便用户点击。数据如下：" + JSONUtil.toJsonStr(resultList);
            
        } catch (Exception e) {
            log.error("MCP 图片搜索工具执行异常", e);
            return "系统错误：暂时无法搜索图片。";
        }
    }
}
