package org.gnori.chatwebsockets.api.controller.user.admin.payload;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.core.domain.user.enums.Role;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class UpdateAdminUserPayload extends AdminUserPayload {
    String name;
    String email;
    List<Role> roleList;
}
