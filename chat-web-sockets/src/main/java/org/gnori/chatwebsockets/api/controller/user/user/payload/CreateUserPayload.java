package org.gnori.chatwebsockets.api.controller.user.user.payload;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class CreateUserPayload extends UpdateUserPayload {
    String username;
    String password;
}
