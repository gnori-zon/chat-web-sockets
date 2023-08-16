package org.gnori.chatwebsockets.core.service.domain.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.user.payload.AdminUserPayload;
import org.gnori.chatwebsockets.api.controller.user.payload.UserPayload;
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
    public UserDto create(UserPayload payload) {
        if (repository.existsByUsername(payload.getUsername())) throw new ConflictException(EXIST_USERNAME_EX);

        final User userEntity = new User(
                payload.getUsername(),
                bCryptPasswordEncoder.encode(payload.getNewPassword()),
                payload.getName(),
                payload.getEmail(),
                null, null
        );
        userEntity.setRoles(List.of(Role.USER));

        return converter.convertFrom(
                repository.save(userEntity)
        );
    }

    @Override
    public UserDto update(UserDto dto, CustomUserDetails user) {
        if (isNewUsernameAndSomeoneElseHasIt(dto.getUsername(), user.getUsername())) throw new ConflictException(EXIST_USERNAME_EX);
        final User userEntity = user.getUser();
        userEntity.setName(dto.getName());
        userEntity.setUsername(dto.getUsername());
        userEntity.setEmail(dto.getEmail());
        return converter.convertFrom(repository.save(userEntity));
    }

    @Override
    public UserDto changePassword(UserPayload payload, CustomUserDetails user) {
        if (isNotValidOldPass(payload, user)) throw new ConflictException("Not valid old password");
        final User userEntity = user.getUser();
        userEntity.setPassword(bCryptPasswordEncoder.encode(payload.getNewPassword()));
        return converter.convertFrom(repository.save(userEntity));
    }

    @Override
    public UserDto adminUpdateById(AdminUserPayload payload) {
        final User oldUserEntity = repository.findById(payload.getId()).orElseThrow(NotFoundException::new);
        if (isNewUsernameAndSomeoneElseHasIt(payload.getUserPayload().getUsername(), oldUserEntity.getUsername())) throw new ConflictException(EXIST_USERNAME_EX);

        final User newUserEntity = createFrom(payload);
        newUserEntity.setChatIds(oldUserEntity.getChatIds());
        newUserEntity.setId(oldUserEntity.getId());

        return converter.convertFrom(repository.save(newUserEntity));
    }

    @Override
    public UserDto adminCreate(AdminUserPayload payload) {
        if (repository.existsByUsername(payload.getUserPayload().getUsername())) throw new ConflictException(EXIST_USERNAME_EX);
        final User newUserEntity = createFrom(payload);
        return converter.convertFrom(repository.save(newUserEntity));
    }

    @Override
    public void delete(CustomUserDetails user) {
        repository.delete(user.getUser());
    }

    private boolean isNewUsernameAndSomeoneElseHasIt(String dtoUsername, String username) {
        return !dtoUsername.equals(username) && repository.existsByUsername(dtoUsername);
    }

    private boolean isNotValidOldPass(UserPayload payload, CustomUserDetails user) {
        return !bCryptPasswordEncoder.encode(payload.getOldPassword()).equals(user.getPassword());
    }

    private User createFrom(AdminUserPayload payload) {
        return new User(
                payload.getUserPayload().getUsername(),
                bCryptPasswordEncoder.encode(payload.getUserPayload().getNewPassword()),
                payload.getUserPayload().getName(),
                payload.getUserPayload().getEmail(),
                payload.getRoleList(),
                null
        );
    }
}
