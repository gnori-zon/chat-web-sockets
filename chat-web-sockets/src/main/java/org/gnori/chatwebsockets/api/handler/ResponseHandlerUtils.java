package org.gnori.chatwebsockets.api.handler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseHandlerUtils {

    public static String contentTypeWithCharset(MediaType type, Charset charset) {
        return String.format("%s;charset=%s", type, charset);
    }
}
