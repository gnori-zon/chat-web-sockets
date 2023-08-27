package org.gnori.chatwebsockets.core.service.domain.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.ListUtils;
import org.gnori.chatwebsockets.api.controller.chatroom.payload.ChatRoomPayload;
import org.gnori.chatwebsockets.api.controller.chatroom.payload.CreateChatRoomPayload;
import org.gnori.chatwebsockets.api.controller.chatroom.payload.UpdateChatRoomPayload;
import org.gnori.chatwebsockets.api.controller.chatroom.user.UserChatRoomPayload;
import org.gnori.chatwebsockets.api.converter.impl.ChatRoomConverter;
import org.gnori.chatwebsockets.api.dto.ChatRoomDto;
import org.gnori.chatwebsockets.core.domain.chat.ChatRoom;
import org.gnori.chatwebsockets.core.domain.user.User;
import org.gnori.chatwebsockets.core.exception.impl.ForbiddenException;
import org.gnori.chatwebsockets.core.exception.impl.NotFoundException;
import org.gnori.chatwebsockets.core.repository.ChatRoomRepository;
import org.gnori.chatwebsockets.core.repository.UserRepository;
import org.gnori.chatwebsockets.core.service.domain.ChatRoomService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatRoomServiceImpl implements ChatRoomService<CustomUserDetails> {

    ChatRoomConverter converter;
    ChatRoomRepository repository;
    UserRepository userRepository;

    @Override
    public List<ChatRoomDto> getAll(CustomUserDetails user) {
        final User userData = user.getUser();
        final List<String> chatIds = userData.getChatIds();
        if (chatIds == null || chatIds.isEmpty()) return Collections.emptyList();
        return converter.convertAll(
                StreamSupport.stream(repository.findAllById(chatIds).spliterator(), false)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public ChatRoomDto get(ChatRoomPayload payload, CustomUserDetails user) {
        final String chatRoomId = payload.getChatRoomId();
        if (userRepository.hasChatRoomId(user.getUsername(), chatRoomId)) {
            return converter.convertFrom(getChatRoomOrElseThrow(chatRoomId));
        }
        throw new ForbiddenException();
    }

    @Override
    public ChatRoomDto delete(ChatRoomPayload payload, CustomUserDetails user) {
        final String chatRoomId = payload.getChatRoomId();
        final Optional<ChatRoom> optionalChatRoom = repository.findById(chatRoomId);
        if (optionalChatRoom.isPresent()) {
            final ChatRoom chatRoom = optionalChatRoom.get();
            if (user.getUsername().equals(chatRoom.getOwnerUsername())) {
                repository.deleteById(chatRoomId);
                return null;
            }
        }
        throw new ForbiddenException();
    }

    @Override
    public ChatRoomDto create(CreateChatRoomPayload payload, CustomUserDetails user) {
        ChatRoom chatRoom = new ChatRoom(
                null,
                payload.getName(),
                payload.getDescription(),
                user.getUsername(),
                List.of(user.getUser())
        );

        chatRoom = repository.save(chatRoom);
        userRepository.addChatRoomId(user.getUsername(), chatRoom.getId());

        return converter.convertFrom(chatRoom);
    }

    @Override
    public ChatRoomDto update(UpdateChatRoomPayload payload, CustomUserDetails user) {
        final String chatRoomId = payload.getChatRoomId();
        final Optional<ChatRoom> optionalChatRoom = repository.findById(chatRoomId);
        if (optionalChatRoom.isPresent()) {
            final ChatRoom chatRoom = optionalChatRoom.get();
            if (user.getUsername().equals(chatRoom.getOwnerUsername())) {
                chatRoom.setName(payload.getName());
                chatRoom.setDescription(payload.getDescription());
                return converter.convertFrom(
                        repository.save(chatRoom)
                );
            }
        }
        throw new ForbiddenException();
    }

    @Override
    public ChatRoomDto deleteUser(UserChatRoomPayload payload, CustomUserDetails user) {
        final String chatRoomId = payload.getChatRoomId();
        final String username = payload.getUsername();
        final ChatRoom chatRoom = getChatRoomOrElseThrow(chatRoomId);

        if (user.getUsername().equals(chatRoom.getOwnerUsername()) && !user.getUsername().equals(username)) {
            if (isNotBlankList(chatRoom.getConnectedUsers())) {
                final User deletingUser = userRepository.findByUsername(username)
                        .orElseThrow(NotFoundException::new);

                boolean isDeleted = chatRoom.getConnectedUsers().remove(deletingUser);
                if (isDeleted) {
                    repository.save(chatRoom);
                }
            }
            userRepository.deleteChatRoomId(username, chatRoomId);
            return converter.convertFrom(chatRoom);
        }
        throw new ForbiddenException();
    }

    @Override
    public ChatRoomDto addUser(UserChatRoomPayload payload, CustomUserDetails user) {
        final String chatRoomId = payload.getChatRoomId();
        final String username = payload.getUsername();
        ChatRoom chatRoom = getChatRoomOrElseThrow(chatRoomId);

        if (user.getUsername().equals(chatRoom.getOwnerUsername())) {
            final User addingUser = userRepository.findByUsername(username)
                    .orElseThrow(NotFoundException::new);

            final Set<User> existUser = new HashSet<>(ListUtils.defaultIfNull(chatRoom.getConnectedUsers(), new ArrayList<>()));
            if (!new HashSet<>(addingUser.getChatIds()).contains(chatRoomId)) {
                existUser.add(addingUser);
                chatRoom.setConnectedUsers(existUser.stream().toList());
                addingUser.getChatIds().add(chatRoomId);

                chatRoom = repository.save(chatRoom);
                userRepository.save(addingUser);
            }
            return converter.convertFrom(chatRoom);
        }
        throw new ForbiddenException();
    }

    private ChatRoom getChatRoomOrElseThrow(String chatRoomId) {
        return repository.findById(chatRoomId)
                .orElseThrow(NotFoundException::new);
    }

    private boolean isNotBlankList(List<?> list) {
        return list != null && !list.isEmpty();
    }
}
