package org.gnori.chatwebsockets.api.controller.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.user.payload.UserPayload;
import org.gnori.chatwebsockets.api.dto.UserDto;
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
public class UserController {

    SimpMessagingTemplate simpMessagingTemplate;
    UserService userService;

    @MessageMapping(USERS + CREATE_PATH)
    public void create(
            @Payload UserPayload payload
    ) {
        simpMessagingTemplate.convertAndSend(
                String.format(TOPIC_USER, payload.getUsername()),
                userService.create(payload)
        );
    }

    @MessageMapping(USERS + UPDATE_PATH)
    public void update(
            @Payload UserPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Optional.ofNullable(headerAccessor.getSessionAttributes()).ifPresent(
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_USER, user.getUsername()),
                            userService.updateById(
                                    user.getUser().getId(),
                                    new UserDto(null, payload.getUsername(), payload.getName(), payload.getEmail()),
                                    user
                            )
                    );
                }
        );
    }

    @MessageMapping(USERS + CHANGE_PASS_PATH)
    public void changePassword(
            @Payload UserPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Optional.ofNullable(headerAccessor.getSessionAttributes()).ifPresent(
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_USER, user.getUsername()),
                            userService.changePassword(
                                    payload,
                                    user
                            )
                    );
                }
        );
    }

    @MessageMapping(USERS + DELETE_PATH)
    public void delete(
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Optional.ofNullable(headerAccessor.getSessionAttributes()).ifPresent(
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    userService.deleteById(user.getUser().getId(), user);
                }
        );
    }
}
