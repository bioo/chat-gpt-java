package com.scyr.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 成功标识
     */
    private static final Integer SUCCESS_CODE = 1;

    /**
     * 失败标识
     */
    private static final Integer FAILURE_CODE = 0;

    /**
     * 响应码
     * 1 成功
     * 0 失败
     */
    private Integer code;
    /**
     * 响应信息
     */
    private String message;
    /**
     * 响应数据
     */
    private T data;
    /**
     * 响应时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dateTime;

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "请求成功", data, new Date());
    }

    public static Result<String> failure(String message) {
        return new Result<>(FAILURE_CODE, message, null, new Date());
    }

    public static <T> Result<T> failure(T obj) {
        return new Result<>(FAILURE_CODE, "请求失败", obj, new Date());
    }


    public static Result<Object> failure() {
        return new Result<>(FAILURE_CODE, "请求失败", null, new Date());
    }

    public static <T> Result<T> failure(String message, T obj) {
        return new Result<>(FAILURE_CODE, message, obj, new Date());
    }

}
