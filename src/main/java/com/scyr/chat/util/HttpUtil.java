package com.scyr.chat.util;

import com.scyr.chat.cache.Cache;
import com.scyr.chat.entity.APIKeyMoneyBalance;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class HttpUtil {

    /**
     * get 请求
     */
    public static String get(String url) throws IOException {
        return get(url, new HashMap<>());
    }

    public static String get(String url, Map<String, String> header) throws IOException {
        // 1.获取OkHttpClient对象
        OkHttpClient client = new OkHttpClient();
        // 2.设置请求
        Request.Builder builder = new Request.Builder()
                .get()
                .url(url);
        header.forEach(builder::addHeader);
        Request request = builder.build();
        // 3.封装call
        Call call = client.newCall(request);
        // 4.同步调用,返回Response,会抛出IO异常
        Response response = call.execute();
        if (response.isSuccessful()) {
            return Objects.requireNonNull(response.body()).string();
        }
        throw new RuntimeException("call api error, response: " + response);
    }

    public static Float getApiKeyBalance(String apiKey) {
        log.info("call apiKey balance apiKey is: {}", apiKey);
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + apiKey);
        String url = "https://api.openai.com/dashboard/billing/credit_grants";
        Float available = null;
        try {
            String json = HttpUtil.get(url, header);
            log.info("call apiKey balance response json: {}, apiKey is: {}", json, apiKey);
            APIKeyMoneyBalance apiKeyMoneyBalance = MapperUtils.json2pojo(json, APIKeyMoneyBalance.class);
            log.info("call apiKey balance response json convert to domain is: {}, apiKey is: {}", apiKeyMoneyBalance, apiKey);
            available = apiKeyMoneyBalance.getTotalAvailable();
        } catch (Exception e) {
            log.error("check apiKey balance error: ", e);
        }
        // 这里返回最小额度，是因为避免错误判断返回0 导致从集合中删除了，相当于暂时下线改apiKey
        return ObjectUtils.isEmpty(available) ? Cache.MIN_API_KEY_BALANCE : available;
    }

}
