package org.gnori.chatwebsockets.api.controller.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.user.payload.AdminUserPayload;
import org.gnori.chatwebsockets.core.domain.user.enums.Role;
import org.gnori.chatwebsockets.core.service.domain.impl.UserService;
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
public class AdminUserController {

    SimpMessagingTemplate simpMessagingTemplate;
    UserService userService;

    @MessageMapping(ADMIN_USERS + CREATE_PATH)
    public void create(
            @Payload AdminUserPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Optional.ofNullable(headerAccessor.getSessionAttributes()).ifPresent(
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    if (user.getUser().getRoles().contains(Role.ADMIN)) {
                        simpMessagingTemplate.convertAndSend(
                                String.format(TOPIC_ADMIN_USER, user.getUsername()),
                                userService.adminCreate(payload)
                        );
                    }
                }
        );
    }

    @MessageMapping(ADMIN_USERS + UPDATE_PATH)
    public void update(
            @Payload AdminUserPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Optional.ofNullable(headerAccessor.getSessionAttributes()).ifPresent(
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    if (user.getUser().getRoles().contains(Role.ADMIN)) {
                        simpMessagingTemplate.convertAndSend(
                                String.format(TOPIC_ADMIN_USER, user.getUsername()),
                                userService.adminUpdateById(payload)
                        );
                    }
                }
        );
    }

    @MessageMapping(ADMIN_USERS + DELETE_PATH)
    public void delete(
            @Payload Long id,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Optional.ofNullable(headerAccessor.getSessionAttributes()).ifPresent(
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    if (user.getUser().getRoles().contains(Role.ADMIN)) {
                        userService.deleteById(id, user);
                    }
                }
        );
    }
}
