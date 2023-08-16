package org.gnori.chatwebsockets.api.converter.impl;

import org.gnori.chatwebsockets.api.converter.AbstractConverter;
import org.gnori.chatwebsockets.api.dto.MessageDto;
import org.gnori.chatwebsockets.core.domain.message.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageConverter implements AbstractConverter<MessageDto, Message> {
    @Override
    public Message convertFrom(MessageDto dto) {
        return new Message(
                dto.getId(),
                dto.getFromUser(),
                dto.getToUser(),
                dto.getText()
        );
    }

    @Override
    public MessageDto convertFrom(Message entity) {
        return new MessageDto(
                entity.getKey(),
                entity.getFromUser(),
                entity.getToUser(),
                entity.getText()
        );
    }
}
