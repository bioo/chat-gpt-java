package com.scyr.chat.config;

import lombok.Data;

@Data
public class ChatGPTConfig {

    private String model;

    private Double temperature;

    private Integer maxToken;

    private Double topP;

    private Boolean echo;

}
