package org.gnori.chatwebsockets.core.service.domain;

import org.gnori.chatwebsockets.api.dto.AbstractDto;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;

import java.util.List;

public interface AbstractService<ID, D extends AbstractDto<ID>> {

    List<D> getAll(CustomUserDetails user);
    D getById(ID id, CustomUserDetails user);
    void deleteById(ID id, CustomUserDetails user);
    D create(D dto, CustomUserDetails user);
    D updateById(ID id, D dto, CustomUserDetails user);
}
