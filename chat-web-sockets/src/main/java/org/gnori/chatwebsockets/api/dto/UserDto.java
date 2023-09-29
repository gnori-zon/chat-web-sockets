package org.gnori.chatwebsockets.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.core.domain.user.enums.Role;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto implements AbstractDto<Long> {

    Long id;

    String username;

    String name;

    String email;

    List<Role> roles;
}
