package org.gnori.chatwebsockets.core.service.domain.impl;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.chatroom.payload.ChatRoomPayload;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.CreateAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.DeleteAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.GetAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.UpdateAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.user.payload.ChangePasswordUserPayload;
import org.gnori.chatwebsockets.api.controller.user.user.payload.CreateUserPayload;
import org.gnori.chatwebsockets.api.controller.user.user.payload.UpdateUserPayload;
import org.gnori.chatwebsockets.api.converter.impl.UserConverter;
import org.gnori.chatwebsockets.api.dto.ActionType;
import org.gnori.chatwebsockets.api.dto.UserDto;
import org.gnori.chatwebsockets.core.domain.user.User;
import org.gnori.chatwebsockets.core.domain.user.enums.Role;
import org.gnori.chatwebsockets.core.exception.impl.ConflictException;
import org.gnori.chatwebsockets.core.exception.impl.ForbiddenException;
import org.gnori.chatwebsockets.core.exception.impl.NotFoundException;
import org.gnori.chatwebsockets.core.repository.UserRepository;
import org.gnori.chatwebsockets.core.service.domain.ChatRoomService;
import org.gnori.chatwebsockets.core.service.domain.UserService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService<CustomUserDetails> {

    private static final String EXIST_USERNAME_EX = "User with this username already exist";
    private static final SimpleGrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority(Role.ADMIN.name());

    ChatRoomService<CustomUserDetails> chatRoomService;
    BCryptPasswordEncoder bCryptPasswordEncoder;
    UserRepository repository;
    UserConverter converter;

    @Override
    public UserDto get(CustomUserDetails user) {
        return converter.convertWithActionType(getOrElseThrow(user.getUsername()), ActionType.GET);
    }

    @Override
    public UserDto create(CreateUserPayload payload) {
        if (repository.existsByUsername(payload.getUsername())) {
            throw new ConflictException(EXIST_USERNAME_EX);
        }

        final User userEntity = createFrom(payload);

        return converter.convertWithActionType(repository.save(userEntity), ActionType.CREATE);
    }

    @Override
    public UserDto update(UpdateUserPayload payload, CustomUserDetails user) {
        final Long userId = user.getUserId();
        final User userEntity = repository.findById(userId)
                .orElseThrow(NotFoundException::new);

        userEntity.setName(payload.getName());
        userEntity.setEmail(payload.getEmail());

        return converter.convertWithActionType(repository.save(userEntity), ActionType.UPDATE);
    }

    @Override
    public UserDto changePassword(ChangePasswordUserPayload payload, CustomUserDetails user) {
        if (isNotValidOldPassword(payload, user)) throw new ConflictException("Not valid old password");
        final Long userId = user.getUserId();
        final User userEntity = repository.findById(userId)
                .orElseThrow(NotFoundException::new);

        userEntity.setPassword(bCryptPasswordEncoder.encode(payload.getNewPassword()));

        return converter.convertWithActionType(repository.save(userEntity), ActionType.UPDATE);
    }

    @Override
    public void delete(CustomUserDetails user) {

        deleteOwnedChatRoom(user);
        repository.deleteById(user.getUserId());
    }

    @Override
    public UserDto adminGet(GetAdminUserPayload payload, CustomUserDetails adminUser) {

        return invokeIfAdmin(
                adminUser,
                () -> converter.convertWithActionType(getOrElseThrow(payload.getUsername()), ActionType.GET)
        );
    }

    @Override
    public UserDto adminCreate(CreateAdminUserPayload payload, CustomUserDetails adminUser) {

        return invokeIfAdmin(
                adminUser,
                () -> {
                    if (repository.existsByUsername(payload.getUsername())) {
                        throw new ConflictException(EXIST_USERNAME_EX);
                    }
                    final User newUserEntity = createFrom(payload);

                    return converter.convertWithActionType(repository.save(newUserEntity), ActionType.CREATE);
                }
        );
    }

    @Override
    public UserDto adminUpdate(UpdateAdminUserPayload payload, CustomUserDetails adminUser) {

        return invokeIfAdmin(
                adminUser,
                () -> {
                    final User oldUserEntity = repository.findByUsername(payload.getUsername())
                            .orElseThrow(NotFoundException::new);

                    oldUserEntity.setName(payload.getName());
                    oldUserEntity.setEmail(payload.getEmail());
                    oldUserEntity.setRoles(payload.getRoleList());

                    return converter.convertWithActionType(repository.save(oldUserEntity), ActionType.UPDATE);
                }
        );
    }

    @Override
    public UserDto adminDelete(DeleteAdminUserPayload payload, CustomUserDetails adminUser) {

        return invokeIfAdmin(
                adminUser,
                () -> {
                    final User user = repository.findByUsername(payload.getUsername())
                            .orElseThrow(NotFoundException::new);

                    deleteOwnedChatRoom(new CustomUserDetails(user));
                    repository.delete(user);

                    return converter.convertWithActionType(emptyUser(user.getId()), ActionType.DELETE);
                }
        );
    }

    private User getOrElseThrow(String username) {

        return repository.findByUsername(username)
                .orElseThrow(NotFoundException::new);
    }

    private void deleteOwnedChatRoom(CustomUserDetails user) {

        chatRoomService.getAll(user)
                .forEach(
                        chat -> {
                            if (user.getUsername().equals(chat.getOwnerUsername())) {
                                chatRoomService.delete(new ChatRoomPayload(chat.getId()), user);
                            }
                        }
                );
    }

    private boolean isNotValidOldPassword(ChangePasswordUserPayload payload, CustomUserDetails user) {
        return !bCryptPasswordEncoder.matches(payload.getOldPassword(), user.getPassword());
    }

    private User createFrom(CreateAdminUserPayload payload) {

        final User user = createFrom(payload.getUsername(), payload.getPassword(), payload.getName(), payload.getEmail());
        user.setRoles(payload.getRoleList());

        return user;
    }

    private User createFrom(CreateUserPayload payload) {

        final User user = createFrom(payload.getUsername(), payload.getPassword(), payload.getName(), payload.getEmail());
        user.setRoles(List.of(Role.USER));

        return user;
    }

    private User createFrom(String username, String password, String name, String email) {

        return new User(
                username,
                bCryptPasswordEncoder.encode(password),
                name,
                email,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    private User emptyUser(Long id) {

        final User emptyUser = new User(null, null, null, null, null, null);
        emptyUser.setId(id);

        return emptyUser;
    }

    private <T> T invokeIfAdmin(@NonNull CustomUserDetails userDetails, Supplier<T> supplier) {

        if (userDetails.getAuthorities().contains(ADMIN_AUTHORITY)) {
            return supplier.get();
        } else {
            throw new ForbiddenException();
        }
    }

}
