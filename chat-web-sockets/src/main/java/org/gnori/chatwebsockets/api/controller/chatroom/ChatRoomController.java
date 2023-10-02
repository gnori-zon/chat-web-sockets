package org.gnori.chatwebsockets.api.controller.chatroom;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.BaseWebSocketController;
import org.gnori.chatwebsockets.api.controller.chatroom.payload.ChatRoomPayload;
import org.gnori.chatwebsockets.api.controller.chatroom.payload.CreateChatRoomPayload;
import org.gnori.chatwebsockets.api.controller.chatroom.payload.UpdateChatRoomPayload;
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
public class ChatRoomController extends BaseWebSocketController {

    ChatRoomService<CustomUserDetails> chatRoomService;

    @MessageMapping(CHAT_ROOMS + LIST_PATH)
    public void getAll(
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {

                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    chatRoomService.getAll(user);
                }
        );
    }

    @MessageMapping(CHAT_ROOMS + ONE_PATH)
    public void get(
            @Payload ChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {

                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    chatRoomService.get(payload, user);
                }
        );
    }

    @MessageMapping(CHAT_ROOMS + CREATE_PATH)
    public void create(
            @Payload CreateChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {

                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    chatRoomService.create(payload, user);
                }
        );
    }

    @MessageMapping(CHAT_ROOMS + UPDATE_PATH)
    public void updateById(
            @Payload UpdateChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {

                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    chatRoomService.update(payload, user);
                }
        );
    }

    @MessageMapping(CHAT_ROOMS + DELETE_PATH)
    public void deleteById(
            @Payload ChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {

                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    chatRoomService.delete(payload, user);
                }
        );
    }
}
