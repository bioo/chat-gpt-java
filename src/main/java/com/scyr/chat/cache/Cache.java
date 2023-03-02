package com.scyr.chat.cache;

import com.scyr.chat.entity.ChatSessionActiveInfo;
import com.scyr.chat.entity.OpenAiServiceInfo;
import com.scyr.chat.openai.domain.MessageContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {

    public final static Float MIN_API_KEY_BALANCE = 0.1F;

    public final static Long SESSION_EXPIRATION_DURATION = 60 * 1000L;

    public final static Map<String, OpenAiServiceInfo> OPEN_AI_SERVICE_INFO_MAP = new LinkedHashMap<>();

    public static final Map<String, Queue<MessageContext>> QUESTION_CONTEXT_MAP = new ConcurrentHashMap<>();

    public static final Map<String, ChatSessionActiveInfo> SESSION_ACTIVE_MAP = new ConcurrentHashMap<>();

}
