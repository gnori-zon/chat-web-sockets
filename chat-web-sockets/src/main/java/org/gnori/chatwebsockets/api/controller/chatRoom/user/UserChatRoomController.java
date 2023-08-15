package org.gnori.chatwebsockets.api.controller.chatroom.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.core.service.domain.impl.ChatRoomService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.gnori.chatwebsockets.api.constant.Endpoint.*;
import static org.gnori.chatwebsockets.core.service.security.util.SecurityUtil.convertFrom;

@RestController
@RequestMapping(CHAT_ROOMS)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserChatRoomController {

    SimpMessagingTemplate simpMessagingTemplate;
    ChatRoomService chatRoomService;

    @MessageMapping(USERS + ADD_PATH)
    public void addUser(
            @Payload UserChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        final Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
        if (sessionAttrs != null) {
            final CustomUserDetails user = convertFrom(headerAccessor.getUser());
            simpMessagingTemplate.convertAndSend(
                    String.format(TOPIC_USER_CHAT_ROOMS, user.getUsername()),
                    chatRoomService.addUser(payload.getTargetId(), payload.getUsername(), user)
            );
        }
    }

    @SubscribeMapping(USERS + DELETE_PATH)
    public void deleteUser(
            @Payload UserChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        final Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
        if (sessionAttrs != null) {
            final CustomUserDetails user = convertFrom(headerAccessor.getUser());
            simpMessagingTemplate.convertAndSend(
                    String.format(TOPIC_USER_CHAT_ROOMS, user.getUsername()),
                    chatRoomService.deleteUser(payload.getTargetId(), payload.getUsername(), user)
            );
        }
    }
}
