package org.gnori.chatwebsockets.core.service.domain;

import org.gnori.chatwebsockets.api.controller.user.payload.AdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.payload.UserPayload;
import org.gnori.chatwebsockets.api.dto.UserDto;
import org.gnori.chatwebsockets.core.exception.impl.ConflictException;
import org.gnori.chatwebsockets.core.exception.impl.NotFoundException;

public interface UserService<A> {

    UserDto create(UserPayload payload) throws ConflictException;
    UserDto update(UserDto dto, A user) throws ConflictException;
    UserDto changePassword(UserPayload payload, A user) throws ConflictException;
    UserDto adminUpdateById(AdminUserPayload payload) throws NotFoundException, ConflictException;
    UserDto adminCreate(AdminUserPayload payload) throws ConflictException;
    void delete(A user);
}
