package org.gnori.chatwebsockets.core.service.domain;

import org.gnori.chatwebsockets.api.controller.user.admin.payload.CreateAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.UpdateAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.payload.UserPayload;
import org.gnori.chatwebsockets.api.controller.user.user.payload.ChangePasswordUserPayload;
import org.gnori.chatwebsockets.api.controller.user.user.payload.CreateUserPayload;
import org.gnori.chatwebsockets.api.dto.UserDto;
import org.gnori.chatwebsockets.core.exception.impl.ConflictException;
import org.gnori.chatwebsockets.core.exception.impl.NotFoundException;

public interface UserService<A> {

    UserDto create(CreateUserPayload payload) throws ConflictException;
    UserDto update(UserPayload payload, A user) throws ConflictException;
    UserDto changePassword(ChangePasswordUserPayload payload, A user) throws ConflictException;
    UserDto adminUpdateById(UpdateAdminUserPayload payload) throws NotFoundException, ConflictException;
    UserDto adminCreate(CreateAdminUserPayload payload) throws ConflictException;
    void delete(A user);
    void adminDelete(Long id);
}
