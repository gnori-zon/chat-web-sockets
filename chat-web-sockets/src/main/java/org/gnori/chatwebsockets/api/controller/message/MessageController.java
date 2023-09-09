package org.gnori.chatwebsockets.api.controller.message;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.BaseWebSocketController;
import org.gnori.chatwebsockets.api.controller.message.payload.CreateMessagePayload;
import org.gnori.chatwebsockets.api.controller.message.payload.MessagePayload;
import org.gnori.chatwebsockets.api.controller.message.payload.UpdateMessagePayload;
import org.gnori.chatwebsockets.api.dto.MessageDto;
import org.gnori.chatwebsockets.core.service.domain.MessageService;
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
public class MessageController extends BaseWebSocketController {

    MessageService<CustomUserDetails> messageService;

    public MessageController(SimpMessagingTemplate simpMessagingTemplate, MessageService<CustomUserDetails> messageService) {
        super(simpMessagingTemplate);
        this.messageService = messageService;
    }

    @MessageMapping(OLD_MESSAGES)
    public void getOldMessages(
            @Payload MessagePayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        doIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final String chatRoomId = payload.getChatRoomId();
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final List<MessageDto> oldMessages = messageService.getAll(payload, user);

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_CHAT_ROOM_OLD_MESSAGES, chatRoomId),
                            oldMessages
                    );
                }
        );
    }

    @MessageMapping(MESSAGES + SEND_PATH)
    public void sendMessage(
            @Payload CreateMessagePayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        doIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final String chatRoomId = payload.getChatRoomId();
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final MessageDto createdMessageDto = messageService.create(payload, user);

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_CHAT_ROOM_MESSAGES, chatRoomId),
                            createdMessageDto
                    );
                }
        );
    }

    @MessageMapping(MESSAGES + UPDATE_PATH)
    public void updateMessage(
            @Payload UpdateMessagePayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        doIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final String chatRoomId = payload.getChatRoomId();
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final MessageDto updatedMessageDto = messageService.update(payload, user);

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_CHAT_ROOM_OLD_MESSAGES, chatRoomId),
                            updatedMessageDto
                    );
                }
        );
    }

    @MessageMapping(MESSAGES + DELETE_PATH)
    public void deleteMessage(
            @Payload MessagePayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        doIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final String chatRoomId = payload.getChatRoomId();
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final MessageDto deletedMessageDto = messageService.delete(payload, user);

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_CHAT_ROOM_UPDATE_MESSAGES, chatRoomId),
                            deletedMessageDto
                    );
                }
        );
    }
}
