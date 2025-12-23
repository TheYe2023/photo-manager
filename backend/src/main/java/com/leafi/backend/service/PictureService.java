package com.leafi.backend.service;

import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leafi.backend.model.entity.Picture;
import com.leafi.backend.model.entity.User;
import com.leafi.backend.model.dto.picture.*;
import com.leafi.backend.model.vo.PictureVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface PictureService extends IService<Picture> {
    
    /**
     * 校验图片
     *
     * @param picture
     */
    void validPicture(Picture picture);

    /**  
     * 上传图片  
     *  
     * @param multipartFile  
     * @param pictureUploadRequest  
     * @param loginUser  
     * @return  
     */  
    PictureVO uploadPicture(Object multipartFile,  
                            PictureUploadRequest pictureUploadRequest,  
                            User loginUser);

    /**
     * 获取图片包装类（单条）
     *
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片包装类（分页）
     *
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**  
     * 图片审核  
     *  
     * @param pictureReviewRequest  
     * @param loginUser  
     */  
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);
    
    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 填充审核参数（改进版）
     *
     * @param picture
     * @param loginUser
     */
    void fillReviewParamsPlus(Picture picture, User loginUser);

    /**
     * 校验空间图片的权限
     *
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 清理图片文件
     *
     * @param oldPicture
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 删除图片
     *
     * @param pictureId
     * @param loginUser
     */
    void deletePicture(long pictureId, User loginUser);

    /**
     * 编辑图片
     *
     * @param pictureEditRequest
     * @param loginUser
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

}
