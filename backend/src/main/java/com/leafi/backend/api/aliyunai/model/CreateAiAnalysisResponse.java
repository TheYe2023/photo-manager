package com.leafi.backend.api.aliyunai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Deprecated
@NoArgsConstructor
@AllArgsConstructor
public class CreateAiAnalysisResponse {

    private Output output;

    /**
     * 请求 ID
     */
    private String requestId;

    /**
     * 任务输出
     */
    @Data
    public static class Output {
        private String taskId;

        /**
         * 任务状态
         * 
         * <ul>
         *      <li>QUEUEING：排队中</li>
         *      <li>SUSPENDED：已暂停</li>
         *      <li>PROCESSING：处理中</li>
         *      <li>SUCCESS：成功</li>
         *      <li>FAILED：失败</li>
         *      <li>UNKNOW：未知</li>
         * </ul>
         */
        private String status;
    }

    /**
     * 接口错误码
     */
    private String code;

    /**
     * 接口错误描述
     */
    private String message;

}
