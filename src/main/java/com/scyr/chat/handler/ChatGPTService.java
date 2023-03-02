package com.scyr.chat.handler;

import com.scyr.chat.cache.Cache;
import com.scyr.chat.config.ChatConfig;
import com.scyr.chat.entity.OpenAiServiceInfo;
import com.scyr.chat.exception.ChatException;
import com.scyr.chat.openai.OpenAiService;
import com.scyr.chat.openai.domain.MessageContext;
import com.scyr.chat.openai.domain.request.CompletionRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

@Slf4j
@Component
public class ChatGPTService {

    @Resource
    private ChatConfig chatConfig;

    @PostConstruct
    public void init() {
        for (String apiKey : chatConfig.getTokens()) {
            apiKey = apiKey.trim();
            if (StringUtils.isNotBlank(apiKey)) {
                OpenAiService openAiService = new OpenAiService(apiKey, chatConfig.getTimeoutSeconds());
                Cache.OPEN_AI_SERVICE_INFO_MAP.put(apiKey, new OpenAiServiceInfo(
                        openAiService,
                        apiKey,
                        openAiService.getApiKeyBalance()
                ));
                log.info("api key: {} account initialization succeeded", apiKey);
            }
        }
    }

    public OpenAiServiceInfo getOpenAiService(String sessionId) throws ChatException {
        boolean firstSight = false;
        OpenAiServiceInfo openAiServiceToUse = null;
        // 如果这个sessionId 是已经有过对话的，则先拿出上一次使用的那个 apiKey 对应的service
        if (Cache.SESSION_ACTIVE_MAP.containsKey(sessionId)) {
            openAiServiceToUse = getOpenAiServiceInfo(sessionId);
        }
        // 如果这个sessionId 是首次来对话，或者之前使用的apiKey 额度已经用完，则重新选一个
        if (ObjectUtils.isEmpty(openAiServiceToUse)) {
            firstSight = true;
            List<OpenAiServiceInfo> hasBalance = Cache.OPEN_AI_SERVICE_INFO_MAP.values().stream()
                    .filter(value -> value.getAvailableBalance() > Cache.MIN_API_KEY_BALANCE)
                    .sorted().toList();
            //获取使用次数最小的openAiService 否则获取map中的第一个
            openAiServiceToUse = hasBalance.stream().findFirst().orElse(null);
        }
        if (ObjectUtils.isEmpty(openAiServiceToUse)) {
            log.warn("没有可用的 OpenAiService, 请管理员检查是否所有apiKey额度已用完");
            throw new ChatException("暂时没有可用的GPT节点");
        }
        if (firstSight) {
            openAiServiceToUse.addUseAmount(sessionId);
        }
        openAiServiceToUse.refreshAccessTime(sessionId);
        return openAiServiceToUse;
    }

    private static OpenAiServiceInfo getOpenAiServiceInfo(String sessionId) {
        OpenAiServiceInfo openAiServiceToUse;
        openAiServiceToUse = Cache.OPEN_AI_SERVICE_INFO_MAP.get(Cache.SESSION_ACTIVE_MAP.get(sessionId).getLastUseApiKey());
        // 被定时任务判断为额度不够后，从OPEN_AI_SERVICE_INFO_MAP删除后，里get到的是null，则同样需要重新选service
        if (ObjectUtils.isEmpty(openAiServiceToUse)) {
            openAiServiceToUse = null;
        } else {
            if (openAiServiceToUse.getAvailableBalance() <= Cache.MIN_API_KEY_BALANCE) {
                // 如果这个过期了，则要把这个 subUseAmount
                openAiServiceToUse.subUseAmount();
                openAiServiceToUse = null;
            }
        }
        return openAiServiceToUse;
    }

    public CompletionRequest.CompletionRequestBuilder getBasicCompletionRequestBuilder() {
        return CompletionRequest.builder()
                .model(OpenAiService.MODEL)
                .temperature(chatConfig.getChatGPT().getTemperature())
                .maxTokens(chatConfig.getChatGPT().getMaxToken());
    }

    public List<MessageContext> getQuestion(String sessionId, @NonNull MessageContext questionMessage) throws ChatException {
        if (chatConfig.getChatGPT().getMaxToken() < StringUtils.length(questionMessage.getContent()) * 2) {
            throw new ChatException("问题太长了");
        }
        List<MessageContext> resultList = new LinkedList<>();
        Map<String, Queue<MessageContext>> questionContextMap = Cache.QUESTION_CONTEXT_MAP;
        if (questionContextMap.containsKey(sessionId)) {
            resultList = getMessageContexts(sessionId, questionMessage, resultList, questionContextMap);
        }
        resultList.add(questionMessage);
        return resultList;
    }

    private List<MessageContext> getMessageContexts(String sessionId, MessageContext questionMessage, List<MessageContext> resultList, Map<String, Queue<MessageContext>> questionContextMap) {
        Queue<MessageContext> messageContexts = questionContextMap.get(sessionId);
        // 一个汉字大概两个token 预设回答的文字是提问文字数量的两倍
        while (chatConfig.getChatGPT().getMaxToken() <
                (2 *
                        (
                                new LinkedList<>(messageContexts).stream()
                                        .mapToInt(messageContext -> StringUtils.length(messageContext.getContent()))
                                        .sum()
                                        +
                                        StringUtils.length(questionMessage.getContent())
                        )
                )
        ) {
            // 从队首开始丢弃
            messageContexts.poll();
        }
        // 如果丢完了（前一个回答 + 当前问题 超出了最大 max_tokens），就重新开启一个会话（不是很合理，可以优化）
        if (ObjectUtils.isNotEmpty(messageContexts)) {
            resultList = new LinkedList<>(messageContexts);
        }
        return resultList;
    }

    public void updatePrompt(String sessionId, MessageContext question, MessageContext answer) {
        Map<String, Queue<MessageContext>> questionContextMap = Cache.QUESTION_CONTEXT_MAP;
        if (questionContextMap.containsKey(sessionId)) {
            Queue<MessageContext> queue = questionContextMap.get(sessionId);
            queue.offer(question);
            queue.offer(answer);
        } else {
            Queue<MessageContext> queue = new LinkedList<>();
            queue.offer(question);
            queue.offer(answer);
            questionContextMap.put(sessionId, queue);
        }
    }

}
