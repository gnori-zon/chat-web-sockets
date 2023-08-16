package org.gnori.chatwebsockets.core.service.domain;

import org.gnori.chatwebsockets.api.dto.MessageDto;
import org.gnori.chatwebsockets.core.exception.impl.ForbiddenException;
import org.gnori.chatwebsockets.core.exception.impl.NotFoundException;

import java.util.List;

public interface MessageService<A> {

    List<MessageDto> getAll(MessageDto dto, A user) throws ForbiddenException;
    MessageDto get(MessageDto dto, A user) throws ForbiddenException, NotFoundException;
    MessageDto delete(MessageDto dto, A user) throws ForbiddenException, NotFoundException;
    MessageDto create(MessageDto dto, A user) throws ForbiddenException;
    MessageDto update(MessageDto dto, A user) throws ForbiddenException, NotFoundException;
}
