package pl.lodz.p.it.ssbd2026.ssbd01.interceptors;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Interceptor rejestrujący logowanie dla metod i klas oznaczonych adnotacją @TransactionLogged.
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class TransactionLoggingInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TransactionLoggingInterceptor.class);

    @Before("Pointcuts.transactionLoggedAnnotatedMethods()")
    public void registerSynchronization() {

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        RetryContext retryContext = RetrySynchronizationManager.getContext();
        String retryInfo = (retryContext != null) ? " (retry # " + retryContext.getRetryCount() + ")" : "";

        final String transactionId = TransactionSynchronizationManager.getCurrentTransactionName()
                + ":" + Thread.currentThread().threadId()
                + retryInfo;

        TransactionSynchronizationLogger synchronizationLogger = new TransactionSynchronizationLogger(transactionId);
        TransactionSynchronizationManager.registerSynchronization(synchronizationLogger);
        log.trace("Transaction synchronization: {} registered", synchronizationLogger.getTransactionId());
    }
}