package org.gnori.chatwebsockets.core.service.message.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.gnori.chatwebsockets.api.controller.user.admin.payload.AdminUserPayload;
import org.gnori.chatwebsockets.api.dto.ChatRoomDto;
import org.gnori.chatwebsockets.api.dto.UserDto;
import org.gnori.chatwebsockets.core.service.message.BaseAopMessenger;
import org.gnori.chatwebsockets.core.service.security.CustomUserDetails;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import static org.gnori.chatwebsockets.api.constant.Endpoint.TOPIC_ADMIN_USER;
import static org.gnori.chatwebsockets.api.constant.Endpoint.TOPIC_USER;

@Aspect
@Component
public class AopUserMessenger extends BaseAopMessenger {

    public AopUserMessenger(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate);
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.UserServiceImpl.get(..))")
    public void getOneUser() {
    }

    @Around("getOneUser()")
    public Object sendOneUser(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final String username = extractUserFrom(args, 0)
                            .getUsername();

                    sendToUserTopic(returningValue, username);
                }
        );
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.UserServiceImpl.update(..))")
    public void updateUser() {
    }

    @Around("updateUser()")
    public Object sendAfterUpdateUser(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final String username = extractUserFrom(args, 1)
                            .getUsername();

                    sendToUserTopic(returningValue, username);
                }
        );
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.UserServiceImpl.changePassword(..))")
    public void changePassword() {
    }

    @Around("changePassword()")
    public Object sendAfterChangePassword(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {
                    final String username = extractUserFrom(args, 1)
                            .getUsername();

                    sendToUserTopic(returningValue, username);
                }
        );
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.UserServiceImpl.adminGet(..))")
    public void getOneUserForAdmin() {
    }

    @Around("getOneUserForAdmin()")
    public Object sendOneUserForAdmin(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final String username = extractUserFrom(args, 1)
                            .getUsername();

                    sendToAdminTopic(returningValue, username);
                }
        );
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.UserServiceImpl.adminCreate(..))")
    public void adminCreateUser() {
    }

    @Around("adminCreateUser()")
    public Object sendAfterAdminCreateUser(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final String username = extractUserFrom(args, 1)
                            .getUsername();

                    sendToAdminTopic(returningValue, username);
                }
        );
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.UserServiceImpl.adminUpdate(..))")
    public void adminUpdateUser() {
    }

    @Around("adminUpdateUser()")
    public Object sendAfterAdminUpdateUser(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final UserDto userDto = convertFrom(returningValue);
                    final String username = extractUserFrom(args, 1)
                            .getUsername();


                    sendToAdminTopic(userDto, username);
                    sendToUserTopic(userDto, userDto.getUsername());
                }
        );
    }

    @Pointcut(value = "execution(* org.gnori.chatwebsockets.core.service.domain.impl.UserServiceImpl.adminDelete(..))")
    public void adminDeleteUser() {
    }

    @Around("adminDeleteUser()")
    public Object sendAfterAdminDeleteUser(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        return invokeAfterProceed(
                proceedingJoinPoint,
                (args, returningValue) -> {

                    final UserDto userDto = convertFrom(returningValue);
                    final String username = extractUserFrom(args, 1)
                            .getUsername();

                    sendToAdminTopic(userDto, username);
                    sendToUserTopic(userDto, userDto.getUsername());
                }
        );
    }

    private <T> void sendToAdminTopic(T objectToSend, Object... argsToPattern) {
        send(objectToSend, TOPIC_ADMIN_USER, argsToPattern);
    }

    private <T> void sendToUserTopic(T objectToSend, Object... argsToPattern) {
        send(objectToSend, TOPIC_USER, argsToPattern);
    }

    private UserDto convertFrom(Object object) {
        return (UserDto) object;
    }

    private CustomUserDetails extractUserFrom(Object[] args, int numberPosition) {
        return extractFromArgs(args, numberPosition, CustomUserDetails.class);
    }
}
