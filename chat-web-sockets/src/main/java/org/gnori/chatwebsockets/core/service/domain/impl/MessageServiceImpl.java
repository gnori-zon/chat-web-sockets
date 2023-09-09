package org.gnori.chatwebsockets.core.service.domain.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.message.payload.CreateMessagePayload;
import org.gnori.chatwebsockets.api.controller.message.payload.MessagePayload;
import org.gnori.chatwebsockets.api.controller.message.payload.UpdateMessagePayload;
import org.gnori.chatwebsockets.api.converter.impl.MessageConverter;
import org.gnori.chatwebsockets.api.dto.MessageDto;
import org.gnori.chatwebsockets.core.domain.chat.ChatRoom;
import org.gnori.chatwebsockets.core.domain.message.Message;
import org.gnori.chatwebsockets.core.domain.message.MessagePrimaryKey;
import org.gnori.chatwebsockets.core.domain.user.User;
import org.gnori.chatwebsockets.core.exception.impl.ForbiddenException;
import org.gnori.chatwebsockets.core.exception.impl.NotFoundException;
import org.gnori.chatwebsockets.core.repository.ChatRoomRepository;
import org.gnori.chatwebsockets.core.repository.MessageRepository;
import org.gnori.chatwebsockets.core.repository.UserRepository;
import org.gnori.chatwebsockets.core.service.domain.MessageService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageServiceImpl implements MessageService<CustomUserDetails> {

    MessageConverter converter;
    MessageRepository repository;
    ChatRoomRepository chatRoomRepository;
    UserRepository userRepository;

    @Override
    public List<MessageDto> getAll(MessagePayload payload, CustomUserDetails user) {
        final String chatRoomId = payload.getChatRoomId();
        final List<String> chatIds = getUserChatIds(user);

        if (chatIds.contains(chatRoomId)) {

            return converter.convertAll(repository.findAllByChatId(chatRoomId));
        }
        throw new ForbiddenException();
    }

    @Override
    public MessageDto get(MessagePayload payload, CustomUserDetails user) {
        final String chatRoomId = payload.getChatRoomId();
        final List<String> chatIds = getUserChatIds(user);

        if (chatIds.contains(chatRoomId)) {
            final MessagePrimaryKey primaryKey = new MessagePrimaryKey(payload.getFromUser(), payload.getChatRoomId(), payload.getDate());

            return converter.convertFrom(
                    getMessageOrElseThrow(primaryKey)
            );
        }
        throw new ForbiddenException();
    }

    @Override
    public MessageDto delete(MessagePayload payload, CustomUserDetails user) {
        final String chatRoomId = payload.getChatRoomId();
        final List<String> chatIds = getUserChatIds(user);

        if (chatIds.contains(chatRoomId)) {
            if (user.getUsername().equals(payload.getFromUser()) || isOwnerChatRoom(chatRoomId, user)) {
                final MessagePrimaryKey primaryKey = new MessagePrimaryKey(payload.getFromUser(), payload.getChatRoomId(), payload.getDate());

                repository.deleteByKey(primaryKey.getUsername(), primaryKey.getChatRoomId(), primaryKey.getDate());

                return new MessageDto(primaryKey, null, null);
            }
        }
        throw new ForbiddenException();
    }

    @Override
    public MessageDto create(CreateMessagePayload payload, CustomUserDetails user) {
        final String chatRoomId = payload.getChatRoomId();
        final List<String> chatIds = getUserChatIds(user);

        if (chatIds.contains(chatRoomId)) {
            final Message message = new Message(
                    new MessagePrimaryKey(payload.getFromUser(), payload.getChatRoomId(), payload.getDate()),
                    user.getUsername(),
                    payload.getText()
            );

            return converter.convertFrom(
                    repository.save(message)
            );
        }
        throw new ForbiddenException();
    }

    @Override
    public MessageDto update(UpdateMessagePayload payload, CustomUserDetails user) {
        final String chatRoomId = payload.getChatRoomId();
        final List<String> chatIds = getUserChatIds(user);

        if (chatIds.contains(chatRoomId)) {
            final MessagePrimaryKey primaryKey = new MessagePrimaryKey(payload.getFromUser(), payload.getChatRoomId(), payload.getDate());
            final Message message = getMessageOrElseThrow(primaryKey);
            message.setFromUser(payload.getFromUser());
            message.setText(payload.getText());

            return converter.convertFrom(repository.save(message));
        }
        throw new ForbiddenException();
    }

    private boolean isOwnerChatRoom(String chatRoomId, CustomUserDetails user) {
        return user.getUsername().equals(
                chatRoomRepository.findById(chatRoomId)
                        .map(ChatRoom::getOwnerUsername)
                        .orElseThrow(NotFoundException::new)
        );
    }

    private Message getMessageOrElseThrow(MessagePrimaryKey primaryKey) {
        return repository.findByKey(primaryKey.getUsername(), primaryKey.getChatRoomId(), primaryKey.getDate())
                .orElseThrow(NotFoundException::new);
    }

    private List<String> getUserChatIds(CustomUserDetails user) {
        final Long userId = user.getUserId();
        final User userEntity = userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);         /* get from userRepository because chatIds is mutable parameter
                                                                                    (that can be changed by other users) */
        final List<String> chatIds = userEntity.getChatIds();

        return chatIds != null ? chatIds : Collections.emptyList();
    }
}
