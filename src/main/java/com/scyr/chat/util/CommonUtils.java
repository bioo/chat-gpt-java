package com.scyr.chat.util;

import org.apache.commons.lang3.ObjectUtils;

public class CommonUtils {

    public static void sleep(Long second) {
        if (ObjectUtils.isEmpty(second)) {
            second = 0L;
        }
        try {
            Thread.sleep(second * 1000);
        } catch (InterruptedException ignored) {
        }
    }

}
