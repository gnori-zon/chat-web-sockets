package org.gnori.chatwebsockets.core.domain.chat;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.core.domain.user.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("chat_rooms")
@FieldDefaults(level = AccessLevel.PROTECTED)
public class ChatRoom implements Serializable {

    @Id
    String id;

    String name;

    String description;

    String ownerUsername;

    List<User> connectedUsers = new ArrayList<>();
}
