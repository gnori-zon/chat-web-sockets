package org.gnori.chatwebsockets.core.service.domain.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.CreateAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.UpdateAdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.payload.UserPayload;
import org.gnori.chatwebsockets.api.controller.user.user.payload.ChangePasswordUserPayload;
import org.gnori.chatwebsockets.api.controller.user.user.payload.CreateUserPayload;
import org.gnori.chatwebsockets.api.converter.impl.UserConverter;
import org.gnori.chatwebsockets.api.dto.UserDto;
import org.gnori.chatwebsockets.core.domain.user.User;
import org.gnori.chatwebsockets.core.domain.user.enums.Role;
import org.gnori.chatwebsockets.core.exception.impl.ConflictException;
import org.gnori.chatwebsockets.core.exception.impl.NotFoundException;
import org.gnori.chatwebsockets.core.repository.UserRepository;
import org.gnori.chatwebsockets.core.service.domain.UserService;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService<CustomUserDetails> {

    private static final String EXIST_USERNAME_EX = "User with this username already exist";

    BCryptPasswordEncoder bCryptPasswordEncoder;
    UserRepository repository;
    UserConverter converter;

    @Override
    public UserDto get(CustomUserDetails user) {
        final User existUser = repository.findByUsername(user.getUsername())
                .orElseThrow(NotFoundException::new);

        return converter.convertFrom(existUser);
    }

    @Override
    public UserDto create(CreateUserPayload payload) {
        if (repository.existsByUsername(payload.getUsername())) throw new ConflictException(EXIST_USERNAME_EX);

        final User userEntity = createFrom(payload);

        return converter.convertFrom(
                repository.save(userEntity)
        );
    }

    @Override
    public UserDto update(UserPayload payload, CustomUserDetails user) {
        final Long userId = user.getUserId();
        final User userEntity = repository.findById(userId)
                .orElseThrow(NotFoundException::new);

        userEntity.setName(payload.getName());
        userEntity.setEmail(payload.getEmail());

        return converter.convertFrom(repository.save(userEntity));
    }

    @Override
    public UserDto changePassword(ChangePasswordUserPayload payload, CustomUserDetails user) {
        if (isNotValidOldPass(payload, user)) throw new ConflictException("Not valid old password");
        final Long userId = user.getUserId();
        final User userEntity = repository.findById(userId)
                .orElseThrow(NotFoundException::new);

        userEntity.setPassword(bCryptPasswordEncoder.encode(payload.getNewPassword()));

        return converter.convertFrom(repository.save(userEntity));
    }

    @Override
    public UserDto adminUpdateById(UpdateAdminUserPayload payload) {
        final User oldUserEntity = repository.findById(payload.getId()).orElseThrow(NotFoundException::new);
        if (isNewUsernameAndSomeoneElseHasIt(payload.getUsername(), oldUserEntity.getUsername()))
            throw new ConflictException(EXIST_USERNAME_EX);

        oldUserEntity.setUsername(payload.getUsername());
        oldUserEntity.setName(payload.getName());
        oldUserEntity.setEmail(payload.getEmail());
        oldUserEntity.setRoles(payload.getRoleList());

        return converter.convertFrom(repository.save(oldUserEntity));
    }

    @Override
    public UserDto adminCreate(CreateAdminUserPayload payload) {
        if (repository.existsByUsername(payload.getUsername())) throw new ConflictException(EXIST_USERNAME_EX);
        final User newUserEntity = createFrom(payload);
        return converter.convertFrom(repository.save(newUserEntity));
    }

    @Override
    public void delete(CustomUserDetails user) {
        repository.deleteById(user.getUserId());
    }

    @Override
    public void adminDelete(Long id) {
        repository.deleteById(id);
    }

    private boolean isNewUsernameAndSomeoneElseHasIt(String dtoUsername, String username) {
        return !dtoUsername.equals(username) && repository.existsByUsername(dtoUsername);
    }

    private boolean isNotValidOldPass(ChangePasswordUserPayload payload, CustomUserDetails user) {
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
        user.setChatIds(new ArrayList<>());
        return user;
    }

    private User createFrom(String username, String password, String name, String email) {

        return new User(
                username,
                bCryptPasswordEncoder.encode(password),
                name,
                email,
                null, null
        );
    }

}
