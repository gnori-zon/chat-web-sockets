package org.gnori.chatwebsockets.api.controller.user.payload;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class UserPayload {
    String name;
    String email;
}
