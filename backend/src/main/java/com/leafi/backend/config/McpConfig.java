package com.leafi.backend.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import java.util.function.Function;

import com.leafi.backend.service.PictureService;
import com.leafi.backend.model.dto.picture.McpPictureSearchRequest;

@Configuration
public class McpConfig {

    PictureService pictureService;

    /**
     * 注册 ChatClient Bean
     * Spring AI 会自动注入 ChatClient.Builder
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        // 这里可以配置默认的 System Prompt 或者默认的工具
        return builder
                .defaultSystem("你是一个专业的智能照片管家。")
                .build();
    }

    /**
     * 注册 PictureService 作为工具回调提供者
     */
    @Bean
    public ToolCallbackProvider pictureTools(PictureService pictureService) {
        // 使用 Spring AI 标准的 MethodToolCallbackProvider
        return MethodToolCallbackProvider.builder()
                .toolObjects(pictureService) 
                .build();
    }

    @Bean
    @Description("搜索图片库。支持关键词、描述或标签搜索。")
    public Function<McpPictureSearchRequest, String> callPictureSearch(PictureService pictureService) {
        // 这里的参数直接传入 pictureService，Spring 会自动注入已经创建好的 Service 实例
        return request -> pictureService.callPictureSearch(request.getSearchText());
    }
}