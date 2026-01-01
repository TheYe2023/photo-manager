package com.leafi.backend.controller;

import java.util.List;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.time.Duration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.BeanUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.leafi.backend.annotation.AuthCheck;
import com.leafi.backend.common.BaseResponse;
import com.leafi.backend.common.DeleteRequest;
import com.leafi.backend.common.ResultUtils;
import com.leafi.backend.constant.UserConstant;
import com.leafi.backend.exception.BusinessException;
import com.leafi.backend.exception.ThrowUtils;
import com.leafi.backend.model.dto.picture.CreatePictureAnalysisTaskRequest;
import com.leafi.backend.model.dto.picture.PictureEditRequest;
import com.leafi.backend.model.dto.picture.PictureQueryRequest;
import com.leafi.backend.model.dto.picture.PictureReviewRequest;
import com.leafi.backend.model.dto.picture.PictureUpdateRequest;
import com.leafi.backend.model.dto.picture.PictureUploadRequest;
import com.leafi.backend.model.vo.PictureVO;
import com.leafi.backend.model.vo.PictureTagCategory;
import com.leafi.backend.model.entity.Picture;
import com.leafi.backend.model.entity.Space;
import com.leafi.backend.model.entity.User;
import com.leafi.backend.model.enums.PictureReviewStatusEnum;
import com.leafi.backend.service.PictureService;
import com.leafi.backend.service.UserService;
import com.leafi.backend.service.SpaceService;
import com.leafi.backend.exception.ErrorCode;
import com.leafi.backend.model.dto.picture.ChatRequest;


import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private ChatClient chatClient;
    /**  
     * 上传图片（可重新上传）  
     */  
    @PostMapping("/upload")  
    // @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  
    public BaseResponse<PictureVO> uploadPicture(  
            @RequestPart("file") MultipartFile multipartFile,  
            PictureUploadRequest pictureUploadRequest,  
            HttpServletRequest request) {  
        User loginUser = userService.getLoginUser(request);  
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);  
        return ResultUtils.success(pictureVO);  
    }

    /**  
     * 通过 URL 上传图片（可重新上传）  
     */  
    @PostMapping("/upload/url")  
    public BaseResponse<PictureVO> uploadPictureByUrl(  
            @RequestBody PictureUploadRequest pictureUploadRequest,  
            HttpServletRequest request) {  
        User loginUser = userService.getLoginUser(request);  
        String fileUrl = pictureUploadRequest.getFileUrl();  
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);  
        return ResultUtils.success(pictureVO);  
    }

    @PostMapping("/delete")
    // @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest
            , HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.deletePicture(deleteRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }
    
    /**  
     * 更新图片（仅管理员可用）  
     */  
    @PostMapping("/update")  
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {  
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {  
            throw new BusinessException(ErrorCode.PARAMS_ERROR);  
        }  
        // 将实体类和 DTO 进行转换  
        Picture picture = new Picture();  
        BeanUtils.copyProperties(pictureUpdateRequest, picture);  
        // 注意将 list 转为 string  
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));  
        // 数据校验  
        pictureService.validPicture(picture);  
        // 判断是否存在  
        long id = pictureUpdateRequest.getId();  
        Picture oldPicture = pictureService.getById(id);  
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);  
        // 补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);
        // 操作数据库  
        boolean result = pictureService.updateById(picture);  
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);  
        return ResultUtils.success(true);  
    }  
    
    /**  
     * 
     * 根据 id 获取图片（仅管理员可用）  
     */  
    @GetMapping("/get")  
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {  
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);  
        // 查询数据库  
        Picture picture = pictureService.getById(id);  
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);  
        // 获取封装类  
        return ResultUtils.success(picture);  
    }  
    
    /**  
     * 根据 id 获取图片（封装类）  
     */  
    @GetMapping("/get/vo")  
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {  
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);  
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 空间权限校验
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
        }
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);  
        // 获取封装类  
        return ResultUtils.success(pictureService.getPictureVO(picture, request));  
    }  
    
    /**  
     * 分页获取图片列表（仅管理员可用）  
     */  
    @PostMapping("/list/page")  
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {  
        long current = pictureQueryRequest.getCurrent();  
        long size = pictureQueryRequest.getPageSize();  
        // 查询数据库  
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),  
                pictureService.getQueryWrapper(pictureQueryRequest));  
        return ResultUtils.success(picturePage);  
    }  
    
    /**  
     * 分页获取图片列表（封装类）  
     */  
    @PostMapping("/list/page/vo")  
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,  
                                                            HttpServletRequest request) {  
        long current = pictureQueryRequest.getCurrent();  
        long size = pictureQueryRequest.getPageSize();  
        // 限制爬虫  
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);  
        // 空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        // 公开图库
        if (spaceId == null) {
            // 普通用户默认只能查看已过审的公开数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            // 私有空间
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }
        // 设置只能查看审核通过的图片
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 查询数据库  
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),  
                pictureService.getQueryWrapper(pictureQueryRequest));  
        // 获取封装类  
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));  
    }  
    
    /**  
     * 编辑图片（给用户使用）  
     */   
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**  
     * 获取图片标签和分类列表  
     */
    @GetMapping("/tag_category")  
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {  
        PictureTagCategory pictureTagCategory = new PictureTagCategory();  
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "风景", "艺术", "校园", "动漫", "背景", "高清");  
        List<String> categoryList = Arrays.asList("图片", "表情包", "海报", "素材", "模板");  
        pictureTagCategory.setTagList(tagList);  
        pictureTagCategory.setCategoryList(categoryList);  
        return ResultUtils.success(pictureTagCategory);  
    }

    /**  
     * 审核图片（仅管理员可用）  
     */
    @PostMapping("/review")  
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,  
                                                HttpServletRequest request) {  
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);  
        User loginUser = userService.getLoginUser(request);  
        pictureService.doPictureReview(pictureReviewRequest, loginUser);  
        return ResultUtils.success(true);  
    }

    /**
     * 创建AI图片分析任务
     */
    @PostMapping("/tag_category/create_task")
    public BaseResponse<List<String>> createAIPictureAnalyzeTask(
            @RequestBody CreatePictureAnalysisTaskRequest createPictureAnalysisTaskRequest,
            HttpServletRequest request) {
        if (createPictureAnalysisTaskRequest == null || createPictureAnalysisTaskRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<String> tags = pictureService.createPictureAnalysisTask(createPictureAnalysisTaskRequest, loginUser);
        return ResultUtils.success(tags);
    }

    @PostMapping("/chat")
    public BaseResponse<String> doChat(@RequestBody ChatRequest request) {
        String response = chatClient.prompt()
                .system("你是一个专业的智能照片管家。你可以通过调用搜索工具帮助用户查找照片。")
                .user(request.getMessage())
                .functions("callPictureSearch") // 开启工具权限
                .call()
                .content();
                
        return ResultUtils.success(response);
    }
}
