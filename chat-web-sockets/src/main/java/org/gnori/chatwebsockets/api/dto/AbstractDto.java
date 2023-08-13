package org.gnori.chatwebsockets.api.dto;

public interface AbstractDto<ID> {

    ID getId();
    void setId(ID id);
}
