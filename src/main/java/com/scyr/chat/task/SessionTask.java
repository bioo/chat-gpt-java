package com.scyr.chat.task;


import com.scyr.chat.cache.Cache;
import com.scyr.chat.entity.OpenAiServiceInfo;
import com.scyr.chat.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class SessionTask {

    @Scheduled(cron = "0/5 * *  * * ? ")   //每5秒执行一次
    public void clearSessionExecute() {
        Set<String> needRemoveSession = new HashSet<>();
        Cache.SESSION_ACTIVE_MAP.forEach((sessionId, activeInfo) -> {
            if (System.currentTimeMillis() - activeInfo.getLastRequestTime() > Cache.SESSION_EXPIRATION_DURATION) {
                needRemoveSession.add(sessionId);
            }
        });
        log.info("需要删除的sessionId有：{}", needRemoveSession);
        needRemoveSession.forEach(sessionId -> {
            OpenAiServiceInfo openAiServiceInfo = Cache.OPEN_AI_SERVICE_INFO_MAP.get(Cache.SESSION_ACTIVE_MAP.get(sessionId).getLastUseApiKey());
            if (ObjectUtils.isNotEmpty(openAiServiceInfo)) {
                openAiServiceInfo.subUseAmount();
            }
            Cache.SESSION_ACTIVE_MAP.remove(sessionId);
            Cache.PROMPT_CONTEXT_MAP.remove(sessionId);
        });
    }

    @Scheduled(cron = "0/60 * *  * * ? ")   //每60秒执行一次
    public void clearApiKeyExecute() {
        Set<String> needRemoveApiKey = new HashSet<>();
        Cache.OPEN_AI_SERVICE_INFO_MAP.values().forEach(openAiServiceInfo -> {
            Float apiKeyBalance = HttpUtil.getApiKeyBalance(openAiServiceInfo.getApiKey());
            if (apiKeyBalance < Cache.MIN_API_KEY_BALANCE) {
                needRemoveApiKey.add(openAiServiceInfo.getApiKey());
            }
            openAiServiceInfo.setAvailableBalance(apiKeyBalance);
            sleep(1L);
        });
        log.info("需要删除的ApiServiceInfo有：{}", needRemoveApiKey);
        needRemoveApiKey.forEach(Cache.OPEN_AI_SERVICE_INFO_MAP::remove);
    }

    private void sleep(Long second) {
        if (ObjectUtils.isEmpty(second)){
            second = 0L;
        }
        try {
            Thread.sleep(second * 1000);
        } catch (InterruptedException ignored) {
        }
    }

}
