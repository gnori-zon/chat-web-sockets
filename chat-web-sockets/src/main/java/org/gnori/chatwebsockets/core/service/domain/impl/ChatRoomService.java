package org.gnori.chatwebsockets.core.service.domain.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.ListUtils;
import org.gnori.chatwebsockets.api.converter.ChatRoomConverter;
import org.gnori.chatwebsockets.api.dto.ChatRoomDto;
import org.gnori.chatwebsockets.api.dto.UserDto;
import org.gnori.chatwebsockets.core.domain.chat.ChatRoom;
import org.gnori.chatwebsockets.core.domain.user.User;
import org.gnori.chatwebsockets.core.repository.ChatRoomRepository;
import org.gnori.chatwebsockets.core.repository.UserRepository;
import org.gnori.chatwebsockets.core.service.domain.BaseService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatRoomService extends BaseService<ChatRoom, String, ChatRoomDto, ChatRoomConverter, ChatRoomRepository> {

    UserRepository userRepository;

    public ChatRoomService(ChatRoomConverter converter, ChatRoomRepository repository, UserRepository userRepository) {
        super(converter, repository);
        this.userRepository = userRepository;
    }

    @Override
    public List<ChatRoomDto> getAll(CustomUserDetails user) {
        final User userData = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new RuntimeException("internal error"));
        final List<String> chatIds = userData.getChatIds();
        if (chatIds == null || chatIds.isEmpty()) return Collections.emptyList();
        return converter.convertAll(
                StreamSupport.stream(repository.findAllById(chatIds).spliterator(), false)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public ChatRoomDto getById(String chatRoomId, CustomUserDetails user) {
        if (userRepository.hasChatRoomId(user.getUsername(), chatRoomId)) {
            return super.getById(chatRoomId, user);
        }
        throw new RuntimeException("You don't have enough rights to do this");
    }

    @Override
    public void deleteById(String chatRoomId, CustomUserDetails user) {
        final Optional<ChatRoom> optionalChatRoom = repository.findById(chatRoomId);
        if (optionalChatRoom.isPresent()) {
            final ChatRoom chatRoom = optionalChatRoom.get();
            if (user.getUsername().equals(chatRoom.getOwnerUsername())) {
                repository.deleteById(chatRoomId);
                return;
            }
        }
        throw new RuntimeException("You don't have enough rights to do this");
    }

    @Override
    public ChatRoomDto create(ChatRoomDto dto, CustomUserDetails user) {
        ChatRoom chatRoom = converter.convertFrom(dto);
        chatRoom.setConnectedUsers(Collections.singletonList(user.getUser()));
        chatRoom.setOwnerUsername(user.getUsername());

        chatRoom = repository.save(chatRoom);
        userRepository.addChatRoomId(user.getUsername(), chatRoom.getId());

        return converter.convertFrom(chatRoom);
    }

    @Override
    public ChatRoomDto updateById(String chatRoomId, ChatRoomDto dto, CustomUserDetails user) {
        final Optional<ChatRoom> optionalChatRoom = repository.findById(chatRoomId);
        if (optionalChatRoom.isPresent()) {
            final ChatRoom chatRoom = optionalChatRoom.get();
            if (user.getUsername().equals(chatRoom.getOwnerUsername())) {
                chatRoom.setName(dto.getName());
                chatRoom.setDescription(dto.getDescription());
                return converter.convertFrom(
                        repository.save(chatRoom)
                );
            }
        }
        throw new RuntimeException("You don't have enough rights to do this");
    }

    public List<UserDto> deleteUser(String chatRoomId, String username, CustomUserDetails user) {
        final ChatRoom chatRoom = getChatRoomOrElseThrow(chatRoomId);

        if (user.getUsername().equals(chatRoom.getOwnerUsername()) && !user.getUsername().equals(username)) {
            if (isNotBlankList(chatRoom.getConnectedUsers())) {
                chatRoom.getConnectedUsers().remove(user.getUser());
            }
            repository.save(chatRoom);
            userRepository.deleteChatRoomId(username, chatRoomId);
            return converter.convertAllUserFrom(chatRoom.getConnectedUsers());
        }
        throw new RuntimeException("You don't have enough rights to do this");
    }

    public List<UserDto> addUser(String chatRoomId, String username, CustomUserDetails user) {
        ChatRoom chatRoom = getChatRoomOrElseThrow(chatRoomId);

        if (user.getUsername().equals(chatRoom.getOwnerUsername())) {
            final Set<User> existUser = new HashSet<>(ListUtils.defaultIfNull(chatRoom.getConnectedUsers(), new ArrayList<>()));
            existUser.add(user.getUser());
            chatRoom.setConnectedUsers(existUser.stream().toList());

            chatRoom = repository.save(chatRoom);
            userRepository.addChatRoomId(username, chatRoomId);
            return converter.convertAllUserFrom(chatRoom.getConnectedUsers());
        }
        throw new RuntimeException("You don't have enough rights to do this");
    }

    private ChatRoom getChatRoomOrElseThrow(String chatRoomId) {
        return repository.findById(chatRoomId)
                .orElseThrow(
                        () -> new RuntimeException(String.format("not found chat-room with id: %s", chatRoomId))
                );
    }

    private boolean isNotBlankList(List<?> list) {
        return list != null && !list.isEmpty();
    }
}
