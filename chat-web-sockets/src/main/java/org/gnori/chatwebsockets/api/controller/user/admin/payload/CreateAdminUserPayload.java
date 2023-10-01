package org.gnori.chatwebsockets.api.controller.user.admin.payload;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.user.user.payload.UpdateUserPayload;
import org.gnori.chatwebsockets.core.domain.user.enums.Role;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class CreateAdminUserPayload extends UpdateAdminUserPayload {
    String password;
}
