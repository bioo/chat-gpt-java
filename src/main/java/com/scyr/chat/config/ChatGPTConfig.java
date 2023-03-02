package com.scyr.chat.config;

import lombok.Data;

@Data
public class ChatGPTConfig {

    private String model = "gpt-3.5-turbo";

    private Double temperature;

    private Integer maxToken;

    private Double topP;

}
