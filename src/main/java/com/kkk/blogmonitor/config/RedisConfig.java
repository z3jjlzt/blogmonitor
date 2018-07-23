package com.kkk.blogmonitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Created by z3jjlzt on 2018/7/23.
 */

@Configuration
public class RedisConfig {
    @Value("${spring.redis.database}")
    private int index;
    @Value("${spring.redis.host}")
    private String hostName;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.port}")
    private int port;
}
