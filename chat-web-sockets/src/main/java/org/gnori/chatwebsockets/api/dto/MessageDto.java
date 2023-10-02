package org.gnori.chatwebsockets.api.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.core.domain.message.MessagePrimaryKey;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class MessageDto extends BaseDto<MessagePrimaryKey> {

    MessagePrimaryKey messagePrimaryKey;
    String fromUser;
    String text;

    @Override
    public MessagePrimaryKey getId() {
        return messagePrimaryKey;
    }

    @Override
    public void setId(MessagePrimaryKey messagePrimaryKey) {
        this.messagePrimaryKey = messagePrimaryKey;
    }
}
