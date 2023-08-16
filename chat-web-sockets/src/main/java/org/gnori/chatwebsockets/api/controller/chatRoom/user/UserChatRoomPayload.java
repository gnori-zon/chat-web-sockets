package org.gnori.chatwebsockets.api.controller.chatroom.user;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.api.controller.chatroom.payload.ChatRoomPayload;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class UserChatRoomPayload extends ChatRoomPayload {
    String username;
}
