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
    PictureVO uploadPicture(MultipartFile multipartFile,  
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
}
