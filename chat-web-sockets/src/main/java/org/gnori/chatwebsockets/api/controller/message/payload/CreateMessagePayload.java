package org.gnori.chatwebsockets.api.controller.message.payload;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class CreateMessagePayload extends MessagePayload {
    String text;
}
