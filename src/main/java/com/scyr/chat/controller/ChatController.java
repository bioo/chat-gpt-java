package com.scyr.chat.controller;

import com.scyr.chat.cache.Cache;
import com.scyr.chat.config.ChatConfig;
import com.scyr.chat.dto.ChatRequest;
import com.scyr.chat.dto.ChatResponse;
import com.scyr.chat.dto.Result;
import com.scyr.chat.exception.ChatException;
import com.scyr.chat.service.InteractService;
import com.scyr.chat.web.BaseController;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController extends BaseController {

    @Resource
    private ChatConfig chatConfig;

    @Resource
    private InteractService interactService;


    @GetMapping("/config")
    public Result<ChatConfig> getToken() {
        return Result.success(chatConfig);
    }

    @GetMapping("/cache")
    public Result<Map<String, Object>> cache() {
        Map<String, Object> map = new HashMap<>();
        map.put("open_ai_service_info_map", Cache.OPEN_AI_SERVICE_INFO_MAP);
        map.put("prompt_context_map", Cache.PROMPT_CONTEXT_MAP);
        map.put("session_active_map", Cache.SESSION_ACTIVE_MAP);
        return Result.success(map);
    }

    @PostMapping("/question")
    public Result<ChatResponse> question(@RequestBody ChatRequest chatRequest) {
        ChatResponse chatResponse = new ChatResponse();
        try {
            chatRequest.setSessionId(getHeaderSessionId());
            BeanUtils.copyProperties(chatRequest, chatResponse);
            return Result.success(interactService.chat(chatRequest));
        } catch (ChatException e) {
            chatResponse.setAnswer(e.getMessage());
            return Result.failure(e.getMessage(), chatResponse);
        }
    }


}
