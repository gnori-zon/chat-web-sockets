package org.gnori.chatwebsockets.api.controller;

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@AllArgsConstructor
public abstract class BaseWebSocketController {

    protected void executeIfSessionAttrsIsPresent(SimpMessageHeaderAccessor headerAccessor, Consumer<Map<String, Object>> consumer) {
        Optional.ofNullable(headerAccessor.getSessionAttributes())
                .ifPresent(consumer);
    }
}
