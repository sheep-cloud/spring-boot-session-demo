package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieHttpSessionStrategy;
import org.springframework.session.web.http.HttpSessionStrategy;

/**
 * @author: colg
 */
@Configuration
@EnableRedisHttpSession(redisNamespace = "demo", maxInactiveIntervalInSeconds = 60)
public class RedisSessionConfig {

    /*
        redisNamespace: 为键定义唯一的命名空间
               默认前缀：spring:session
               命名前缀：spring.session:<redisNamespace>
        maxInactiveIntervalInSeconds：redis 保存时间（秒），默认 1800秒，30分钟
     */

    /**
     * 将HTTP请求和响应映射到会话的策略。默认使用 cookie来传送session信息。
     *
     * @return
     */
    @Bean
    public HttpSessionStrategy httpsessionstrategy() {
        // 使用 header 来传送 session信息
//        return new HeaderHttpSessionStrategy();
        // 使用cookie来传送 session 信息
        return new CookieHttpSessionStrategy();
    }
}
