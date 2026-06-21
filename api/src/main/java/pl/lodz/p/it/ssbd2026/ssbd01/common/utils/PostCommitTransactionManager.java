package pl.lodz.p.it.ssbd2026.ssbd01.common.utils;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/**
 * Komponent pozwalający na odroczenie wykonania akcji do momentu pomyślnego zatwierdzenia bieżącej transakcji.
 */
@Component
public class PostCommitTransactionManager {
    /**
     * Metoda rejestrująca akcję do wykonania po pomyślnym zakończeniu bieżącej transakcji.
     * @param action kod opakowany w obiekt Runnable, który ma zostać wykonany po zatwierdzeniu transakcji.
     * @throws IllegalStateException jeżeli w momencie wywołania metody, nie jest aktywna żadna transakcja.
     */
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
