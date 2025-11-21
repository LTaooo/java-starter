package com.lt.springstarter.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean Validation 配置
 * 提供 Validator 实例用于数据验证
 */
@Configuration
public class ValidatorConfig {
    
    /**
     * 创建 Validator Bean
     * 
     * @return Validator 实例
     */
    @Bean
    public Validator validator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator();
        }
    }
}

