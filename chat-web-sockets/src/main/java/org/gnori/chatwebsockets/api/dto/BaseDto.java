package org.gnori.chatwebsockets.api.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class BaseDto<ID> implements AbstractDto<ID> {

    ActionType actionType;

    public <DTO extends BaseDto<ID>> DTO withActionType(ActionType actionType) throws ClassCastException{
        this.actionType = actionType;

        return (DTO) this;
    }
}
