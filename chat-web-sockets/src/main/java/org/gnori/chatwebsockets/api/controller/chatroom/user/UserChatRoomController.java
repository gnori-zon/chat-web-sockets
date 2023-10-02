package org.gnori.chatwebsockets.api.controller.chatroom.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.BaseWebSocketController;
import org.gnori.chatwebsockets.core.service.domain.ChatRoomService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

import static org.gnori.chatwebsockets.api.constant.Endpoint.*;
import static org.gnori.chatwebsockets.core.service.security.util.SecurityUtil.convertFrom;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserChatRoomController extends BaseWebSocketController {

    ChatRoomService<CustomUserDetails> chatRoomService;

    @MessageMapping(CHAT_ROOMS + USERS + ADD_PATH)
    public void addUser(
            @Payload UserChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {

                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    chatRoomService.addUser(payload, user);
                }
        );
    }

    @MessageMapping(CHAT_ROOMS + USERS + DELETE_PATH)
    public void deleteUser(
            @Payload UserChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {

                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    chatRoomService.deleteUser(payload, user);
                }
        );
    }
}
