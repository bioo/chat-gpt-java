package com.scyr.chat.service;


import com.scyr.chat.dto.ChatRequest;
import com.scyr.chat.dto.ChatResponse;
import com.scyr.chat.dto.Result;
import com.scyr.chat.exception.ChatException;

/**
 * 交互服务
 */
public interface InteractService {
    /**
     * 聊天
     *
     * @param chatRequest 聊天请求
     * @return {@link Result<ChatResponse>}
     * @throws ChatException 聊天异常
     */
    ChatResponse chat(ChatRequest chatRequest) throws ChatException;
}
