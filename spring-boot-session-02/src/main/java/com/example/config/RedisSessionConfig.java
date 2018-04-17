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
        redisNamespace：必须相同，以便于从redis取得session
     */

    @Bean
    public HttpSessionStrategy httpsessionstrategy() {
        return new CookieHttpSessionStrategy();
    }

}
