package com.scyr.chat.service.impl;

import com.scyr.chat.dto.ChatRequest;
import com.scyr.chat.dto.ChatResponse;
import com.scyr.chat.entity.OpenAiServiceInfo;
import com.scyr.chat.exception.ChatException;
import com.scyr.chat.handler.ChatGPTService;
import com.scyr.chat.openai.OpenAiService;
import com.scyr.chat.openai.domain.MessageContext;
import com.scyr.chat.openai.domain.request.CompletionRequest;
import com.scyr.chat.service.InteractService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 交互服务impl
 */
@Service
@Slf4j
public class InteractServiceImpl implements InteractService {

    @Resource
    ChatGPTService chatGPTService;

    @Override
    public ChatResponse chat(ChatRequest chatRequest) throws ChatException {
        ChatResponse chatResponse = new ChatResponse();
        BeanUtils.copyProperties(chatRequest, chatResponse);
        if (StringUtils.isBlank(chatRequest.getQuestion())) {
            throw new ChatException("你没有提出任何问题！");
        }
        MessageContext questionMessage = MessageContext.builder().role("user").content(chatRequest.getQuestion()).build();
        List<MessageContext> question = chatGPTService.getQuestion(chatRequest.getSessionId(), questionMessage);
        //向gpt提问
        OpenAiServiceInfo openAiServiceToUse = chatGPTService.getOpenAiService(chatRequest.getSessionId());
        OpenAiService openAiService = openAiServiceToUse.getOpenAiService();
        CompletionRequest completionRequest = chatGPTService.getBasicCompletionRequestBuilder().messages(question).build();
        MessageContext answerMessage = null;
        try {
            answerMessage = openAiService.createCompletion(completionRequest).getChoices().get(0).getMessage();
        } catch (Exception e) {
            log.error("向gpt提问失败，提问内容：{}，原因：{}", chatRequest.getQuestion(), e.getMessage(), e);
        }
        if (ObjectUtils.isEmpty(answerMessage) || StringUtils.isBlank(answerMessage.getContent())) {
            throw new ChatException("GPT可能暂时不想理你");
        }
        chatGPTService.updatePrompt(chatRequest.getSessionId(),questionMessage, answerMessage);
        String answer = StringUtils.trim(answerMessage.getContent());
        chatResponse.setAnswer(answer);
        return chatResponse;
    }
}
