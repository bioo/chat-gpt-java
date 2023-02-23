package com.scyr.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 问题请求
 */
@Data
public class ChatResponse extends ChatRequest implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 答案
     */
    private String answer = StringUtils.EMPTY;

    /**
     * 返回时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date responseDate = new Date();

}
