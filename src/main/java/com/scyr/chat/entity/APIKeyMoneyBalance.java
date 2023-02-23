package com.scyr.chat.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class APIKeyMoneyBalance {

    /**
     * 类型
     */
    private String object;

    /**
     * 总共的额度
     */
    @JsonProperty("total_granted")
    private Float totalGranted;

    /**
     * 已使用额度
     */
    @JsonProperty("total_used")
    private Float totalUsed;

    /**
     * 剩余额度
     */
    @JsonProperty("total_available")
    private Float totalAvailable;

    /**
     * 这个字段与余额判断无关，可以忽略
     */
    private Object grants;

}
