package org.gnori.chatwebsockets.api.controller.chatRoom;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.dto.ChatRoomDto;
import org.gnori.chatwebsockets.core.service.domain.impl.ChatRoomService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.gnori.chatwebsockets.api.constant.Endpoint.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatRoomController {

    SimpMessagingTemplate simpMessagingTemplate;
    ChatRoomService chatRoomService;

    @MessageMapping(CHAT_ROOMS + LIST_PATH)
    public void getForUser(
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Optional.ofNullable(headerAccessor.getSessionAttributes()).ifPresent(
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_USER_CHAT_ROOMS, user.getUsername()),
                            chatRoomService.getAll(user)
                    );
                });
    }

    @MessageMapping(CHAT_ROOMS + ONE_PATH)
    public void getChatById(
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Optional.ofNullable(headerAccessor.getSessionAttributes()).ifPresent(
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final String targetId = (String) sessionAttrs.get(TARGET_ID);
                    final ChatRoomDto chatRoomDto = chatRoomService.getById(targetId, user);

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_USER_CHAT_ROOMS, user.getUsername()),
                            chatRoomDto
                    );
                });
    }

    @MessageMapping(CHAT_ROOMS + CREATE_PATH)
    public void create(
            @Payload ChatRoomDto chatRoomDto,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Optional.ofNullable(headerAccessor.getSessionAttributes()).ifPresent(
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final ChatRoomDto createdChatRoomDto = chatRoomService.create(chatRoomDto, user);

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_USER_CHAT_ROOMS, user.getUsername()),
                            createdChatRoomDto
                    );
                });
    }

    @MessageMapping(CHAT_ROOMS + UPDATE_PATH)
    @SendTo("/topic/chat-rooms")
    public void updateById(
            @Payload ChatRoomDto chatRoomDto,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        final Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
        if (sessionAttrs != null) {
            final CustomUserDetails user = convertFrom(headerAccessor.getUser());
            final String targetId = (String) sessionAttrs.get(TARGET_ID);

            simpMessagingTemplate.convertAndSend(
                    String.format(TOPIC_USER_CHAT_ROOMS, user.getUsername()),
                    chatRoomService.updateById(targetId, chatRoomDto, user)
            );
        }
    }

    @MessageMapping(CHAT_ROOMS + DELETE_PATH)
    public void deleteById(
            SimpMessageHeaderAccessor headerAccessor
    ) {
        final Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
        if (sessionAttrs != null) {
            final CustomUserDetails user = convertFrom(headerAccessor.getUser());
            final String targetId = (String) sessionAttrs.get(TARGET_ID);
            chatRoomService.deleteById(targetId, user);
        }
    }

    private CustomUserDetails convertFrom(Principal user) {
        return Objects.requireNonNull(
                (CustomUserDetails)((UsernamePasswordAuthenticationToken) user).getPrincipal()
        );
    }


}
