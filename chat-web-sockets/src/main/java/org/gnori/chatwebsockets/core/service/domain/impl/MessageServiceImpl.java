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
import org.gnori.chatwebsockets.core.exception.impl.ForbiddenException;
import org.gnori.chatwebsockets.core.exception.impl.NotFoundException;
import org.gnori.chatwebsockets.core.repository.ChatRoomRepository;
import org.gnori.chatwebsockets.core.repository.MessageRepository;
import org.gnori.chatwebsockets.core.service.domain.MessageService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageServiceImpl implements MessageService<CustomUserDetails> {

    MessageConverter converter;
    MessageRepository repository;
    ChatRoomRepository chatRoomRepository;

    @Override
    public List<MessageDto> getAll(MessagePayload payload, CustomUserDetails user) {
        final String chatRoomId = payload.getChatRoomId();
        if (user.getUser().getChatIds().contains(chatRoomId)) {

            return converter.convertAll(repository.findAllByChatId(chatRoomId));
        }
        throw new ForbiddenException();
    }

    @Override
    public MessageDto get(MessagePayload payload, CustomUserDetails user) {
        final String chatRoomId = payload.getChatRoomId();
        if (user.getUser().getChatIds().contains(chatRoomId)) {
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
        if (user.getUser().getChatIds().contains(chatRoomId)) {
            if (user.getUsername().equals(payload.getFromUser()) || isOwnerChatRoom(chatRoomId, user)) {
                final MessagePrimaryKey primaryKey = new MessagePrimaryKey(payload.getFromUser(), payload.getChatRoomId(), payload.getDate());

                repository.deleteById(MessagePrimaryKey.of(primaryKey));

                return new MessageDto(primaryKey, null, null);
            }
        }
        throw new ForbiddenException();
    }

    @Override
    public MessageDto create(CreateMessagePayload payload, CustomUserDetails user) {
        final String chatRoomId = payload.getChatRoomId();
        if (user.getUser().getChatIds().contains(chatRoomId)) {
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
        if (user.getUser().getChatIds().contains(chatRoomId)) {
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
        return repository.findById(MessagePrimaryKey.of(primaryKey))
                .orElseThrow(NotFoundException::new);
    }
}
