package com.scyr.chat.openai.domain.response;


import lombok.Data;

import java.util.List;

@Data
public class CompletionResult {

    private String id;

    private String object;

    private Long created;

    private String model;

    private List<CompletionChoice> choices;

    private Usage usage;

}
