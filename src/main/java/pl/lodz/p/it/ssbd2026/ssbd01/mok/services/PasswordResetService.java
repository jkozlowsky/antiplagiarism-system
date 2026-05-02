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
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.OneTimeTokenConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenEnt;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.HashEncryptionException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.OneTimeTokenInvalidException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final OneTimeTokenRepository tokenRepository;
    private final AccountRepository accountRepository;

    private final EmailService emailService;
    private final EmailTokenService tokenService;

    @Value("${app.frontend.url}")
    private String frontendUrl;
    @PersistenceContext
    private EntityManager entityManager;

    private final PasswordEncoder passwordEncoder;

    /**
     * @param email: adres email na który będzie wysłany jednorazowy adres url
     */
    public void initiatePasswordReset(String email) {
        if (accountRepository.findByEmail(email).isEmpty()) {
            //Po prostu zwracamy bo użytkownik nie powinien wiedzieć czy podany mail jest w bazie.
            return;
        }

        List<OneTimeTokenEnt> potentialClashedToken = tokenRepository.findByUsernameAndIsUsedIsFalse(email);
        potentialClashedToken.forEach(item -> item.setUsed(true));
        if (!potentialClashedToken.isEmpty()) {
            tokenRepository.saveAllAndFlush(potentialClashedToken);
        }

        OneTimeTokenEnt token = new OneTimeTokenEnt();
        String rawToken = UUID.randomUUID().toString();
        token.setTokenValue(hashToken(rawToken));
        token.setUsername(email);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(OneTimeTokenConsts.EXPIRATION_TIME_MINUTES));
        token.setUsed(false);
        tokenRepository.saveAndFlush(token);

        String magicLink = frontendUrl + "reset-password?token=" + rawToken;
        log.info(magicLink);
        emailService.sendEmail(email, "Password Reset", "Click to reset: " + magicLink);
    } //TODO: i18n

    /**
     * @param passwordHash: skrót nowego hasła użtkownika
     * @param token:        token który użytkownik otrzymał w powiadomieniu na mail
     */
    public void resolvePasswordReset(String token, String passwordHash) {
        String hashedTokenInput = hashToken(token);
        OneTimeTokenEnt tokenEntity = tokenRepository
                .findByTokenValueAndIsUsedIsFalseAndExpiresAtAfter(hashedTokenInput, LocalDateTime.now())
                .orElseThrow(() -> new OneTimeTokenInvalidException("Time to reset password has expired or the token is used"));
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

            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new HashEncryptionException("Algorithm " + OneTimeTokenConsts.HASHING_ALGORITHM + " does not exist", e);
        }
    }

}
