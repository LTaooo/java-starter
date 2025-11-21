package com.lt.springstarter.util;

import com.alibaba.nacos.shaded.com.google.gson.Gson;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.Map;

public class HttpUtil {
    public static Request.Builder newJsonRequest(String url) {
        return new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json");
    }

    public static Request.Builder postJson(String url, Map<String, Object> body) {
        return newJsonRequest(url)
                .post(RequestBody.create(new Gson().toJson(body).getBytes()));
    }

    public static Request.Builder get(String url, Map<String, String> params) {
        HttpUrl baseUrl = HttpUrl.parse(url);
        if (baseUrl == null) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }

        HttpUrl.Builder urlBuilder = baseUrl.newBuilder();
        if (params != null && !params.isEmpty()) {
            params.forEach(urlBuilder::addQueryParameter);
        }

        return new Request.Builder()
                .url(urlBuilder.build())
                .get();
    }
}