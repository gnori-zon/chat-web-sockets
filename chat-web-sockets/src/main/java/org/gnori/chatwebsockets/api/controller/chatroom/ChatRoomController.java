package org.gnori.chatwebsockets.api.controller.chatroom;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.BaseWebSocketController;
import org.gnori.chatwebsockets.api.controller.chatroom.payload.ChatRoomPayload;
import org.gnori.chatwebsockets.api.controller.chatroom.payload.CreateChatRoomPayload;
import org.gnori.chatwebsockets.api.controller.chatroom.payload.UpdateChatRoomPayload;
import org.gnori.chatwebsockets.api.dto.ChatRoomDto;
import org.gnori.chatwebsockets.core.service.domain.ChatRoomService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.gnori.chatwebsockets.api.constant.Endpoint.*;
import static org.gnori.chatwebsockets.core.service.security.util.SecurityUtil.convertFrom;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatRoomController extends BaseWebSocketController {

   ChatRoomService<CustomUserDetails> chatRoomService;

    public ChatRoomController(SimpMessagingTemplate simpMessagingTemplate, ChatRoomService<CustomUserDetails> chatRoomService) {
        super(simpMessagingTemplate);
        this.chatRoomService = chatRoomService;
    }

    @MessageMapping(CHAT_ROOMS + LIST_PATH)
    public void getForUser(
            SimpMessageHeaderAccessor headerAccessor
    ) {
        doIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final List<ChatRoomDto> chatRoomDtoAll = chatRoomService.getAll(user);

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_USER_CHAT_ROOMS, user.getUsername()),
                            chatRoomDtoAll
                    );
                }
        );
    }

    @MessageMapping(CHAT_ROOMS + ONE_PATH)
    public void get(
            @Payload ChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        doIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final ChatRoomDto chatRoomDto = chatRoomService.get(payload, user);

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_USER_CHAT_ROOMS, user.getUsername()),
                            chatRoomDto
                    );
                }
        );
    }

    @MessageMapping(CHAT_ROOMS + CREATE_PATH)
    public void create(
            @Payload CreateChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        doIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final ChatRoomDto createdChatRoomDto = chatRoomService.create(payload, user);

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_USER_CHAT_ROOMS, user.getUsername()),
                            createdChatRoomDto
                    );
                }
        );
    }

    @MessageMapping(CHAT_ROOMS + UPDATE_PATH)
    public void updateById(
            @Payload UpdateChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        doIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final ChatRoomDto chatRoomDto = chatRoomService.update(payload, user);

                    chatRoomDto.getConnectedUsers().forEach(
                            userDto ->
                                    simpMessagingTemplate.convertAndSend(
                                            String.format(TOPIC_USER_UPDATE_CHAT_ROOMS, userDto.getUsername()),
                                            chatRoomDto
                                    )
                    );
                }
        );
    }

    @MessageMapping(CHAT_ROOMS + DELETE_PATH)
    public void deleteById(
            @Payload ChatRoomPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        doIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final ChatRoomDto chatRoomDto = chatRoomService.delete(payload, user);
                    final ChatRoomDto emptyChatRoomDto = new ChatRoomDto(chatRoomDto.getId());

                    chatRoomDto.getConnectedUsers().forEach(
                            userDto ->
                                    simpMessagingTemplate.convertAndSend(
                                            String.format(TOPIC_USER_UPDATE_CHAT_ROOMS, userDto.getUsername()),
                                            emptyChatRoomDto
                                    )
                    );
                }
        );
    }
}
