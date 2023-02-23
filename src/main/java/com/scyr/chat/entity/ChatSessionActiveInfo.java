package com.scyr.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ChatSessionActiveInfo {

    /**
     * 最后访问时间
     */
    private Long lastRequestTime;

    /**
     * 最后一次使用的apiKey
     */
    private String lastUseApiKey;

}
