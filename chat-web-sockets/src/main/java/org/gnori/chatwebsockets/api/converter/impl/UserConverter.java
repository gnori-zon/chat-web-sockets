package org.gnori.chatwebsockets.api.converter.impl;

import org.gnori.chatwebsockets.api.converter.AbstractConverter;
import org.gnori.chatwebsockets.api.dto.UserDto;
import org.gnori.chatwebsockets.core.domain.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserConverter implements AbstractConverter<UserDto, User> {

    @Override
    public User convertFrom(UserDto dto) {
        return new User(
                dto.getUsername(),
                null,
                dto.getName(),
                dto.getEmail(),
                null,
                null
        );
    }

    @Override
    public UserDto convertFrom(User entity) {
        return new UserDto(
                entity.getId(),
                entity.getUsername(),
                entity.getName(),
                entity.getEmail()
        );
    }
}
