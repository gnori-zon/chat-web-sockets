package org.gnori.chatwebsockets.api.controller.chatRoom;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.dto.ChatRoomDto;
import org.gnori.chatwebsockets.core.service.domain.impl.ChatRoomService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.gnori.chatwebsockets.api.constant.Endpoint.CHAT_ROOMS;
import static org.gnori.chatwebsockets.api.constant.Endpoint.TARGET_ID;

@RestController
@RequestMapping(CHAT_ROOMS)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatRoomController {

    ChatRoomService chatRoomService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ChatRoomDto> getForUser(@AuthenticationPrincipal CustomUserDetails user) {
        return chatRoomService.getAll(user);
    }

    @GetMapping(TARGET_ID)
    @ResponseStatus(HttpStatus.OK)
    public ChatRoomDto getChatById(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String targetId
    ) {
        return chatRoomService.getById(targetId, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatRoomDto create(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody ChatRoomDto chatRoomDto
    ) {
        return chatRoomService.create(chatRoomDto, user);
    }

    @PutMapping(TARGET_ID)
    @ResponseStatus(HttpStatus.OK)
    public ChatRoomDto updateById(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String targetId,
            @RequestBody ChatRoomDto chatRoomDto
    ) {
        return chatRoomService.updateById(targetId, chatRoomDto, user);
    }

    @DeleteMapping(TARGET_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String targetId
    ) {
        chatRoomService.deleteById(targetId, user);
    }


}
