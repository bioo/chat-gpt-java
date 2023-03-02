package com.scyr.chat.openai;

import cn.hutool.http.*;
import com.scyr.chat.cache.Cache;
import com.scyr.chat.entity.APIKeyMoneyBalance;
import com.scyr.chat.openai.domain.request.CompletionRequest;
import com.scyr.chat.openai.domain.response.CompletionResult;
import com.scyr.chat.util.CommonUtils;
import com.scyr.chat.util.MapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

@Slf4j
public class OpenAiService {

    public static final String MODEL = "gpt-3.5-turbo";

    private static final String BASE_URL = "https://api.openai.com";

    private static final String V1 = "/v1";

    private static final String CHAT_URL = "/chat/completions";

    private static final String GET_BALANCE_URL = "/dashboard/billing/credit_grants";

    private static final String TOKEN_PREFIX = "Bearer ";

    private final String token;

    private final Integer timeoutSeconds;

    public OpenAiService(String token, Integer timeoutSeconds) {
        this.token = token;
        if (ObjectUtils.isEmpty(timeoutSeconds)) {
            timeoutSeconds = 10;
        }
        this.timeoutSeconds = timeoutSeconds;
    }

    public CompletionResult createCompletion(CompletionRequest completionRequest) throws Exception {
        String url = String.format("%s%s%s", BASE_URL, V1, CHAT_URL);
        String payload = MapperUtils.obj2jsonIgnoreNull(completionRequest);
        HttpResponse httpResponse = HttpRequest.post(url)
                .header(Header.AUTHORIZATION, String.format("%s%s", TOKEN_PREFIX, token))
                .body(payload)
                .timeout(timeoutSeconds * 1000)
                .execute();
        int httpStatusCode = httpResponse.getStatus();
        if (httpStatusCode == HttpStatus.HTTP_INTERNAL_ERROR || httpStatusCode == HttpStatus.HTTP_UNAVAILABLE ||
                httpStatusCode == HttpStatus.HTTP_BAD_REQUEST || httpStatusCode == 429) {
            httpResponse = retryRequest(url, payload);
            httpStatusCode = httpResponse.getStatus();
        }
        if (httpStatusCode != HttpStatus.HTTP_OK) {
            throw new HttpException("call chatGPT api: {}, response not ok, response code: {}", url, httpStatusCode);
        }
        String result = httpResponse.body();
        log.info("http status: {}, response body: {}", httpStatusCode, result);
        CompletionResult completionResult = MapperUtils.json2pojo(result, CompletionResult.class);
        log.info("response completionResult: {}", completionResult);
        return completionResult;
    }

    private HttpResponse retryRequest(String url, String payload) {
        log.info("尝试重试一次发送");
        //可能是同时请求过多，尝试重新发送
        CommonUtils.sleep(3L);
        return HttpRequest.post(url)
                .header(Header.AUTHORIZATION, String.format("%s%s", TOKEN_PREFIX, token))
                .body(payload)
                .timeout(timeoutSeconds * 1000)
                .execute();
    }

    public Float getApiKeyBalance() {
        String url = String.format("%s%s", BASE_URL, GET_BALANCE_URL);
        log.info("get api key: {} balance", token);
        Float available = null;
        try {
            String result = HttpRequest.get(url)
                    .header(Header.AUTHORIZATION, String.format("%s%s", TOKEN_PREFIX, token))
                    .execute()
                    .body();
            log.info("call apiKey balance response json: {}, apiKey is: {}", result, token);
            APIKeyMoneyBalance apiKeyMoneyBalance = MapperUtils.json2pojo(result, APIKeyMoneyBalance.class);
            log.info("call apiKey balance response json convert to domain is: {}, apiKey is: {}", apiKeyMoneyBalance, token);
            available = apiKeyMoneyBalance.getTotalAvailable();
        } catch (Exception e) {
            log.error("check api key: {} balance error: ", token, e);
        }
        // 这里返回最小额度，是因为避免错误判断返回0 导致从集合中删除了，相当于暂时下线改apiKey
        return ObjectUtils.isEmpty(available) ? Cache.MIN_API_KEY_BALANCE : available;
    }

}
