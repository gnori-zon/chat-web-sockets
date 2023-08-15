package org.gnori.chatwebsockets.api.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Endpoint {

    public static final String ADD_PATH = ":add";
    public static final String CREATE_PATH = ":create";
    public static final String UPDATE_PATH = ":update";
    public static final String DELETE_PATH = ":delete";
    public static final String LIST_PATH = ":list";
    public static final String ONE_PATH = ":one";
    public static final String CHANGE_PASS_PATH = ":change-password";

    public static final String CHAT_ROOMS = "/chat-rooms";
    public static final String USERS = "/users";
    public static final String ADMIN_USERS = "/admin/users";

    public static final String TOPIC = "/topic";
    public static final String TOPIC_USER_CHAT_ROOMS = TOPIC + "/%s" + CHAT_ROOMS;

    public static final String TOPIC_ADMIN_USER = TOPIC + "/admin/%s" + USERS;
    public static final String TOPIC_USER = TOPIC + "/%s" + USERS;
}
