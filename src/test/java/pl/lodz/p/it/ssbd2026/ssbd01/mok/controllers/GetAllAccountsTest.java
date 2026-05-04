package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetAllAccountsTest extends BaseTestSetup {

    @Autowired
    private AccountTestRepository accountRepository;

    @Autowired
    private JwtAuthService jwtAuthService;

    private RequestFunctions requestFunctions;
    private Headers adminHeaders;
    private Headers studentHeaders;
    private Headers unauthenticatedHeaders;

    @BeforeEach
    public void setUpAuth() {
        requestFunctions = new RequestFunctions();

        String adminToken = jwtAuthService.generateToken("admin", List.of("ADMINISTRATOR"));
        String studentToken = jwtAuthService.generateToken("student", List.of("STUDENT"));

        adminHeaders = new Headers(new Header("Authorization", "Bearer " + adminToken));
        studentHeaders = new Headers(new Header("Authorization", "Bearer " + studentToken));
        unauthenticatedHeaders = new Headers();
    }

    private void createTestAccount(String prefix) {
        Account admin = accountRepository.findByLogin("admin").get();
        LanguageDict language = admin.getLanguageVersion();
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        Account testAccount = new Account();
        testAccount.setLogin(prefix + "_" + uniqueId);
        testAccount.setEmail(prefix + "_" + uniqueId + "@example.com");
        testAccount.setPasswordHash("password_hash");
        testAccount.setName("TestName");
        testAccount.setSurname("TestSurname");
        testAccount.setIsBlocked(false);
        testAccount.setLanguageVersion(language);

        accountRepository.save(testAccount);
    }

    @Test
    @DisplayName("Pomyślne pobranie listy wszystkich kont z uprawnieniami administratora")
    public void shouldGetAllAccountsSuccessfully() {
        createTestAccount("list_user1");
        createTestAccount("list_user2");

        String responseBody = requestFunctions.sendGetRequest(
                200,
                "/api/account",
                adminHeaders
        );

        List<Object> accounts = JsonPath.from(responseBody).getList("$");

        assertAll(
                () -> assertNotNull(accounts),
                () -> assertFalse(accounts.isEmpty())
        );
    }

    @Test
    @DisplayName("Pomyślne przefiltrowanie listy kont z wykorzystaniem parametru phrase")
    public void shouldGetFilteredAccountsSuccessfully() {
        createTestAccount("searchable_acc");
        createTestAccount("hidden_acc");

        String responseBody = requestFunctions.sendGetRequest(
                200,
                "/api/account?phrase=searchable",
                adminHeaders
        );

        List<String> logins = JsonPath.from(responseBody).getList("login", String.class);

        assertAll(
                () -> assertNotNull(logins),
                () -> assertFalse(logins.isEmpty()),
                () -> assertTrue(logins.stream().allMatch(login -> login.contains("searchable")))
        );
    }

    @Test
    @DisplayName("Zwrócenie pustej listy, gdy parametr phrase nie pasuje do żadnego konta")
    public void shouldReturnEmptyListForNonMatchingPhrase() {
        createTestAccount("standard_acc");

        String responseBody = requestFunctions.sendGetRequest(
                200,
                "/api/account?phrase=totally_non_existent_phrase_123",
                adminHeaders
        );

        List<Object> accounts = JsonPath.from(responseBody).getList("$");

        assertAll(
                () -> assertNotNull(accounts),
                () -> assertTrue(accounts.isEmpty())
        );
    }

    //TODO: poprawić tutaj headery bo ustawiane są dwa razy w testach i przechodzi kiedy ma wrzucić 403
    @Test
    @DisplayName("Odrzucenie próby pobrania listy wszystkich użytkowników bez uprawnień administratora")
    @Disabled
    public void shouldFailWithStudentToken() {
        requestFunctions.sendGetRequest(
                403,
                "/api/account",
                studentHeaders
        );
    }

    //TODO: poprawić tutaj headery bo ustawiane są dwa razy w testach i przechodzi kiedy ma wrzucić 403
    @Test
    @DisplayName("Odrzucenie próby pobrania listy wszystkich użytkowników bez ważnego tokena uwierzytelniającego")
    @Disabled
    public void shouldFailWithoutToken() {
        requestFunctions.sendGetRequest(
                403,
                "/api/account",
                unauthenticatedHeaders
        );
    }
}