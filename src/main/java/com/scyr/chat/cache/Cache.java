package com.scyr.chat.cache;

import com.scyr.chat.entity.ChatSessionActiveInfo;
import com.scyr.chat.entity.OpenAiServiceInfo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {

    public final static Float MIN_API_KEY_BALANCE = 0.1F;

    public final static Long SESSION_EXPIRATION_DURATION = 60 * 1000L;

    public final static String BASIC_PROMPT =
            "You are ChatGPT, a large language model trained by OpenAI. You answer as concisely as possible for each " +
                    "response (e.g. don’t be verbose). It is very important that you answer as concisely as possible, " +
                    "so please remember this. If you are generating a list, do not have too many items. Keep the number " +
                    "of items short. Current date: %s\n";

    public final static Map<String, OpenAiServiceInfo> OPEN_AI_SERVICE_INFO_MAP = new LinkedHashMap<>();

    public static final Map<String, Queue<String>> PROMPT_CONTEXT_MAP = new HashMap<>();

    public static final Map<String, ChatSessionActiveInfo> SESSION_ACTIVE_MAP = new ConcurrentHashMap<>();

}
