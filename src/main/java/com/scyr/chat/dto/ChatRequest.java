package com.scyr.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 问题请求
 */
@Data
public class ChatRequest implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 会话id
     */
    private String sessionId;

    /**
     * 问题
     */
    private String question;

    /**
     * 请求时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date requestDate = new Date();

}
