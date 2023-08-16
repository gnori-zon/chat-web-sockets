package org.gnori.chatwebsockets.core.service.domain.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
    public List<MessageDto> getAll(MessageDto dto, CustomUserDetails user) {
        final String chatRoomId = dto.getMessagePrimaryKey().getChatRoomId();
        if (user.getUser().getChatIds().contains(chatRoomId)) {
            final String chatRoomIdFormMessage = dto.getMessagePrimaryKey().getChatRoomId();
            final String usernameFromMessage = dto.getId().getUsername();

            return converter.convertAll(repository.findAllById(
                    List.of(MessagePrimaryKey.of(chatRoomIdFormMessage, usernameFromMessage))));
        }
        throw new ForbiddenException();
    }

    @Override
    public MessageDto get(MessageDto dto, CustomUserDetails user) {
        final String chatRoomId = dto.getMessagePrimaryKey().getChatRoomId();
        if (user.getUser().getChatIds().contains(chatRoomId)) {
            return converter.convertFrom(
                    getMessageOrElseThrow(dto.getMessagePrimaryKey())
            );
        }
        throw new ForbiddenException();
    }

    @Override
    public MessageDto delete(MessageDto dto, CustomUserDetails user) {
        final String chatRoomId = dto.getMessagePrimaryKey().getChatRoomId();
        if (user.getUser().getChatIds().contains(chatRoomId)) {
            if (dto.getFromUser().equals(user.getUsername()) || isOwnerChatRoom(chatRoomId, user)) {
                repository.deleteById(MessagePrimaryKey.of(dto.getMessagePrimaryKey()));

                return dto;
            }
        }
        throw new ForbiddenException();
    }

    @Override
    public MessageDto create(MessageDto dto, CustomUserDetails user) {
        final String chatRoomId = dto.getMessagePrimaryKey().getChatRoomId();
        if (user.getUser().getChatIds().contains(chatRoomId)) {
            dto.getMessagePrimaryKey().setUsername(user.getUsername());
            dto.setFromUser(user.getUsername());

            return converter.convertFrom(
                    repository.save(converter.convertFrom(dto))
            );
        }
        throw new ForbiddenException();
    }

    @Override
    public MessageDto update(MessageDto dto, CustomUserDetails user) {
        final String chatRoomId = dto.getMessagePrimaryKey().getChatRoomId();
        if (user.getUser().getChatIds().contains(chatRoomId)) {
            final Message message = getMessageOrElseThrow(dto.getMessagePrimaryKey());
            message.setFromUser(user.getUsername());
            message.setText(dto.getText());

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
