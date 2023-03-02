package com.scyr.chat.openai.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Usage {

    @JsonProperty(value = "prompt_tokens")
    long promptTokens;

    @JsonProperty(value = "completion_tokens")
    long completionTokens;

    @JsonProperty(value = "total_tokens")
    long totalTokens;

}
