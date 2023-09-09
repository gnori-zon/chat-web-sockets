package org.gnori.chatwebsockets.api.controller.chatroom.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.BaseWebSocketController;
import org.gnori.chatwebsockets.api.dto.ChatRoomDto;
import org.gnori.chatwebsockets.core.service.domain.ChatRoomService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import static org.gnori.chatwebsockets.api.constant.Endpoint.*;
import static org.gnori.chatwebsockets.core.service.security.util.SecurityUtil.convertFrom;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserChatRoomController extends BaseWebSocketController {

    ChatRoomService<CustomUserDetails> chatRoomService;

    public UserChatRoomController(SimpMessagingTemplate simpMessagingTemplate, ChatRoomService<CustomUserDetails> chatRoomService) {
        super(simpMessagingTemplate);
        this.chatRoomService = chatRoomService;
    }

    @MessageMapping(CHAT_ROOMS + USERS + ADD_PATH)
    public void addUser(
            @Payload UserChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        doIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final ChatRoomDto chatRoomDto = chatRoomService.addUser(payload, user);

                    chatRoomDto.getConnectedUsers().forEach(
                            userDto -> {
                                simpMessagingTemplate.convertAndSend(
                                        String.format(TOPIC_USER_CHAT_ROOMS, userDto.getUsername()),
                                        chatRoomDto
                                );
                            }
                    );
                }
        );
    }

    @MessageMapping(CHAT_ROOMS + USERS + DELETE_PATH)
    public void deleteUser(
            @Payload UserChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        doIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final ChatRoomDto chatRoomDto = chatRoomService.deleteUser(payload, user);
                    final ChatRoomDto emptyChatRoomDto = new ChatRoomDto(chatRoomDto.getId());

                    chatRoomDto.getConnectedUsers().forEach(
                            userDto -> {
                                simpMessagingTemplate.convertAndSend(
                                        String.format(TOPIC_USER_CHAT_ROOMS, userDto.getUsername()),
                                        chatRoomDto
                                );
                            }
                    );

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_USER_UPDATE_CHAT_ROOMS, payload.getUsername()),
                            emptyChatRoomDto
                    );
                }
        );
    }
}
