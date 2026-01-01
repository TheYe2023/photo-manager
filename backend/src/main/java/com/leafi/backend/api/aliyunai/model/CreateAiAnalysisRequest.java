package com.leafi.backend.api.aliyunai.model;

import java.io.Serializable;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

@Data
public class CreateAiAnalysisRequest implements Serializable {
    /**
     * 模型
     */
    private String model;

    /**
     * 输入
     */
    private Input input;

    @Data
    public static class Input {
        /**
         * 图片URL
         */
        @Alias("ImageUrl")
        private String imageUrl;
    }


}
