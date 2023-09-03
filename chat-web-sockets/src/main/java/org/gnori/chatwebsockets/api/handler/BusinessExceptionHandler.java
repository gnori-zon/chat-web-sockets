package org.gnori.chatwebsockets.api.handler;

import lombok.RequiredArgsConstructor;
import org.gnori.chatwebsockets.api.dto.ExceptionDto;
import org.gnori.chatwebsockets.core.exception.BusinessLogicException;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

import static org.gnori.chatwebsockets.api.constant.Endpoint.TOPIC_USER_ERROR;
import static org.gnori.chatwebsockets.core.service.security.util.SecurityUtil.convertFrom;

@RestControllerAdvice
@RequiredArgsConstructor
public class BusinessExceptionHandler {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageExceptionHandler(BusinessLogicException.class)
    public void handleException(
            SimpMessageHeaderAccessor headerAccessor,
            BusinessLogicException ex
    ) {
        Optional.ofNullable(headerAccessor.getSessionAttributes()).ifPresent(
                sessionAttrs -> {
                    final CustomUserDetails user = convertFrom(headerAccessor.getUser());
                    simpMessagingTemplate.convertAndSend(
                            String.format(TOPIC_USER_ERROR, user.getUsername()),
                                new ExceptionDto(ex.getStatus().value(), ex.getMessage())
                            );
                });


    }
}
