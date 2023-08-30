package org.gnori.chatwebsockets.core.service.domain;

import org.gnori.chatwebsockets.api.controller.chatroom.payload.ChatRoomPayload;
import org.gnori.chatwebsockets.api.controller.chatroom.payload.CreateChatRoomPayload;
import org.gnori.chatwebsockets.api.controller.chatroom.payload.UpdateChatRoomPayload;
import org.gnori.chatwebsockets.api.controller.chatroom.user.UserChatRoomPayload;
import org.gnori.chatwebsockets.api.dto.ChatRoomDto;
import org.gnori.chatwebsockets.core.exception.impl.ForbiddenException;
import org.gnori.chatwebsockets.core.exception.impl.NotFoundException;

import java.util.List;

public interface ChatRoomService<A> {

    List<ChatRoomDto> getAll(A user);
    ChatRoomDto get(ChatRoomPayload payload, A user) throws ForbiddenException, NotFoundException;
    ChatRoomDto delete(ChatRoomPayload payload, A user) throws ForbiddenException;
    ChatRoomDto create(CreateChatRoomPayload payload, A user);
    ChatRoomDto update(UpdateChatRoomPayload payload, A user) throws ForbiddenException;
    ChatRoomDto deleteUser(UserChatRoomPayload payload, A user) throws ForbiddenException, NotFoundException;
    ChatRoomDto addUser(UserChatRoomPayload payload, A user) throws ForbiddenException, NotFoundException;

}
