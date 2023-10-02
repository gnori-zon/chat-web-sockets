package org.gnori.chatwebsockets.core.service.message;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.function.BiConsumer;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BaseAopMessenger {

    SimpMessagingTemplate simpMessagingTemplate;

    protected <T> T extractFromArgs(Object[] args, int numberPosition, Class<T> clazz) {
        return clazz.cast(args[numberPosition]);
    }

    protected <T> void send(T objectToSend, String topicPattern, Object... argsToPattern) {

        simpMessagingTemplate.convertAndSend(
                String.format(topicPattern, argsToPattern),
                objectToSend
        );
    }

    protected Object invokeAfterProceed(
            ProceedingJoinPoint proceedingJoinPoint,
            BiConsumer<Object[], Object> biConsumer
    ) throws Throwable {

        final Object[] args = proceedingJoinPoint.getArgs();
        final Object returningValue = proceedingJoinPoint.proceed(args);

        try {
            return returningValue;
        } finally {
            biConsumer.accept(args, returningValue);
        }
    }
}
