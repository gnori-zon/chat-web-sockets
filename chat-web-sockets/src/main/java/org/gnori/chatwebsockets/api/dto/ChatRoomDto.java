package org.gnori.chatwebsockets.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRoomDto extends BaseDto<String> {

    String id;
    String name;
    String ownerUsername;
    String description;
    List<UserDto> connectedUsers;

    public ChatRoomDto(String id, ActionType actionType) {
        super(actionType);
        this.id = id;
    }
}
