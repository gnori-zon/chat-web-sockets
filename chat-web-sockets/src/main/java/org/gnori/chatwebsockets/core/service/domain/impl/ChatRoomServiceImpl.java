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
import org.gnori.chatwebsockets.api.dto.ActionType;
import org.gnori.chatwebsockets.api.dto.ChatRoomDto;
import org.gnori.chatwebsockets.core.domain.chat.ChatRoom;
import org.gnori.chatwebsockets.core.domain.user.User;
import org.gnori.chatwebsockets.core.exception.impl.ForbiddenException;
import org.gnori.chatwebsockets.core.exception.impl.NotFoundException;
import org.gnori.chatwebsockets.core.repository.ChatRoomRepository;
import org.gnori.chatwebsockets.core.repository.MessageRepository;
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
    MessageRepository messageRepository;
    UserRepository userRepository;

    @Override
    public List<ChatRoomDto> getAll(CustomUserDetails user) {

        final Long userId = user.getUserId();
        final User userEntity = userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);
        final List<String> chatIds = userEntity.getChatIds();

        if (chatIds != null && !chatIds.isEmpty()) {

            return converter.convertWithActionType(
                    StreamSupport.stream(repository.findAllById(chatIds).spliterator(), false)
                            .collect(Collectors.toList()),
                    ActionType.GET
            );
        }

        return Collections.emptyList();
    }

    @Override
    public ChatRoomDto get(ChatRoomPayload payload, CustomUserDetails user) {

        final String chatRoomId = payload.getChatRoomId();

        if (userRepository.hasChatRoomId(user.getUsername(), chatRoomId)) {

            return converter.convertWithActionType(getChatRoomOrElseThrow(chatRoomId), ActionType.GET);
        }
        throw new ForbiddenException();
    }

    @Override
    public ChatRoomDto create(CreateChatRoomPayload payload, CustomUserDetails user) {

        final Long userId = user.getUserId();
        final User userEntity = userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);

        ChatRoom chatRoom = new ChatRoom(
                null,
                payload.getName(),
                payload.getDescription(),
                user.getUsername(),
                List.of(userEntity)
        );

        final List<String> chatIds = userEntity.getChatIds();
        chatIds.add(chatRoom.getId());

        chatRoom = repository.save(chatRoom);
        userRepository.addChatRoomId(user.getUsername(), chatRoom.getId());

        return converter.convertWithActionType(chatRoom, ActionType.CREATE);
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

                return converter.convertWithActionType(repository.save(chatRoom), ActionType.UPDATE);
            }
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
                chatRoom.getConnectedUsers()
                        .forEach(
                                connectedUser -> {
                                    userRepository.deleteChatRoomId(connectedUser.getUsername(), chatRoomId);
                                    messageRepository.deleteAllByChatRoomIdAndUsername(connectedUser.getUsername(), chatRoomId);
                                }
                        );

                return converter.convertWithActionType(chatRoom, ActionType.DELETE);
            }
        }
        throw new ForbiddenException();
    }

    @Override
    public ChatRoomDto deleteUser(UserChatRoomPayload payload, CustomUserDetails user) {

        final String chatRoomId = payload.getChatRoomId();
        final String username = payload.getUsername();
        final ChatRoom chatRoom = getChatRoomOrElseThrow(chatRoomId);

        if (isCanBeDeleted(chatRoom, user, username)) {

            if (isNotBlankList(chatRoom.getConnectedUsers())) {

                final User deletingUser = userRepository.findByUsername(username)
                        .orElseThrow(NotFoundException::new);

                boolean isDeleted = chatRoom.getConnectedUsers().remove(deletingUser);

                if (isDeleted) {
                    repository.save(chatRoom);
                }
            }
            userRepository.deleteChatRoomId(username, chatRoomId);

            return converter.convertWithActionType(chatRoom, ActionType.UPDATE);
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
            final Set<String> chatIds = new HashSet<>(ListUtils.defaultIfNull(addingUser.getChatIds(), new ArrayList<>()));

            if (!chatIds.contains(chatRoomId)) {
                existUser.add(addingUser);
                chatRoom.setConnectedUsers(existUser.stream().toList());
                chatIds.add(chatRoomId);
                addingUser.setChatIds(chatIds.stream().toList());

                chatRoom = repository.save(chatRoom);
                userRepository.save(addingUser);
            }

            return converter.convertWithActionType(chatRoom, ActionType.UPDATE);
        }
        throw new ForbiddenException();
    }

    private boolean isCanBeDeleted(ChatRoom chatRoom, CustomUserDetails user, String username) {

        if (user.getUsername().equals(chatRoom.getOwnerUsername())) {
            return !user.getUsername().equals(username);
        } else {
            return user.getUsername().equals(username);
        }
    }

    private ChatRoom getChatRoomOrElseThrow(String chatRoomId) {

        return repository.findById(chatRoomId)
                .orElseThrow(NotFoundException::new);
    }

    private boolean isNotBlankList(List<?> list) {
        return list != null && !list.isEmpty();
    }
}
