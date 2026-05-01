package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;

public class NEG08_ChangeOwnPasswordServiceTest extends BaseTestSetup {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Account accountTest;
    private final String OLD_PASSWORD_PLAIN = "PoprawneStareHaslo";
    private final String LOGIN = "negTestLogin";

    @BeforeEach
    public void setup(){

    }



}
