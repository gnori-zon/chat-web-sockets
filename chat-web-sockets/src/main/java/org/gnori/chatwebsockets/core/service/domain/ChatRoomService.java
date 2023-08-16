package org.gnori.chatwebsockets.core.service.domain;

import org.gnori.chatwebsockets.api.dto.ChatRoomDto;
import org.gnori.chatwebsockets.core.exception.impl.ForbiddenException;
import org.gnori.chatwebsockets.core.exception.impl.NotFoundException;

import java.util.List;

public interface ChatRoomService<A> {

    List<ChatRoomDto> getAll(A user);
    ChatRoomDto get(ChatRoomDto dto, A user) throws ForbiddenException, NotFoundException;
    ChatRoomDto delete(ChatRoomDto dto, A user) throws ForbiddenException;
    ChatRoomDto create(ChatRoomDto dto, A user);
    ChatRoomDto update(ChatRoomDto dto, A user) throws ForbiddenException;
    ChatRoomDto deleteUser(String chatRoomId, String username, A user) throws ForbiddenException, NotFoundException;
    ChatRoomDto addUser(String chatRoomId, String username, A user) throws ForbiddenException, NotFoundException;

}
