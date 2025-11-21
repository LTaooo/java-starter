package com.lt.springstarter.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class OKHttpConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // 连接超时
                .readTimeout(480, TimeUnit.SECONDS)    // 读超时
                .writeTimeout(480, TimeUnit.SECONDS)   // 写超时
                .connectionPool(new ConnectionPool(20, 5, TimeUnit.MINUTES)) // 连接池
                .retryOnConnectionFailure(true)       // 自动重试
                .build();
    }
}