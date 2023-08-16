package org.gnori.chatwebsockets.api.controller.message;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.message.payload.MessagePayload;
import org.gnori.chatwebsockets.api.controller.message.payload.NewMessagePayload;
import org.gnori.chatwebsockets.api.dto.MessageDto;
import org.gnori.chatwebsockets.core.domain.message.MessagePrimaryKey;
import org.gnori.chatwebsockets.core.service.domain.MessageService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static org.gnori.chatwebsockets.api.constant.Endpoint.*;
import static org.gnori.chatwebsockets.core.service.security.util.SecurityUtil.convertFrom;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageController {

    MessageService<CustomUserDetails> messageService;
    SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping(OLD_MESSAGES)
    public void getOldMessages(
            @Payload MessagePayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Optional.ofNullable(headerAccessor.getSessionAttributes()).ifPresent(
                sessionAttrs -> {
                    final String chatRoomId = payload.getChatRoomId();
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_CHAT_ROOM_OLD_MESSAGES, chatRoomId),
                            messageService.getAll(
                                    new MessageDto(
                                            new MessagePrimaryKey(user.getUsername(), chatRoomId, payload.getDate()),
                                            user.getUsername(),
                                            null, null),
                                    user
                            )
                    );
                }
        );
    }

    @MessageMapping(MESSAGES + SEND_PATH)
    public void sendMessage(
            @Payload NewMessagePayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Optional.ofNullable(headerAccessor.getSessionAttributes()).ifPresent(
                sessionAttrs -> {
                    final String chatRoomId = payload.getChatRoomId();
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_CHAT_ROOM_MESSAGES, chatRoomId),
                            messageService.create(
                                    new MessageDto(
                                            new MessagePrimaryKey(user.getUsername(), chatRoomId, payload.getDate()),
                                            user.getUsername(),
                                            null, payload.getText()),
                                    user
                            )
                    );
                }
        );
    }

    @MessageMapping(MESSAGES + UPDATE_PATH)
    public void updateMessage(
            @Payload NewMessagePayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Optional.ofNullable(headerAccessor.getSessionAttributes()).ifPresent(
                sessionAttrs -> {
                    final String chatRoomId = payload.getChatRoomId();
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_CHAT_ROOM_OLD_MESSAGES, chatRoomId),
                            messageService.update(
                                    new MessageDto(
                                            new MessagePrimaryKey(user.getUsername(), chatRoomId, payload.getDate()),
                                            user.getUsername(),
                                            null, payload.getText()),
                                    user
                            )
                    );
                }
        );
    }

    @MessageMapping(MESSAGES + DELETE_PATH)
    public void deleteMessage(
            @Payload MessagePayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Optional.ofNullable(headerAccessor.getSessionAttributes()).ifPresent(
                sessionAttrs -> {
                    final String chatRoomId = payload.getChatRoomId();
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_CHAT_ROOM_UPDATE_MESSAGES, chatRoomId),
                            messageService.delete(
                                    new MessageDto(
                                            new MessagePrimaryKey(user.getUsername(), chatRoomId, payload.getDate()),
                                            user.getUsername(),
                                            null, null),
                                    user
                            )
                    );
                }
        );
    }
}
