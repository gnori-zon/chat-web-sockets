package org.gnori.chatwebsockets.api.controller.chatroom.payload;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class ChatRoomPayload {
    String chatRoomId;
}
