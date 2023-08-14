package org.gnori.chatwebsockets.api.controller;

import lombok.RequiredArgsConstructor;
import org.gnori.chatwebsockets.core.domain.message.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    @MessageMapping("/chat/message:send")
    @SendTo("/topic/public")
    public Message sendMessage(@Payload Message message) {
        return message;
    }

    @MessageMapping("/chat/user:add")
    @SendTo("/topic/public")
    public Message addUser (
            @Payload Message message) {
        return message;
    }
}
