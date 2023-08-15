package org.gnori.chatwebsockets.api.controller.user.payload;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPayload {
    String username;
    String oldPassword;
    String newPassword;
    String name;
    String email;
}
