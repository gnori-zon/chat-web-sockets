package org.gnori.chatwebsockets.api.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Endpoint {

    public static final String TARGET_ID = "{targetId}";
    public static final String ADD_PATH = ":add";
    public static final String DELETE_PATH = ":delete";

    public static final String CHAT_ROOMS = "/chat-rooms";
    public static final String CHAT_ROOM_WITH_ID = CHAT_ROOMS + "/" + TARGET_ID;
    public static final String USERS = "/users";
}
