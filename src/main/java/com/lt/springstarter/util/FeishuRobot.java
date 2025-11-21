package com.lt.springstarter.util;

import com.alibaba.nacos.shaded.com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 飞书机器人工具类
 * 用于发送消息到飞书群
 */
@Slf4j
@Component
public class FeishuRobot {

    private static final String BASE_URL = "https://open.feishu.cn/open-apis/bot/v2/hook/";
    private static final String DEFAULT_GROUP = "603bc7ea-d763-4363-94a4-xxx";
    private static final String DEFAULT_TOKEN = "Nj5rjIfs1hhFdv1Xunixxx";

    private final OkHttpClient okHttpClient;

    @Value("${spring.application.name:unknown}")
    private String appName;

    @Value("${spring.profiles.active:unknown}")
    private String appEnv;

    public FeishuRobot(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    /**
     * 发送文本消息到飞书群
     *
     * @param content 要发送的文本内容
     * @return 响应结果，失败返回null
     */
    public String sendTextMessage(String content) {
        return sendTextMessage(content, null, null);
    }

    /**
     * 发送文本消息到飞书群
     *
     * @param content 要发送的文本内容
     * @param group   群组ID, 为null时使用默认群组
     * @param token   token, 为null时使用默认token
     * @return 响应结果，失败返回null
     */
    public String sendTextMessage(String content, String group, String token) {
        try {
            String url = BASE_URL + (group != null ? group : DEFAULT_GROUP);
            String secretToken = token != null ? token : DEFAULT_TOKEN;

            // 生成时间戳
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            // 生成签名
            String sign = genSign(timestamp, secretToken);

            // 构建完整的消息内容
            String fullContent = String.format("项目: %s\n环境: %s\n错误信息: %s",
                    appName, appEnv, content);

            // 构建请求数据
            Map<String, Object> data = buildRequestData(fullContent, timestamp, sign);

            // 发送请求
            return postRequest(url, data);

        } catch (Exception e) {
            log.error("机器人发送消息时出现请求错误: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 构建请求数据
     */
    private Map<String, Object> buildRequestData(String content, String timestamp, String sign) {
        Map<String, Object> data = new HashMap<>();
        data.put("msg_type", "text");
        data.put("timestamp", timestamp);
        data.put("sign", sign);

        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("text", content);
        data.put("content", contentMap);

        return data;
    }

    /**
     * 发送POST请求
     */
    private String postRequest(String url, Map<String, Object> data) throws Exception {
        String jsonData = new Gson().toJson(data);

        RequestBody body = RequestBody.create(
                jsonData.getBytes(StandardCharsets.UTF_8)
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                log.error("飞书机器人请求失败: statusCode={}, message={}",
                        response.code(), response.message());
                return null;
            }
        }
    }

    /**
     * 生成签名
     *
     * @param timestamp 时间戳
     * @param secret    密钥
     * @return 签名字符串
     */
    private String genSign(String timestamp, String secret) throws Exception {
        // 拼接timestamp和secret
        String stringToSign = timestamp + "\n" + secret;

        // 使用HmacSHA256算法计算签名
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                stringToSign.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        mac.init(secretKeySpec);
        byte[] hmacCode = mac.doFinal();

        // 对结果进行base64处理
        return Base64.getEncoder().encodeToString(hmacCode);
    }
}

