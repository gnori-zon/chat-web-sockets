package org.gnori.chatwebsockets.api.controller.message.payload;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class NewMessagePayload extends MessagePayload {
    String text;
}
