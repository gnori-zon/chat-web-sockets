package org.gnori.chatwebsockets.api.controller.user.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.BaseWebSocketController;
import org.gnori.chatwebsockets.api.controller.user.payload.UserPayload;
import org.gnori.chatwebsockets.api.controller.user.user.payload.ChangePasswordUserPayload;
import org.gnori.chatwebsockets.api.controller.user.user.payload.CreateUserPayload;
import org.gnori.chatwebsockets.api.dto.UserDto;
import org.gnori.chatwebsockets.core.service.domain.UserService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.gnori.chatwebsockets.api.constant.Endpoint.*;
import static org.gnori.chatwebsockets.core.service.security.util.SecurityUtil.convertFrom;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController extends BaseWebSocketController {

    UserService<CustomUserDetails> userService;

    public UserController(SimpMessagingTemplate simpMessagingTemplate, UserService<CustomUserDetails> userService) {
        super(simpMessagingTemplate);
        this.userService = userService;
    }

    @PostMapping(USERS + SIGN_UP_PATH)
    public UserDto create(
            @RequestBody CreateUserPayload bodyPayload
    ) {
        return userService.create(bodyPayload);
    }


    @MessageMapping(USERS + DELETE_PATH)
    public void delete(
            SimpMessageHeaderAccessor headerAccessor
    ) {
        doIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    userService.delete(user);
                }
        );
    }

    @MessageMapping(USERS + UPDATE_PATH)
    public void update(
            @Payload UserPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        doIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final UserDto updatedUserDto = userService.update(payload, user);

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_USER, user.getUsername()),
                            updatedUserDto
                    );
                }
        );
    }

    @MessageMapping(USERS + CHANGE_PASS_PATH)
    public void changePassword(
            @Payload ChangePasswordUserPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        doIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final UserDto userDtoWithUpdatedPassword = userService.changePassword(payload, user);

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_USER, user.getUsername()),
                            userDtoWithUpdatedPassword
                    );
                }
        );
    }

    @MessageMapping(USERS_SELF)
    public void get(
            SimpMessageHeaderAccessor headerAccessor
    ) {
        doIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    final UserDto userDto = userService.get(user);

                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_USER, user.getUsername()),
                            userDto
                    );
                }
        );
    }

}
