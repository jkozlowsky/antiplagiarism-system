package pl.lodz.p.it.ssbd2026.ssbd01.interceptors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

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
                message.append("| ").append(joinPoint.getSignature().toShortString())
                        .append(":").append(Thread.currentThread().threadId());
                message.append(" | User: ").append(username);
                message.append(" | Args: ");
                if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
                    String[] parameterNames = methodSignature.getParameterNames();
                    Object[] args = joinPoint.getArgs();

                    for (int i = 0; i < args.length; i++) {
                        String paramName = (parameterNames != null && parameterNames.length > i) ? parameterNames[i].toLowerCase() : "";
                        Object arg = args[i];

                        if (paramName.contains("password") || paramName.contains("haslo") || paramName.contains("token")) {
                            message.append("*** UKRYTE *** ");
                        } else {
                            String argStr = (arg != null) ? arg.toString() : "null";
                            argStr = argStr.replaceAll("(?i)((?:password|haslo|token|idToken)\\s*=\\s*)[^,\\}\\]]+", "$1*** UKRYTE ***");
                            message.append(argStr).append(" ");
                        }
                    }
                } else {
                    for (Object arg : joinPoint.getArgs()) {
                        message.append(arg).append(" ");
                    }
                }
            } catch (Exception e) {
                log.error("| Unexpected exception within interceptor: ", e);
                throw e;
            }

            result = joinPoint.proceed();

        } catch (Throwable t) {
            message.append(" | Thrown exception: ").append(t.getClass().getSimpleName()).append(" - ").append(t.getMessage());

            if (t instanceof AppBaseException) {
                log.warn(message.toString());
            } else {
                log.error(message.toString(), t);
            }
            throw t;
        }

        if (result != null) {
            message.append(" | Returned type: ").append(result.getClass().getSimpleName());
            // message.append(" | Returned: ").append(result); // AVOID THIS FOR ENTITIES
        } else {
            message.append(" | Returned: null");
        }
        log.info(message.toString());

        return result;
    }
}