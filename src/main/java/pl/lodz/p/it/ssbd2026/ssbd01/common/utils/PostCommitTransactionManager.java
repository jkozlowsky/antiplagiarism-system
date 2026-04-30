package pl.lodz.p.it.ssbd2026.ssbd01.common.utils;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;



@Component
public class PostCommitTransactionManager {
    public void register(Runnable action) {
        if(!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("No active transaction to register post-commit action");
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
