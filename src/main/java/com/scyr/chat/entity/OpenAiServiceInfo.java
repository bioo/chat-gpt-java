package com.scyr.chat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.scyr.chat.cache.Cache;
import com.scyr.chat.openai.OpenAiService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class OpenAiServiceInfo implements Comparable<OpenAiServiceInfo> {

    /**
     * openAiService 句柄
     */
    @JsonIgnore
    private OpenAiService openAiService;

    /**
     * key
     */
    private String apiKey;

    /**
     * 余额
     */
    private Float availableBalance;

    /**
     * 当前使用 session 个数
     */
    private Integer useAmount = 0;

    /**
     * 最后更改余额时间
     */
    private Long lastUpdateBalanceTime = System.currentTimeMillis();

    /**
     * 最后更改 session 个数时间
     */
    private Long lastUpdateUseAmountTime = System.currentTimeMillis();

    public OpenAiServiceInfo(OpenAiService openAiService, String apiKey, Float availableBalance) {
        this.openAiService = openAiService;
        this.apiKey = apiKey;
        this.availableBalance = availableBalance;
    }

    public void setAvailableBalance(Float availableBalance) {
        this.availableBalance = availableBalance;
        this.lastUpdateBalanceTime = System.currentTimeMillis();
    }

    public synchronized void addUseAmount(String sessionId) {
        this.useAmount = this.useAmount + 1;
        Cache.SESSION_ACTIVE_MAP.put(sessionId, new ChatSessionActiveInfo(System.currentTimeMillis(), this.apiKey));
        this.lastUpdateUseAmountTime = System.currentTimeMillis();
    }

    public synchronized void refreshAccessTime(String sessionId) {
        Cache.SESSION_ACTIVE_MAP.put(sessionId, new ChatSessionActiveInfo(System.currentTimeMillis(), this.apiKey));
    }

    public synchronized void subUseAmount() {
        if (this.useAmount > 0) {
            this.useAmount = this.useAmount - 1;
        }
        this.lastUpdateUseAmountTime = System.currentTimeMillis();
    }

    @Override
    public int compareTo(OpenAiServiceInfo o) {
        // 升序
        return this.getUseAmount() - o.getUseAmount();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenAiServiceInfo that = (OpenAiServiceInfo) o;
        return StringUtils.equals(apiKey, that.apiKey);
    }

    @Override
    public int hashCode() {
        return StringUtils.isNotBlank(apiKey) ? apiKey.hashCode() : 0;
    }
}
