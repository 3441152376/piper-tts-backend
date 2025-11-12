package com.example.tts.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 注册配置属性类。
 */
@Configuration
@EnableConfigurationProperties({PiperProperties.class})
public class PropertiesConfig {
}


