package org.gnori.chatwebsockets.api.controller.user.user.payload;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.user.payload.UserPayload;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class UpdateUserPayload extends UserPayload {
    String name;
    String email;
}
