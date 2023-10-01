package org.gnori.chatwebsockets.api.controller.user.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.BaseWebSocketController;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.CreateAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.DeleteAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.GetAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.UpdateAdminUserPayload;
import org.gnori.chatwebsockets.core.service.domain.UserService;
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
public class AdminUserController extends BaseWebSocketController {

    UserService<CustomUserDetails> userService;

    @MessageMapping(ADMIN_USERS + GET_PATH)
    public void get(
            @Payload GetAdminUserPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    userService.adminGet(payload, user);
                }
        );
    }

    @MessageMapping(ADMIN_USERS + CREATE_PATH)
    public void create(
            @Payload CreateAdminUserPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    userService.adminCreate(payload, user);
                }
        );
    }

    @MessageMapping(ADMIN_USERS + UPDATE_PATH)
    public void update(
            @Payload UpdateAdminUserPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    userService.adminUpdate(payload, user);
                }
        );
    }

    @MessageMapping(ADMIN_USERS + DELETE_PATH)
    public void delete(
            @Payload DeleteAdminUserPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    userService.adminDelete(payload, user);
                }
        );
    }
}
