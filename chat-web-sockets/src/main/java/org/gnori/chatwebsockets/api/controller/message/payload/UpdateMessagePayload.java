package org.gnori.chatwebsockets.api.controller.message.payload;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class UpdateMessagePayload extends MessagePayload {
    String text;
}
