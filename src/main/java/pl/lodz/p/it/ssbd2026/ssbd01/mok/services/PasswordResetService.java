package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.OneTimeTokenConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenEnt;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenType;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.HashEncryptionException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.OneTimeTokenInvalidException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.schedulers.TokenScheduler;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final OneTimeTokenRepository tokenRepository;
    private final AccountRepository accountRepository;

    private final EmailService emailService;
    private final TokenScheduler tokenService;

    @PersistenceContext
    private EntityManager entityManager;

    private final PasswordEncoder passwordEncoder;

    private final PostCommitTransactionManager postCommitTransactionManager;

    /**
     * Żeby zapobiec zaśmieceniu bazy danych utworzono TokenScheduler który czyści martwe tokeny co minutę.
     * @param email: adres email na który będzie wysłany jednorazowy adres url
     */
    public void initiatePasswordReset(String email) {
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isEmpty()) {
            //Po prostu zwracamy bo użytkownik nie powinien wiedzieć czy podany mail jest w bazie.
            return;
        }

        List<OneTimeTokenEnt> potentialClashedToken = tokenRepository.findByUsernameAndTokenTypeAndIsUsedIsFalse(email, OneTimeTokenType.PASSWORD_RESET);
        potentialClashedToken.forEach(item -> item.setUsed(true));

        if (!potentialClashedToken.isEmpty()) {
            tokenRepository.saveAllAndFlush(potentialClashedToken);
        }

        OneTimeTokenEnt token = new OneTimeTokenEnt();
        String rawToken = UUID.randomUUID().toString();
        token.setTokenValue(hashToken(rawToken));
        token.setUsername(email);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(OneTimeTokenConsts.EXPIRATION_TIME_MINUTES));
        token.setTokenType(OneTimeTokenType.PASSWORD_RESET);
        token.setUsed(false);
        tokenRepository.saveAndFlush(token);

        emailService.sendResetPasswordEmail(email, rawToken, accountOpt.get().getLanguageVersion().getLanguage());
    } //TODO: i18n

    /**
     * @param passwordHash: skrót nowego hasła użtkownika
     * @param token:        token który użytkownik otrzymał w powiadomieniu na mail
     */
    public void resolvePasswordReset(String token, String passwordHash) {
        String hashedTokenInput = hashToken(token);
        OneTimeTokenEnt tokenEntity = tokenRepository
                .findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(hashedTokenInput, OneTimeTokenType.PASSWORD_RESET, LocalDateTime.now())
                .orElseThrow(() -> new OneTimeTokenInvalidException("error.token.reset.expired"));
        //TODO: i18n

        tokenEntity.setUsed(true);
        //Nie rozumiem dlaczego, ale jeżeli robię manualny save albo saveAndFlush
        //to występuje optimisticLockException którego nie mogę obejść.
        //Na razie zostawię je tutaj zakomentowane, ale muszę zapytać kogoś z doktorów co tu się
        // dzieje, bo prawie zwariowałem zanim naprawiłem
//        tokenRepository.save(tokenEntity);

        Account account = accountRepository.findByEmail(tokenEntity.getUsername())
                .orElseThrow(() -> new AccountNotFoundException("Account associated with that email is not found"));
//        entityManager.refresh(account);
        account.setPasswordHash(passwordEncoder.encode(passwordHash));
//        account.setTokenVersion(account.getTokenVersion() + 1);
//        accountRepository.save(account);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(OneTimeTokenConsts.HASHING_ALGORITHM);
            byte[] encodedHash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return getStringFromByteArray(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new HashEncryptionException("Algorithm " + OneTimeTokenConsts.HASHING_ALGORITHM + " does not exist", e);
        }
    }

    private static String getStringFromByteArray(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
