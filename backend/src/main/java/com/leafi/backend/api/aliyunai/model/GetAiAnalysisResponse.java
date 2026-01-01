package com.leafi.backend.api.aliyunai.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

import com.leafi.backend.api.aliyunai.model.GetAiAnalysisResponse.Output;
@Deprecated
@Data
public class GetAiAnalysisResponse implements Serializable {
    /**
     * 请求 ID
     */
    private String requestId;

    /**
     * 输出信息
     */
    private Output output;

    /**
     * 接口错误码
     */
    private String code;

    /**
     * 接口错误描述
     */
    private String message;

    /**
     * 任务输出信息
     */
    public static class Output {   
        /**
         * 任务 ID
         */
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

        /**
         * AI 分析结果
         */
        private List<String> aiAnalysisResults;
    }

    private static final long serialVersionUID = 1L;
}