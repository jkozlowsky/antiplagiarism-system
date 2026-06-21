package pl.lodz.p.it.ssbd2026.ssbd01.interceptors;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;

@RequiredArgsConstructor
public class TransactionSynchronizationLogger implements TransactionSynchronization {

    private static final Logger log = LoggerFactory.getLogger(TransactionSynchronizationLogger.class);

    public enum TransactionSynchronizationStatus { COMMITTED, ROLLED_BACK, UNKNOWN };

    @Getter
    @NonNull
    private String transactionId;

    @Override
    public void beforeCommit(boolean readOnly) {
        log.info("Transaction synchronization: {} goes to commit as {}", transactionId, readOnly?"RO":"RW");
    }

    @Override
    public void afterCompletion(int status) {
        log.info("Transaction synchronization: {} is after completion with: {}", transactionId, TransactionSynchronizationStatus.values()[status]);
    }
}