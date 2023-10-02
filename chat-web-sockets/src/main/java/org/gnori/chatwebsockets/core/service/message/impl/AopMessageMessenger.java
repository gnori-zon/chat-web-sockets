package org.gnori.chatwebsockets.core.service.message.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.gnori.chatwebsockets.api.controller.message.payload.MessagePayload;
import org.gnori.chatwebsockets.core.service.message.BaseAopMessenger;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import static org.gnori.chatwebsockets.api.constant.Endpoint.TOPIC_CHAT_ROOM_MESSAGES;
import static org.gnori.chatwebsockets.api.constant.Endpoint.TOPIC_CHAT_ROOM_OLD_MESSAGES;

@Aspect
@Component
public class AopMessageMessenger extends BaseAopMessenger {

    public AopMessageMessenger(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate);
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.MessageServiceImpl.getAll(..))")
    public void getAllMessages() {}

    @Around("getAllMessages()")
    public Object sendAllMessages(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final MessagePayload payload = extractMessagePayloadFrom(args, 0);
                    final CustomUserDetails userDetails = extractUserDetailsFrom(args, 1);

                    sendToOldMessagesTopic(returningValue, payload.getChatRoomId(), userDetails.getUsername());
                }
        );
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.MessageServiceImpl.create(..))")
    public void createMessage() {}

    @Around("createMessage()")
    public Object sendAfterCreate(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final MessagePayload payload = extractMessagePayloadFrom(args, 0);
                    sendToMessagesTopic(returningValue, payload.getChatRoomId());
                }
        );
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.MessageServiceImpl.update(..))")
    public void updateMessage() {}

    @Around("updateMessage()")
    public Object sendAfterUpdate(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final MessagePayload payload = extractMessagePayloadFrom(args, 0);
                    sendToMessagesTopic(returningValue, payload.getChatRoomId());
                }
        );
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.MessageServiceImpl.delete(..))")
    public void deleteMessage() {}

    @Around("deleteMessage()")
    public Object sendAfterDelete(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final MessagePayload payload = extractMessagePayloadFrom(args, 0);
                    sendToMessagesTopic(returningValue, payload.getChatRoomId());
                }
        );
    }

    private <T> void sendToOldMessagesTopic(T objectToSend, Object... argsToPattern) {
        send(objectToSend, TOPIC_CHAT_ROOM_OLD_MESSAGES, argsToPattern);
    }

    private <T> void sendToMessagesTopic(T objectToSend, Object... argsToPattern) {
        send(objectToSend, TOPIC_CHAT_ROOM_MESSAGES, argsToPattern);
    }

    private CustomUserDetails extractUserDetailsFrom(Object[] args, int numberPosition) {
        return extractFromArgs(args, numberPosition, CustomUserDetails.class);
    }
    private MessagePayload extractMessagePayloadFrom(Object[] args, int numberPosition) {
        return extractFromArgs(args, numberPosition, MessagePayload.class);
    }
}
