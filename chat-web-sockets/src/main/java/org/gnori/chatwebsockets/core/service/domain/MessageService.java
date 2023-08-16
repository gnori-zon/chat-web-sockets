package org.gnori.chatwebsockets.core.service.domain;

import org.gnori.chatwebsockets.api.controller.message.payload.CreateMessagePayload;
import org.gnori.chatwebsockets.api.controller.message.payload.MessagePayload;
import org.gnori.chatwebsockets.api.controller.message.payload.UpdateMessagePayload;
import org.gnori.chatwebsockets.api.dto.MessageDto;
import org.gnori.chatwebsockets.core.exception.impl.ForbiddenException;
import org.gnori.chatwebsockets.core.exception.impl.NotFoundException;

import java.util.List;

public interface MessageService<A> {

    List<MessageDto> getAll(MessagePayload payload, A user) throws ForbiddenException;
    MessageDto get(MessagePayload payload, A user) throws ForbiddenException, NotFoundException;
    MessageDto delete(MessagePayload payload, A user) throws ForbiddenException, NotFoundException;
    MessageDto create(CreateMessagePayload payload, A user) throws ForbiddenException;
    MessageDto update(UpdateMessagePayload payload, A user) throws ForbiddenException, NotFoundException;
}
