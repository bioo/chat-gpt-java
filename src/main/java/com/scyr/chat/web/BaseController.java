package com.scyr.chat.web;

import com.scyr.chat.cache.Cache;
import com.scyr.chat.exception.ChatException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通用的控制器
 */
@Slf4j
public abstract class BaseController {

    @Resource
    protected HttpServletRequest request;
    @Resource
    protected HttpServletResponse response;

    @ModelAttribute
    public void initReqAndRes(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        String requestURI = request.getRequestURI();
        log.info("requestURI ------> {}", requestURI);
        initResponse();
    }

    /**
     * 初始化 HttpServletResponse
     */
    private void initResponse() {
        // 需要符合 JSON API 规范
        try {
            response.setHeader("Content-Type", "application/vnd.api+json");
            response.addHeader("Cache-Control", "must-revalidate");
            response.addHeader("Cache-Control", "no-cache");
            response.addHeader("Cache-Control", "no-store");
            response.setHeader("Access-Control-Expose-Headers", "Authorization");
        } catch (Exception ignored) {
        }
    }

    protected String getHeaderSessionId() throws ChatException {
        String sessionHeaderName = "SessionId";
        String sessionId = request.getHeader(sessionHeaderName);
        Map<String, String> cookies = new HashMap<>();
        Arrays.stream(ObjectUtils.isEmpty(request.getCookies()) ? new Cookie[0] : request.getCookies())
                .forEach(cookie -> cookies.put(cookie.getName(), cookie.getValue()));
        if (StringUtils.isBlank(sessionId)) {
            sessionId = cookies.getOrDefault(sessionHeaderName, null);
            if (StringUtils.isBlank(sessionId)) {
                sessionId = UUID.randomUUID().toString();
                response.addHeader(sessionHeaderName, sessionId);
                response.addCookie(new Cookie(sessionHeaderName, sessionId));
            } else {
                if (!Cache.SESSION_ACTIVE_MAP.containsKey(sessionId)) {
                    response.setHeader(sessionHeaderName, null);
                    Cookie cookie = new Cookie(sessionHeaderName, null);
                    cookie.setMaxAge(0);
                    String requestURI = request.getRequestURI();
                    cookie.setPath(requestURI.substring(0, requestURI.lastIndexOf("/")));
                    response.addCookie(cookie);
                    throw new ChatException("会话已过期，请重试");
                }
            }
        }
        return sessionId;
    }
}
