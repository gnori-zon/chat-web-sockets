package org.gnori.chatwebsockets.api.converter;

import org.gnori.chatwebsockets.api.dto.ChatRoomDto;
import org.gnori.chatwebsockets.api.dto.UserDto;
import org.gnori.chatwebsockets.core.domain.chat.ChatRoom;
import org.gnori.chatwebsockets.core.domain.user.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ChatRoomConverter implements AbstractConverter<ChatRoomDto, ChatRoom> {

    @Override
    public ChatRoom convertFrom(ChatRoomDto dto) {
        if (dto == null) return null;
        final ChatRoom chatRoom = new ChatRoom();

        chatRoom.setName(dto.getName());
        chatRoom.setDescription(dto.getDescription());
        chatRoom.setOwnerUsername(dto.getOwnerUsername());
        return chatRoom;
    }

    @Override
    public ChatRoomDto convertFrom(ChatRoom entity) {
        if (entity == null) return null;
        final ChatRoomDto chatRoomDto = new ChatRoomDto();

        chatRoomDto.setId(entity.getId());
        chatRoomDto.setName(entity.getName());
        chatRoomDto.setDescription(entity.getDescription());
        chatRoomDto.setOwnerUsername(entity.getOwnerUsername());
        chatRoomDto.setConnectedUsers(convertAllUserFrom(entity.getConnectedUsers()));

        return chatRoomDto;
    }

    public List<UserDto> convertAllUserFrom(List<User> connectedUsers) {
        if (connectedUsers == null) return Collections.emptyList();
        return connectedUsers.stream()
                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getName(), user.getEmail()))
                .toList();
    }
}
