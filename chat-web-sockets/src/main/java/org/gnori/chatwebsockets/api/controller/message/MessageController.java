package org.gnori.chatwebsockets.api.controller.message;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.BaseWebSocketController;
import org.gnori.chatwebsockets.api.controller.message.payload.CreateMessagePayload;
import org.gnori.chatwebsockets.api.controller.message.payload.MessagePayload;
import org.gnori.chatwebsockets.api.controller.message.payload.UpdateMessagePayload;
import org.gnori.chatwebsockets.core.service.domain.MessageService;
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
public class MessageController extends BaseWebSocketController {

    MessageService<CustomUserDetails> messageService;

    @MessageMapping(OLD_MESSAGES)
    public void getOldMessages(
            @Payload MessagePayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {

                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    messageService.getAll(payload, user);
                }
        );
    }

    @MessageMapping(MESSAGES + SEND_PATH)
    public void sendMessage(
            @Payload CreateMessagePayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {

                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    messageService.create(payload, user);
                }
        );
    }

    @MessageMapping(MESSAGES + UPDATE_PATH)
    public void updateMessage(
            @Payload UpdateMessagePayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {

                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    messageService.update(payload, user);
                }
        );
    }

    @MessageMapping(MESSAGES + DELETE_PATH)
    public void deleteMessage(
            @Payload MessagePayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {

                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    messageService.delete(payload, user);
                }
        );
    }
}
