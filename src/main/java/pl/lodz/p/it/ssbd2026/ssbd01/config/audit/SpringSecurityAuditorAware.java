package pl.lodz.p.it.ssbd2026.ssbd01.config.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;

import java.util.Optional;

@Component("auditorProvider")
@RequiredArgsConstructor
public class SpringSecurityAuditorAware implements AuditorAware<Account> {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, transactionManager = "mppTransactionManager", readOnly = true)
    public Optional<Account> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        String currentLogin = authentication.getName();

        return accountRepository.findAuditorByLogin(currentLogin);
    }
}