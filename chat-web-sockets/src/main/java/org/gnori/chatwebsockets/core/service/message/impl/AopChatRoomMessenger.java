package org.gnori.chatwebsockets.core.service.message.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.gnori.chatwebsockets.api.controller.chatroom.user.UserChatRoomPayload;
import org.gnori.chatwebsockets.api.dto.ActionType;
import org.gnori.chatwebsockets.api.dto.ChatRoomDto;
import org.gnori.chatwebsockets.core.service.message.BaseAopMessenger;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import static org.gnori.chatwebsockets.api.constant.Endpoint.TOPIC_USER_CHAT_ROOMS;

@Aspect
@Component
public class AopChatRoomMessenger extends BaseAopMessenger {

    public AopChatRoomMessenger(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate);
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.ChatRoomServiceImpl.getAll(..))")
    public void getAllChats() {
    }

    @Around("getAllChats()")
    public Object sendAllChats(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final CustomUserDetails userDetails = extractUserDetails(args, 0);
                    sendToUserChatRoomsTopic(returningValue, userDetails.getUsername());
                }
        );
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.ChatRoomServiceImpl.get(..))")
    public void getOneChats() {
    }

    @Around("getOneChats()")
    public Object sendOneChat(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final CustomUserDetails userDetails = extractUserDetails(args, 1);
                    sendToUserChatRoomsTopic(returningValue, userDetails.getUsername());
                }
        );
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.ChatRoomServiceImpl.create(..))")
    public void createChat() {
    }

    @Around("createChat()")
    public Object sendAfterCreate(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final CustomUserDetails userDetails = extractUserDetails(args, 1);
                    sendToUserChatRoomsTopic(returningValue, userDetails.getUsername());
                }
        );
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.ChatRoomServiceImpl.update(..))")
    public void updateChat() {
    }

    @Around("updateChat()")
    public Object sendAfterUpdate(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final ChatRoomDto chatRoomDto = convertFrom(returningValue);

                    chatRoomDto.getConnectedUsers()
                            .forEach(userDto -> sendToUserChatRoomsTopic(chatRoomDto, userDto.getUsername()));
                }
        );
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.ChatRoomServiceImpl.delete(..))")
    public void deleteChat() {
    }

    @Around("deleteChat()")
    public Object sendAfterDelete(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final ChatRoomDto chatRoomDto = convertFrom(returningValue);
                    final ChatRoomDto emptyChatRoomDto = new ChatRoomDto(chatRoomDto.getId(), ActionType.DELETE);

                    chatRoomDto.getConnectedUsers()
                            .forEach(userDto -> sendToUserChatRoomsTopic(emptyChatRoomDto, userDto.getUsername()));
                }
        );
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.ChatRoomServiceImpl.addUser(..))")
    public void addUserToChat() {
    }

    @Around("addUserToChat()")
    public Object sendAfterAddUserToChat(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final UserChatRoomPayload payload = extractUserChatRoomPayload(args, 0);
                    final ChatRoomDto chatRoomDto = convertFrom(returningValue);
                    chatRoomDto.getConnectedUsers()
                            .forEach(userDto -> {
                                if (userDto.getUsername().equals(payload.getUsername())) {

                                    final ActionType oldActionType = chatRoomDto.getActionType();

                                    chatRoomDto.setActionType(ActionType.GET);
                                    sendToUserChatRoomsTopic(chatRoomDto, userDto.getUsername());
                                    chatRoomDto.setActionType(oldActionType);
                                } else {
                                    sendToUserChatRoomsTopic(chatRoomDto, userDto.getUsername());
                                }
                            });
                }
        );
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.ChatRoomServiceImpl.deleteUser(..))")
    public void deleteUserFromChat() {
    }

    @Around("deleteUserFromChat()")
    public Object sendAfterDeleteUserFromChat(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final UserChatRoomPayload payload = extractUserChatRoomPayload(args, 0);
                    final ChatRoomDto chatRoomDto = convertFrom(returningValue);
                    final ChatRoomDto emptyChatRoomDto = new ChatRoomDto(chatRoomDto.getId(), ActionType.DELETE);

                    chatRoomDto.getConnectedUsers()
                            .forEach(userDto -> sendToUserChatRoomsTopic(chatRoomDto, userDto.getUsername()));
                    sendToUserChatRoomsTopic(emptyChatRoomDto, payload.getUsername());
                }
        );
    }

    private ChatRoomDto convertFrom(Object object) {
        return (ChatRoomDto) object;
    }

    private UserChatRoomPayload extractUserChatRoomPayload(Object[] args, int numberPosition) {
        return extractFromArgs(args, numberPosition, UserChatRoomPayload.class);
    }

    private CustomUserDetails extractUserDetails(Object[] args, int numberPosition) {
        return extractFromArgs(args, numberPosition, CustomUserDetails.class);
    }

    private <T> void sendToUserChatRoomsTopic(T objectToSend, Object... argsToPattern) {
        send(objectToSend, TOPIC_USER_CHAT_ROOMS, argsToPattern);
    }

}
