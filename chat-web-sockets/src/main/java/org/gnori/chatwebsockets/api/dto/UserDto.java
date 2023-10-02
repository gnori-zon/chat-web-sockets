package org.gnori.chatwebsockets.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.core.domain.user.enums.Role;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto extends BaseDto<Long> {

    Long id;
    String username;
    String name;
    String email;
    List<Role> roles;
}
