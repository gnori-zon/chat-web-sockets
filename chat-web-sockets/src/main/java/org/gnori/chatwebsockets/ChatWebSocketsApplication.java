package org.gnori.chatwebsockets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableRedisHttpSession
@EnableAspectJAutoProxy
public class ChatWebSocketsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatWebSocketsApplication.class, args);
    }

}
