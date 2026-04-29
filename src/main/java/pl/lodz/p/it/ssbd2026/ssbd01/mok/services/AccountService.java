package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;


import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.InvalidOldPasswordException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.PasswordSameAsOldException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;

    public void changeOwnPassword (String login, String oldPassword, String newPassword ,String ifMatch){

        Account account = accountRepository.findByLogin(login)
                //WYJĄTEK DO POPRAWY
                .orElseThrow(() -> new RuntimeException("error.account.not.found"));

        String currentVersion = String.valueOf(account.getVersion());

        if(!currentVersion.equals(ifMatch)){
            throw new ConcurrentUpdateException(new OptimisticLockException());
        }

        if (!passwordEncoder.matches(oldPassword, account.getPasswordHash())){
            throw new InvalidOldPasswordException();
        }

        //to wgl potrzebne??
        if (!passwordEncoder.matches(newPassword, account.getPasswordHash())){
            throw new PasswordSameAsOldException()  ;
        }

        account.setPasswordHash(passwordEncoder.encode(newPassword));

        accountRepository.saveAndFlush(account);
    }




}
