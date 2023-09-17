package org.gnori.chatwebsockets.api.controller.user.admin;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.BaseWebSocketController;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.CreateAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.UpdateAdminUserPayload;
import org.gnori.chatwebsockets.api.dto.UserDto;
import org.gnori.chatwebsockets.core.domain.user.enums.Role;
import org.gnori.chatwebsockets.core.service.domain.UserService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.RestController;

import static org.gnori.chatwebsockets.api.constant.Endpoint.*;
import static org.gnori.chatwebsockets.core.service.security.util.SecurityUtil.convertFrom;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserController extends BaseWebSocketController {

    private static final SimpleGrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority(Role.ADMIN.name());

    UserService<CustomUserDetails> userService;

    public AdminUserController(SimpMessagingTemplate simpMessagingTemplate, UserService<CustomUserDetails> userService) {
        super(simpMessagingTemplate);
        this.userService = userService;
    }

    @MessageMapping(ADMIN_USERS + CREATE_PATH)
    public void create(
            @Payload CreateAdminUserPayload payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());

                    doIfAdmin(
                            user,
                            () -> {
                                final UserDto createUser = userService.adminCreate(payload);

                                simpMessagingTemplate.convertAndSend(
                                        String.format(TOPIC_ADMIN_USER, user.getUsername()),
                                        createUser
                                );
                            }
                    );
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
                    doIfAdmin(
                            user,
                            () -> {
                                final UserDto updatedUserDto = userService.adminUpdateByUsername(payload);

                                simpMessagingTemplate.convertAndSend(
                                        String.format(TOPIC_ADMIN_USER, user.getUsername()),
                                        updatedUserDto
                                );
                            }
                    );
                }
        );
    }

    @MessageMapping(ADMIN_USERS + DELETE_PATH)
    public void delete(
            @Payload String username,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        executeIfSessionAttrsIsPresent(headerAccessor,
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    doIfAdmin(
                            user,
                            () -> userService.adminDelete(username)
                    );
                }
        );
    }

    private void doIfAdmin(CustomUserDetails userDetails, Runnable runnable) {
        if (userDetails.getAuthorities().contains(ADMIN_AUTHORITY)) {
            runnable.run();
        }
    }
}
