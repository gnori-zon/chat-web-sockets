package org.gnori.chatwebsockets.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRoomDto implements AbstractDto<String> {

    String id;

    @JsonProperty("name")
    String name;

    @JsonProperty("ownerUsername")
    String ownerUsername;

    @JsonProperty("description")
    String description;

    List<UserDto> connectedUsers;
}
