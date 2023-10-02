package org.gnori.chatwebsockets.core.service.security.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityUtil {

    public static CustomUserDetails convertFrom(Principal user) {

        return Objects.requireNonNull(
                (CustomUserDetails)((UsernamePasswordAuthenticationToken) user).getPrincipal()
        );
    }
}
