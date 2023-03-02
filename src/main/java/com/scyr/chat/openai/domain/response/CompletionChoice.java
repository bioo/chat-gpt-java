package com.scyr.chat.openai.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scyr.chat.openai.domain.MessageContext;
import lombok.Data;

@Data
public class CompletionChoice {

    private MessageContext message;

    @JsonProperty(value = "finish_reason")
    private String finishReason;

    private Integer index;

}
