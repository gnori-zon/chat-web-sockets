package org.gnori.chatwebsockets.api.converter;

import org.gnori.chatwebsockets.api.dto.AbstractDto;

import java.util.List;

public interface AbstractConverter<D extends AbstractDto<?>, E> {

    E convertFrom(D dto);
    D convertFrom(E entity);

    default List<D> convertAll(List<E> entities) {
        return entities.stream()
                .map(this::convertFrom)
                .toList();
    }

    default List<E> covertAll(List<D> dtos) {
        return dtos.stream()
                .map(this::convertFrom)
                .toList();
    }
}
