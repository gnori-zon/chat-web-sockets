package org.gnori.chatwebsockets.api.controller.user.payload;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.core.domain.user.enums.Role;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserPayload {
    Long id;
    List<Role> roleList;
    UserPayload userPayload;
}
