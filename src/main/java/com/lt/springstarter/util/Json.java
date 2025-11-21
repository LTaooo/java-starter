package com.lt.springstarter.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class Json {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 解析 json 字符串为 Map<String, Object>
     *
     * @param json json字符串
     * @return Map<String, Object>
     */
    public static Map<String, Object> toMap(String json) {
        try {
            return mapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 解析失败: " + json, e);
        }
    }

    /**
     * 对象转为 JSON 字符串
     *
     * @param object 任意对象
     * @return JSON 字符串
     */
    public static String toJsonString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转 JSON 失败: " + object, e);
        }
    }

    /**
     * JSON 字符串转为对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @return 对象实例
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 转对象失败: " + json, e);
        }
    }

    /**
     * 将对象转换为 Map<String, Object>
     *
     * @param object 任意对象
     * @return Map<String, Object>
     */
    public static Map<String, Object> objectToMap(Object object) {
        return toMap(toJsonString(object));
    }
}
