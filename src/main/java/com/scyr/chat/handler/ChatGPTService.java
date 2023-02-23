package com.scyr.chat.handler;

import com.scyr.chat.cache.Cache;
import com.scyr.chat.config.ChatConfig;
import com.scyr.chat.entity.OpenAiServiceInfo;
import com.scyr.chat.exception.ChatException;
import com.scyr.chat.util.HttpUtil;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ChatGPTService {

    @Resource
    private ChatConfig chatConfig;

    private CompletionRequest.CompletionRequestBuilder completionRequestBuilder;

    @PostConstruct
    public void init() {
        //ChatGPT
        completionRequestBuilder = CompletionRequest.builder()
                .model(chatConfig.getChatGPT().getModel())
                .temperature(chatConfig.getChatGPT().getTemperature())
                .maxTokens(chatConfig.getChatGPT().getMaxToken());
        for (String apiKey : chatConfig.getTokens()) {
            apiKey = apiKey.trim();
            if (StringUtils.isNotBlank(apiKey)) {
                Cache.OPEN_AI_SERVICE_INFO_MAP.put(apiKey, new OpenAiServiceInfo(
                        new OpenAiService(apiKey, Duration.ofSeconds(chatConfig.getTimeOutSeconds())),
                        apiKey,
                        HttpUtil.getApiKeyBalance(apiKey)
                ));
                log.info("apiKey为 {} 的账号初始化成功", apiKey);
            }
        }
    }

    public OpenAiServiceInfo getOpenAiService(String sessionId) throws ChatException {
        boolean firstSight = false;
        OpenAiServiceInfo openAiServiceToUse = null;
        // 如果这个sessionId 是已经有过对话的，则先拿出上一次使用的那个 apiKey 对应的service
        if (Cache.SESSION_ACTIVE_MAP.containsKey(sessionId)) {
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
        }
        // 如果这个sessionId 是首次来对话，或者之前使用的apiKey 额度已经用完，则重新选一个
        if (ObjectUtils.isEmpty(openAiServiceToUse)) {
            firstSight = true;
            List<OpenAiServiceInfo> hasBalance = Cache.OPEN_AI_SERVICE_INFO_MAP.values().stream()
                    .filter(value -> value.getAvailableBalance() > Cache.MIN_API_KEY_BALANCE)
                    .sorted().collect(Collectors.toList());
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

    public CompletionRequest.CompletionRequestBuilder getBasicCompletionRequestBuilder() {
        return this.completionRequestBuilder;
    }

    public String getPrompt(String sessionId, String newPrompt) throws ChatException {
        StringBuilder prompt = new StringBuilder(String.format(Cache.BASIC_PROMPT, LocalDate.now()));
        Map<String, Queue<String>> promptMap = Cache.PROMPT_CONTEXT_MAP;
        if (promptMap.containsKey(sessionId)) {
            for (String s : promptMap.get(sessionId)) {
                prompt.append(s).append("\n");
            }
        }
        prompt.append("User: ").append(newPrompt).append("\nChatGPT: ");
        //一个汉字大概两个token
        //预设回答的文字是提问文字数量的两倍
        if (chatConfig.getChatGPT().getMaxToken() < (prompt.toString().length() + newPrompt.length()) * 2) {
            if (null == promptMap.get(sessionId) || null == promptMap.get(sessionId).poll()) {
                throw new ChatException("问题太长了");
            }
            return getPrompt(sessionId, newPrompt);
        }
        return prompt.toString();
    }

    public void updatePrompt(String sessionId, String prompt, String answer) {
        Map<String, Queue<String>> promptMap = Cache.PROMPT_CONTEXT_MAP;
        if (promptMap.containsKey(sessionId)) {
            promptMap.get(sessionId).offer("User: " + prompt + "\nChatGPT: " + answer);
        } else {
            Queue<String> queue = new LinkedList<>();
            queue.offer("User: " + prompt + "\nChatGPT: " + answer);
            promptMap.put(sessionId, queue);
        }
    }

}
