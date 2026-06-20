package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.InvalidOldPasswordException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.PasswordSameAsOldException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.LanguageDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.MokRefreshRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceChangePasswordTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;


    @Mock
    private MokRefreshRepository mokRefreshRepository;

    @Mock
    private VersionHashService versionHashService;

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
    void changeOwnPassword_ShouldUpdatePassword_WhenValidDataProvided() {
        String login = "testUser";
        String oldPassword = "oldPassword123";
        String newPassword = "newPassword456";
        String versionHash = "valid-hash";
        mockAccount.setPasswordHash("oldEncodedHash");

        when(accountRepository.findByLogin(login)).thenReturn(Optional.of(mockAccount));
        when(versionHashService.hashObjectVersion(mockAccount.getId(), mockAccount.getVersion())).thenReturn(versionHash);

        when(passwordEncoder.matches(oldPassword, "oldEncodedHash")).thenReturn(true);
        when(passwordEncoder.matches(newPassword, "oldEncodedHash")).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedHash");

        accountService.changeOwnPassword(login, oldPassword, newPassword, versionHash);

        assertEquals("newEncodedHash", mockAccount.getPasswordHash());
        verify(mokRefreshRepository).refresh(mockAccount);
        verify(accountRepository).saveAndFlush(mockAccount);
    }

    @Test
    void changeOwnPassword_ShouldThrowInvalidOldPasswordException_WhenOldPasswordIsIncorrect() {
        String login = "testUser";
        String oldPassword = "wrongOldPassword";
        String newPassword = "newPassword456";
        String versionHash = "valid-hash";
        mockAccount.setPasswordHash("oldEncodedHash");

        when(accountRepository.findByLogin(login)).thenReturn(Optional.of(mockAccount));
        when(versionHashService.hashObjectVersion(mockAccount.getId(), mockAccount.getVersion())).thenReturn(versionHash);

        when(passwordEncoder.matches(oldPassword, "oldEncodedHash")).thenReturn(false);

        assertThrows(InvalidOldPasswordException.class,
                () -> accountService.changeOwnPassword(login, oldPassword, newPassword, versionHash)
        );

        verify(accountRepository, never()).saveAndFlush(any());
    }

    @Test
    void changeOwnPassword_ShouldThrowPasswordSameAsOldException_WhenNewPasswordEqualsOldPassword() {
        String login = "testUser";
        String oldPassword = "oldPassword123";
        String newPassword = "oldPassword123";
        String versionHash = "valid-hash";
        mockAccount.setPasswordHash("oldEncodedHash");

        when(accountRepository.findByLogin(login)).thenReturn(Optional.of(mockAccount));
        when(versionHashService.hashObjectVersion(mockAccount.getId(), mockAccount.getVersion())).thenReturn(versionHash);

        when(passwordEncoder.matches(oldPassword, "oldEncodedHash")).thenReturn(true);
        when(passwordEncoder.matches(newPassword, "oldEncodedHash")).thenReturn(true);

        assertThrows(PasswordSameAsOldException.class,
                () -> accountService.changeOwnPassword(login, oldPassword, newPassword, versionHash)
        );

        verify(accountRepository, never()).saveAndFlush(any());
    }
}