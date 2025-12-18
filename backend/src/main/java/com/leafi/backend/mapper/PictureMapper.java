
package com.leafi.backend.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.leafi.backend.model.entity.Picture;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;

/**
* @author leafi
* @description 针对表【picture(图片)】的数据库操作Mapper
*/
public interface PictureMapper extends BaseMapper<Picture> {

}