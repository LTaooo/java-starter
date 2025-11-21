package com.lt.springstarter.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 验证器工具类
 * 提供静态方法进行数据验证，验证不通过自动抛出异常
 */
public class Validator {

    private static final jakarta.validation.Validator validator;

    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    /**
     * 验证对象
     *
     * @param object 待验证的对象
     * @param <T>    对象类型
     * @throws IllegalArgumentException 验证不通过时抛出，包含所有验证失败的字段和错误信息
     */
    public static <T> void check(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException("参数验证失败: " + message);
        }
    }

    public static <T> boolean isValid(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        return violations.isEmpty();
    }
}

