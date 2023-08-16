package org.gnori.chatwebsockets.api.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.core.domain.message.MessagePrimaryKey;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class MessageDto implements AbstractDto<MessagePrimaryKey> {

    MessagePrimaryKey messagePrimaryKey;
    String fromUser;
    String toUser;
    String text;

    @Override
    public MessagePrimaryKey getId() {
        return null;
    }

    @Override
    public void setId(MessagePrimaryKey messagePrimaryKey) {

    }
}
