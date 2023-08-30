package org.gnori.chatwebsockets.api.controller.chatroom.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.dto.ChatRoomDto;
import org.gnori.chatwebsockets.core.service.domain.ChatRoomService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.gnori.chatwebsockets.api.constant.Endpoint.*;
import static org.gnori.chatwebsockets.core.service.security.util.SecurityUtil.convertFrom;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserChatRoomController {

    SimpMessagingTemplate simpMessagingTemplate;
    ChatRoomService<CustomUserDetails> chatRoomService;

    @MessageMapping(CHAT_ROOMS + USERS + ADD_PATH)
    public void addUser(
            @Payload UserChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        final Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
        if (sessionAttrs != null) {
            final CustomUserDetails user = convertFrom(headerAccessor.getUser());
            final ChatRoomDto chatRoomDto = chatRoomService.addUser(payload, user);
            simpMessagingTemplate.convertAndSend(
                    String.format(TOPIC_USER_CHAT_ROOMS, user.getUsername()),
                    chatRoomDto
            );
            simpMessagingTemplate.convertAndSend(
                    String.format(TOPIC_USER_CHAT_ROOMS, payload.getUsername()),
                    chatRoomDto
            );
        }
    }

    @MessageMapping(CHAT_ROOMS + USERS + DELETE_PATH)
    public void deleteUser(
            @Payload UserChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        final Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
        if (sessionAttrs != null) {
            final CustomUserDetails user = convertFrom(headerAccessor.getUser());
            simpMessagingTemplate.convertAndSend(
                    String.format(TOPIC_USER_CHAT_ROOMS, user.getUsername()),
                    chatRoomService.deleteUser(payload, user)
            );
        }
    }
}
