package org.gnori.chatwebsockets.api.controller.message.payload;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class MessagePayload {
    String chatRoomId;
    LocalDateTime date;
    String fromUser;
}
