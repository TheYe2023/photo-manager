package com.leafi.backend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatRequest implements Serializable {
    /**
     * 用户输入的聊天消息
     */
    private String message;

    private static final long serialVersionUID = 1L;
}
