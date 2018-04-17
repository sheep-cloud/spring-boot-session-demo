# Spring Boot Session Demo
Spring Session提供了一套用于管理session信息的api和实现。
Spring Session管理session，使得以下的功能更加容易实现：

- 编写可水平扩展的原生云应用。
- 将session所保存的状态卸载到特定的外部session存储中，如Redis或JDBC中，它们能够以独立应用服务器的方式提供高质量的集群。
- 当用户使用WebSocket发送请求的时候，能够保持HttpSession处于活跃状态。
- 在非Web请求的处理代码中，能够访问Session数据，比如在JMS消息的处理代码中。
- 支持每个浏览器上使用多个session，聪儿能够很容易的构建更加丰富的终端用户体验。
- 控制session id如何在客户端和服务器之间进行交换，这样的话就能很容易的编写Restful API，因为它可以从HTTP头信息中获取session id，而不必再依赖于cookie。

# Spring Boot 集成Spring session 并存入redis
## 1、spring-boot-session-01
###1、pom.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>spring-boot-session-01</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>spring-boot-session-01</name>
    <description>Demo project for Spring Boot</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.5.10.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- Spring Boot session 依赖 -->
        <dependency>
            <groupId>org.springframework.session</groupId>
            <artifactId>spring-session-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.47</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```
###2、application.properties
```
server.port=8777
# 使用spring session，设置path会导致session丢失，目前未解决。
# 可使用nginx反向代理，http://localhost/iplatform.privilege/session1
#server.context-path=/iplatform.privilege

spring.redis.host=192.168.21.103
spring.redis.port=6379
spring.session.store-type=redis
```
###3、注入RedisSessionConfig
```
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
```
###4、contoller
```
package com.example.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.example.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author colg
 */
@RestController
public class SessionController {

    /**
     * 设置session信息
     * @param request
     * @return
     */
    @GetMapping("/session1")
    public Map<String, Object> map(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>(16);
        User user = new User("Jack", "123456", new Date());

        HttpSession session = request.getSession();
        String sessionId = session.getId();
        StringBuffer requestURL = request.getRequestURL();
        session.setAttribute("sessionId", sessionId);
        session.setAttribute("requestURL", requestURL);
        String loginUser = JSON.toJSONString(user, SerializerFeature.WriteDateUseDateFormat);
        session.setAttribute("loginUser", loginUser);

        map.put("sessionId", sessionId);
        map.put("requestURL", requestURL);
        map.put("loginUser", loginUser);
        return map;
    }

}

```
###5、配置nginx
```
        listen       18080;
        server_name  localhost;
        
		#chigo-service-ac
		location ^~ /chigo-service-ac/ {
			proxy_pass http://localhost:8767/;
			proxy_redirect  off;
		    proxy_set_header  X-Real-IP $remote_addr;
		    proxy_set_header  X-Forwarded-For $proxy_add_x_forwarded_for;
		}

		#iplatform.privilege
		location ^~ /iplatform.privilege/ {
			proxy_pass http://localhost:8777/;
			proxy_redirect  off;
		    proxy_set_header  X-Real-IP $remote_addr;
		    proxy_set_header  X-Forwarded-For $proxy_add_x_forwarded_for;
		}
```
###6、展示
![](http://ww1.sinaimg.cn/large/005PjuVtgy1fqfq5d5cxxj31b20ajwg0.jpg)


##2、spring-boot-session-02
###1、application.properties
```
server.port=8767
#server.context-path=/chigo-service-ac

spring.redis.host=192.168.21.103
spring.redis.port=6379
spring.session.store-type=redis
```
###2、注入RedisSessionConfig
```
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
```
###3、controller
```
package com.example.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author colg
 */
@RestController
public class SessionController {


    /**
     * 获取session内容
     * 
     * @param request
     * @return
     */
    @GetMapping("/session2")
    public Map<String, Object> map2(HttpServletRequest request) {
        HttpSession session = request.getSession();

        Map<String, Object> map = new HashMap<>(2);
        map.put("sessionId", session.getAttribute("sessionId"));
        map.put("requestURL", session.getAttribute("requestURL"));
        String loginUser = (String) session.getAttribute("loginUser");
//        map.put("loginUser", JSON.toJSONString(loginUser));
        map.put("loginUser", loginUser);
        return map;
    }

}
```
###4、展示
![](http://ww1.sinaimg.cn/large/005PjuVtgy1fqfqars9uvj31b40aiabk.jpg)

##3、总结
- spring session使用redis存储session信息，默认使用cookie来传递，应用之间命名空间需要隔离
- 设置**server.context-path**以后，session将会丢失，有知道怎么解决的小伙伴还请告知一下，谢谢。