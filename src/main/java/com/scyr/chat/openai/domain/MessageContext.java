package com.scyr.chat.openai.domain;

import lombok.*;

@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageContext {

    /**
     * 对话角色
     */
    private String role;

    /**
     * 对话内容
     */
    private String content;

}
