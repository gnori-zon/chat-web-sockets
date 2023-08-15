package org.gnori.chatwebsockets.core.service.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.converter.AbstractConverter;
import org.gnori.chatwebsockets.api.dto.AbstractDto;
import org.gnori.chatwebsockets.core.exception.impl.NotFoundException;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class BaseService<E, ID, D extends AbstractDto<ID>, C extends AbstractConverter<D, E>, R extends CrudRepository<E, ID>>
        implements AbstractService<ID, D>{

    C converter;
    R repository;

    @Override
    public List<D> getAll(CustomUserDetails user) {
        return converter.convertAll(
                StreamSupport.stream(repository.findAll().spliterator(), false)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public D getById(ID id, CustomUserDetails user) {
        return converter.convertFrom(
                repository.findById(id)
                        .orElseThrow(NotFoundException::new)
        );
    }

    @Override
    public void deleteById(ID id, CustomUserDetails user) {
        repository.deleteById(id);
    }

    @Override
    public D create(D dto, CustomUserDetails user) {
        return converter.convertFrom(
                repository.save(converter.convertFrom(dto))
        );
    }

    @Override
    public D updateById(ID id, D dto, CustomUserDetails user) {
        if (!repository.existsById(id)) throw new NotFoundException();
        dto.setId(id);
        return converter.convertFrom(
                repository.save(converter.convertFrom(dto))
        );
    }
}
