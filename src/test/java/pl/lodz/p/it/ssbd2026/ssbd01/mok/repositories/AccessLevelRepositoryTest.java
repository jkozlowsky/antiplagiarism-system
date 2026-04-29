package pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevel;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Teacher;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AccessLevelRepositoryTest extends BaseTestSetup {

    @Autowired
    private AccessLevelRepository accessLevelRepository;

    @Autowired
    private AccountTestRepository accountTestRepository;

    @Autowired
    private AccessLevelDictRepository accessLevelDictRepository;

    @Test
    @DisplayName("Zwrócenie obiektu poziomu dostępu, jeśli użytkownik posiada daną rolę - tutaj Teacher")
    void shouldFindAccessLevelByDictAndAccount() {
        Account adminUser = accountTestRepository.findByLogin("admin").get();
        LanguageDict ld = adminUser.getLanguageVersion();

        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        Account account = new Account();
        account.setLogin("repo_" + uniqueId);
        account.setEmail("repo_" + uniqueId + "@example.com");
        account.setPasswordHash("hash");
        account.setName("Name");
        account.setSurname("Surname");
        account.setIsBlocked(false);
        account.setLanguageVersion(ld);
        account = accountTestRepository.save(account);

        AccessLevelDict dict = accessLevelDictRepository.findByAccessLevelName(AccountConsts.ROLE_TEACHER).get();

        Teacher teacherLevel = new Teacher();
        teacherLevel.setAccount(account);
        teacherLevel.setAccessLevelDict(dict);
        accessLevelRepository.save(teacherLevel);

        Optional<AccessLevel> found = accessLevelRepository.findByAccessLevelDictAndAccount(dict, account);

        assertAll(
                () -> assertTrue(found.isPresent()),
                () -> assertInstanceOf(Teacher.class, found.get())
        );
    }

    @Test
    @DisplayName("Zwrócenie pustego wyniku, jeśli użytkownik nie posiada danej roli")
    void shouldReturnEmptyWhenAccessLevelNotAssigned() {
        Account adminUser = accountTestRepository.findByLogin("admin").get();
        LanguageDict ld = adminUser.getLanguageVersion();

        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        Account account = new Account();
        account.setLogin("empty_" + uniqueId);
        account.setEmail("empty_" + uniqueId + "@example.com");
        account.setPasswordHash("hash");
        account.setName("Name");
        account.setSurname("Surname");
        account.setIsBlocked(false);
        account.setLanguageVersion(ld);
        account = accountTestRepository.save(account);

        AccessLevelDict dict = accessLevelDictRepository.findByAccessLevelName(AccountConsts.ROLE_STUDENT).get();

        Optional<AccessLevel> found = accessLevelRepository.findByAccessLevelDictAndAccount(dict, account);

        assertFalse(found.isPresent());
    }
}