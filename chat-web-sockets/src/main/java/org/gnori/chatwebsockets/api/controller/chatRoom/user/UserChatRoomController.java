package org.gnori.chatwebsockets.api.controller.chatRoom.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.dto.UserDto;
import org.gnori.chatwebsockets.core.service.domain.impl.ChatRoomService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.gnori.chatwebsockets.api.constant.Endpoint.*;

@RestController
@RequestMapping(CHAT_ROOM_WITH_ID)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserChatRoomController {

    ChatRoomService chatRoomService;

    @PostMapping(USERS + ADD_PATH)
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> addUser(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String targetId,
            @RequestParam String username
    ) {
        return chatRoomService.addUser(targetId, username, user);
    }

    @DeleteMapping(USERS + DELETE_PATH)
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> deleteUser(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String targetId,
            @RequestParam String username
    ) {
        return chatRoomService.deleteUser(targetId, username, user);
    }
}
