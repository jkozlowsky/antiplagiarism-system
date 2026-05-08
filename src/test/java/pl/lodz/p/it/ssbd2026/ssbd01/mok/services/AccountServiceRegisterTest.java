package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.LanguageDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.MokRefreshRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AccountServiceRegisterTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LanguageDictRepository languageDictRepository;

    @Mock
    private AccessLevelDictRepository accessLevelDictRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PostCommitTransactionManager postCommitTransactionManager;

    @InjectMocks
    private AccountService accountService;

    private Account mockAccount;

    @BeforeEach
    void setUp() {
        mockAccount = new Account();
//        mockAccount.setId(UUID.randomUUID());
//        mockAccount.setVersion(1L);
        mockAccount.setEmail("user@example.com");
        mockAccount.setLogin("testUser");
        mockAccount.setPasswordHash("plainTextPassword");
    }

    @Test
    void registerAccount_ShouldSetDefaultsAndSave_WhenValidAccountProvided() {
        LanguageDict mockLangDict = new LanguageDict();
        mockLangDict.setLanguage("pl");

        AccessLevelDict mockStudentDict = new AccessLevelDict();
        mockStudentDict.setAccessLevelName(AccountConsts.ROLE_STUDENT);

        when(passwordEncoder.encode("plainTextPassword")).thenReturn("encodedPasswordHash");
        when(languageDictRepository.findByLanguage("pl")).thenReturn(Optional.of(mockLangDict));
        when(accessLevelDictRepository.findByAccessLevelName(AccountConsts.ROLE_STUDENT)).thenReturn(Optional.of(mockStudentDict));

        accountService.registerAccount(mockAccount);

        assertEquals("encodedPasswordHash", mockAccount.getPasswordHash());
        assertFalse(mockAccount.getIsVerified());
        assertFalse(mockAccount.getIsBlocked());
        assertNotNull(mockAccount.getActivationToken());
        assertNotNull(mockAccount.getCreationDate());
        assertEquals(0, mockAccount.getFailedAttempts());
        assertEquals(mockLangDict, mockAccount.getLanguageVersion());

        assertEquals(1, mockAccount.getAccessLevelList().size());
        assertEquals(AccountConsts.ROLE_STUDENT, mockAccount.getAccessLevelList().get(0).getAccessLevelDict().getAccessLevelName());

        verify(accountRepository).saveAndFlush(mockAccount);
        verify(postCommitTransactionManager).register(any(Runnable.class));
    }
}
