package pl.lodz.p.it.ssbd2026.ssbd01.mok.schedulers;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenEnt;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailService;

import java.time.LocalDateTime;
import java.util.List;

import static pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenType.ACCOUNT_UNBLOCK;

@Component
@RequiredArgsConstructor
public class AccountScheduler {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final OneTimeTokenRepository tokenRepository;

    @Value("${app.account.unverified.expiration.hours:24}")
    private int expirationHours;

    @Value("${app.account.inactive.expiration.days}")
    private int inactiveExpirationDays;

    @Value("${app.account.inactive.expiresat.days}")
    private int expiresAtDays;

    @Scheduled(fixedDelayString = "${app.scheduler.check.interval.ms}")
    @Transactional(transactionManager = "mokTransactionManager")
    @PreAuthorize("permitAll()")
    public void processInactiveAccounts() {
        LocalDateTime now = LocalDateTime.now();
        int reminderHours = expirationHours / 2;

        accountRepository.findAllByIsVerifiedFalseAndSecondEmailSentFalseAndCreationDateBefore(now.minusHours(reminderHours))
                .forEach(acc -> {
                    String lang = acc.getLanguageVersion().getLanguage();
                    emailService.sendActivationReminderEmail(acc.getEmail(), acc.getActivationToken(), lang);
                    acc.setSecondEmailSent(true);
                });

        accountRepository.findAllByIsVerifiedFalseAndCreationDateBefore(now.minusHours(expirationHours))
                .forEach(acc -> {
                    String lang = acc.getLanguageVersion().getLanguage();
                    emailService.sendAccountDeletedEmail(acc.getEmail(), lang);
                    accountRepository.delete(acc);
                });
    }

    @Scheduled(fixedDelayString = "${app.scheduler.check.interval.ms}")
    @Transactional(transactionManager = "mokTransactionManager")
    @PreAuthorize("permitAll()")
    public void unblockAccountsAfter24Hours() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<Account> accountsToUnblock = accountRepository
                .findByIsBlockedTrueAndFailedAttemptsGreaterThanEqualAndLastLoginFailureDateTimeBefore(3, threshold);

        accountsToUnblock.forEach(acc -> {
            String lang = acc.getLanguageVersion().getLanguage();
            acc.setIsBlocked(false);
            acc.setFailedAttempts(0);
            emailService.sendAccountIsUnblockedInfo(acc.getEmail(), acc.getLogin(), lang);
        });

        accountRepository.saveAllAndFlush(accountsToUnblock);
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(transactionManager = "mokTransactionManager")
    public void blockInactiveAccounts() {
        LocalDateTime lastTimeLogged = LocalDateTime.now().minusDays(inactiveExpirationDays);
        List<Account> inactiveAccounts = accountRepository.findInactiveAccountsToBlock(lastTimeLogged);

        inactiveAccounts.forEach(acc -> {
            acc.setIsBlocked(true);

            String generateUrlToken = java.util.UUID.randomUUID().toString();
            OneTimeTokenEnt token = new OneTimeTokenEnt();
            token.setTokenValue(generateUrlToken);
            token.setUsername(acc.getLogin());
            token.setExpiresAt(LocalDateTime.now().plusDays(expiresAtDays));
            token.setTokenType(ACCOUNT_UNBLOCK);
            token.setUsed(false);

            tokenRepository.save(token);

            String lang = acc.getLanguageVersion().getLanguage();
            emailService.sendUnblockEmailLinkAfterInactivity(acc.getEmail(), acc.getLogin(), generateUrlToken, lang);
        });

        accountRepository.saveAllAndFlush(inactiveAccounts);
    }
}