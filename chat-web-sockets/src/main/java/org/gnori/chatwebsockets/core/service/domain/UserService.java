package org.gnori.chatwebsockets.core.service.domain;

import org.gnori.chatwebsockets.api.controller.user.admin.payload.CreateAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.DeleteAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.GetAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.UpdateAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.user.payload.ChangePasswordUserPayload;
import org.gnori.chatwebsockets.api.controller.user.user.payload.CreateUserPayload;
import org.gnori.chatwebsockets.api.controller.user.user.payload.UpdateUserPayload;
import org.gnori.chatwebsockets.api.dto.UserDto;
import org.gnori.chatwebsockets.core.exception.impl.ConflictException;
import org.gnori.chatwebsockets.core.exception.impl.NotFoundException;

public interface UserService<A> extends DomainService {

    UserDto get(A user) throws NotFoundException;
    UserDto create(CreateUserPayload payload) throws ConflictException;
    UserDto update(UpdateUserPayload payload, A user) throws ConflictException;
    UserDto changePassword(ChangePasswordUserPayload payload, A user) throws ConflictException;
    void delete(A user);

    UserDto adminGet(GetAdminUserPayload payload, A adminUser);
    UserDto adminCreate(CreateAdminUserPayload payload, A adminUser) throws ConflictException;
    UserDto adminUpdate(UpdateAdminUserPayload payload, A adminUser) throws NotFoundException, ConflictException;
    UserDto adminDelete(DeleteAdminUserPayload payload, A adminUser);

}
