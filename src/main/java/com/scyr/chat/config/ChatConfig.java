package com.scyr.chat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@ConfigurationProperties(prefix = "open-ai")
@Component
@Data
public class ChatConfig {

    private Set<String> tokens;

    private Integer timeOutSeconds;

    private ChatGPTConfig chatGPT;

}
