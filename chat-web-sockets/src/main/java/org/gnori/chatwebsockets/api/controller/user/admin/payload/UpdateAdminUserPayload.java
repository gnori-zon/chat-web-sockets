package org.gnori.chatwebsockets.api.controller.user.admin.payload;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class UpdateAdminUserPayload extends AdminUserPayload {
    String username;
}
