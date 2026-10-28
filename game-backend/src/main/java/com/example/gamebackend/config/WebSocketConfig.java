package com.example.gamebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.example.gamebackend.handler.ChallengeWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChallengeWebSocketHandler challengeWebSocketHandler;

    public WebSocketConfig(ChallengeWebSocketHandler challengeWebSocketHandler) {
        this.challengeWebSocketHandler = challengeWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(challengeWebSocketHandler, "/ws/challenge")
                .setAllowedOrigins("*");
    }
}
