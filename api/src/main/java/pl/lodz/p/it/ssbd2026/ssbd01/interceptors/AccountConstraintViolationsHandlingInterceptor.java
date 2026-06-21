package pl.lodz.p.it.ssbd2026.ssbd01.interceptors;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountConstraintViolationException;

/**
 * Przechwytuje błędy związane z brakiem unikalności bazy danych (DataIntegrityViolationException)
 * np. podwójny adres email i zamienia je na wyjątek aplikacyjny z odpowiednią informacją.
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@Component
public class AccountConstraintViolationsHandlingInterceptor {

    @AfterThrowing(pointcut = "Pointcuts.allRepositoryMethods()", throwing = "dive")
    public void handleDataIntegrityViolationException(DataIntegrityViolationException dive) {

        String errorMessage = dive.getMessage();

        if (errorMessage != null && errorMessage.contains(Account.UNIQUE_LOGIN_INDEX_NAME)) {
            throw new AccountConstraintViolationException("error.account.login.exists");
        } else if (errorMessage != null && errorMessage.contains(Account.UNIQUE_EMAIL_INDEX_NAME)) {
            throw new AccountConstraintViolationException("error.account.email.exists");
        } else {
            throw new AccountConstraintViolationException(dive);
        }
    }
}