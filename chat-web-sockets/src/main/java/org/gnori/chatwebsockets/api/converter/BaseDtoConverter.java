package org.gnori.chatwebsockets.api.converter;

import org.gnori.chatwebsockets.api.dto.ActionType;
import org.gnori.chatwebsockets.api.dto.BaseDto;

import java.util.List;
import java.util.stream.Collectors;

public interface BaseDtoConverter<D extends BaseDto<?>, E> extends AbstractConverter<D, E> {

    default D convertWithActionType(E entity, ActionType actionType) {

        return (D) convertFrom(entity)
                .withActionType(actionType);
    }

    default List<D> convertWithActionType(List<E> entities, ActionType actionType) {

        return entities.stream()
                .map(entity -> convertWithActionType(entity, actionType))
                .collect(Collectors.toList());

    }
}
