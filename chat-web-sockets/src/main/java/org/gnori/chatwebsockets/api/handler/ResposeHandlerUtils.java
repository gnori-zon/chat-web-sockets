package org.gnori.chatwebsockets.api.handler;

import org.springframework.http.MediaType;

import java.nio.charset.Charset;

public class ResposeHandlerUtils {

    public static String contentTypeWithCharset(MediaType type, Charset charset) {
        return String.format("%s;charset=%s", type, charset);
    }
}
