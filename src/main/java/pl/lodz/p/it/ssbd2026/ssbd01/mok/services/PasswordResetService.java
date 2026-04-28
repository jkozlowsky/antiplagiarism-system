package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.OneTimeTokenConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenEnt;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.OneTimeTokenInvalidException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final OneTimeTokenRepository tokenRepository;
    private final UserRepository userRepository;

    private final EmailService emailService;
    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * @param email: adres email na który będzie wysłany jednorazowy adres url
     */
    public void initiatePasswordReset(String email) {
        if (userRepository.findByEmail(email).isEmpty()){
            //Po prostu zwracamy bo użytkownik nie powinien wiedzieć czy podany mail jest w bazie.
            return;
        }


        List<OneTimeTokenEnt> potentialClashedToken = tokenRepository.findByUsernameAndIsUsedIsFalse(email);
        potentialClashedToken.forEach(item -> item.setUsed(true));
        if (!potentialClashedToken.isEmpty()){
            tokenRepository.saveAllAndFlush(potentialClashedToken);
        }

        OneTimeTokenEnt token = new OneTimeTokenEnt();
        token.setTokenValue(UUID.randomUUID().toString());
        token.setUsername(email);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(OneTimeTokenConsts.EXPIRATION_TIME_MINUTES));
        token.setUsed(false);
        tokenRepository.saveAndFlush(token);

        String magicLink = frontendUrl + "/reset-password?token=" + token.getTokenValue();
//        log.info(magicLink);
        emailService.sendEmail(email, "Password Reset", "Click to reset: " + magicLink);
    } //TODO: i18n dla maila tutaj

    /**
     * @param passwordHash: skrót nowego hasła użtkownika
     * @param token: token który użytkownik otrzymał w powiadomieniu na mail
     */
    public void resolvePasswordReset(String token, String passwordHash) {
        OneTimeTokenEnt tokenEntity = tokenRepository
                .findByTokenValueAndIsUsedIsFalseAndExpiresAtAfter(token, LocalDateTime.now())
                .orElseThrow(() -> new OneTimeTokenInvalidException("Time to reset password has expired or the token is used"));

        tokenEntity.setUsed(true);
        tokenRepository.saveAndFlush(tokenEntity);
        //TODO: wyliczać skrót z haseł
//        String encodedPassword = passwordEncoder.encode(newPassword);
//        userService.updatePassword(token.getUsername(), encodedPassword);
    }
}
