package org.gnori.chatwebsockets.api.controller.chatroom.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChatRoomPayload extends ChatRoomPayload {

    String name;
    String description;
}
