package com.scyr.chat.service.impl;

import com.scyr.chat.dto.ChatRequest;
import com.scyr.chat.dto.ChatResponse;
import com.scyr.chat.entity.OpenAiServiceInfo;
import com.scyr.chat.exception.ChatException;
import com.scyr.chat.handler.ChatGPTService;
import com.scyr.chat.service.InteractService;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import retrofit2.HttpException;

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
        String prompt = chatGPTService.getPrompt(chatRequest.getSessionId(), chatRequest.getQuestion());
        //向gpt提问
        OpenAiServiceInfo openAiServiceToUse = chatGPTService.getOpenAiService(chatRequest.getSessionId());
        OpenAiService openAiService = openAiServiceToUse.getOpenAiService();
        CompletionRequest completionRequest = chatGPTService.getBasicCompletionRequestBuilder().prompt(prompt).build();
        String answer = null;
        try {
            answer = openAiService.createCompletion(completionRequest).getChoices().get(0).getText();
        } catch (HttpException e) {
            log.error("向gpt提问失败，提问内容：{}，原因：{}", chatRequest.getQuestion(), e.getMessage(), e);
            if (500 == e.code() || 503 == e.code() || 429 == e.code()) {
                log.info("尝试重新发送");
                try {
                    //可能是同时请求过多，尝试重新发送
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    log.error("进程休眠失败，原因：{}", ex.getMessage(), ex);
                    throw new RuntimeException(ex);
                }
                return chat(chatRequest);
            }
        }
        if (null == answer) {
            throw new ChatException("GPT可能暂时不想理你");
        }
        //去除gpt假设的用户提问
        int userIndex = answer.indexOf("User:");
        if (-1 != userIndex) {
            answer = answer.substring(0, userIndex - 1) + "<|im_end|>";
        }
        chatGPTService.updatePrompt(chatRequest.getSessionId(), chatRequest.getQuestion(), answer);
        answer = answer.replace("<|im_end|>", "").trim();
        chatResponse.setAnswer(answer);
        return chatResponse;
    }
}
