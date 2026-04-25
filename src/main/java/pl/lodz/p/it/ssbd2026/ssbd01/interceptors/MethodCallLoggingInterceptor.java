package pl.lodz.p.it.ssbd2026.ssbd01.interceptors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Interceptor odpowiedzialny za logowanie wejścia do metody, przekazanych argumentów,
 * wyjścia z metody oraz ewentualnych rzuconych wyjątków.
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class MethodCallLoggingInterceptor {

    private static final Logger log = LoggerFactory.getLogger(MethodCallLoggingInterceptor.class);

    @Around("Pointcuts.methodCallLoggedAnnotatedMethods() || Pointcuts.allRepositoryMethods()")
    public Object getOperationInfo(ProceedingJoinPoint joinPoint) throws Throwable {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "--ANONYMOUS--";

        StringBuilder message = new StringBuilder("Method call ");

        if (null != RetrySynchronizationManager.getContext()) {
            message.append("(retry # ").append(RetrySynchronizationManager.getContext().getRetryCount()).append(") ");
        }

        Object result;
        try {
            try {
                message.append("| ").append(joinPoint.getSignature().toLongString())
                        .append(":").append(Thread.currentThread().threadId());
                message.append(" | User: ").append(username);
                message.append(" | Args: ");
                for (Object arg : joinPoint.getArgs()) {
                    message.append(arg).append(" ");
                }
            } catch (Exception e) {
                log.error("| Unexpected exception within interceptor: ", e);
                throw e;
            }

            result = joinPoint.proceed();

        } catch (Throwable t) {
            message.append(" | Thrown exception: ").append(t.toString());
            log.warn(message.toString(), t);
            throw t;
        }

        message.append(" | Returned: ").append(result);
        log.info(message.toString());

        return result;
    }
}