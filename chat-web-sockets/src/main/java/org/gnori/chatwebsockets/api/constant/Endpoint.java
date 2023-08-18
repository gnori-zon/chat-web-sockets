package org.gnori.chatwebsockets.api.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Endpoint {

    public static final String START_PAGE_PATH = "/";
    public static final String MAIN_PAGE_PATH = "/main";
    public static final String LOGOUT_PATH = "/main";

    public static final String ADD_PATH = ":add";
    public static final String CREATE_PATH = ":create";
    public static final String UPDATE_PATH = ":update";
    public static final String DELETE_PATH = ":delete";
    public static final String LIST_PATH = ":list";
    public static final String ONE_PATH = ":one";
    public static final String SEND_PATH = ":send";
    public static final String CHANGE_PASS_PATH = ":change-password";
    public static final String SIGN_IN_PATH = ":sign-in";
    public static final String SIGN_UP_PATH = ":sign-up";

    public static final String OLD = "/old";
    public static final String CHAT_ROOMS = "/chat-rooms";
    public static final String USERS = "/users";
    public static final String ADMIN_USERS = "/admin/users";
    public static final String MESSAGES = "/messages";
    public static final String OLD_MESSAGES = OLD + MESSAGES;
    public static final String UPDATE_MESSAGES = "/update" + MESSAGES;


    public static final String TOPIC = "/topic";
    public static final String TOPIC_USER_CHAT_ROOMS = TOPIC + "/%s" + CHAT_ROOMS;

    public static final String TOPIC_ADMIN_USER = TOPIC + "/admin/%s" + USERS;
    public static final String TOPIC_USER = TOPIC + "/%s" + USERS;

    public static final String TOPIC_CHAT_ROOM_MESSAGES = TOPIC + "/%s" + MESSAGES;
    public static final String TOPIC_CHAT_ROOM_OLD_MESSAGES = TOPIC + "/%s" + OLD_MESSAGES;
    public static final String TOPIC_CHAT_ROOM_UPDATE_MESSAGES = TOPIC + "/%s" + UPDATE_MESSAGES;
}
