package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        String adminToken = jwtAuthService.generateToken("admin", List.of(AccountConsts.ROLE_ADMIN));
        String studentToken = jwtAuthService.generateToken("student", List.of("STUDENT"));

        adminHeaders = new Headers(new Header("Authorization", "Bearer " + adminToken));
        studentHeaders = new Headers(new Header("Authorization", "Bearer " + studentToken));
        unauthenticatedHeaders = new Headers();
    }

    private void createTestAccount(String login, String email, String name, String surname) {
        Account admin = accountRepository.findByLogin("admin").get();
        LanguageDict language = admin.getLanguageVersion();

        Account testAccount = new Account();
        testAccount.setLogin(login);
        testAccount.setEmail(email);
        testAccount.setPasswordHash("password_hash");
        testAccount.setName(name);
        testAccount.setSurname(surname);
        testAccount.setIsBlocked(false);
        testAccount.setLanguageVersion(language);

        accountRepository.save(testAccount);
    }

    @BeforeEach
    public void seedDatabaseForSearch() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 4);
        createTestAccount("kowalski.j" + uniqueId, "jan" + uniqueId + "@p.lodz.pl", "Jan", "Kowalski");
        createTestAccount("nowak.a" + uniqueId, "anna.nowak" + uniqueId + "@p.lodz.pl", "Anna", "Nowak");
        createTestAccount("wiacek.p" + uniqueId, "piotr" + uniqueId + "@p.lodz.pl", "Piotr", "Wiacek");
        createTestAccount("anna.z" + uniqueId, "anna.z" + uniqueId + "@p.lodz.pl", "Zofia", "Zawadzka");
    }

    @Test
    @DisplayName("Pomyślne pobranie listy wszystkich kont z paginacją")
    public void shouldGetAllAccountsSuccessfully() {
        String responseBody = requestFunctions.sendGetRequest(
                200,
                "/api/account?page=0&size=10",
                adminHeaders
        );

        List<Object> accounts = JsonPath.from(responseBody).getList("content");

        assertAll(
                () -> assertNotNull(accounts),
                () -> assertFalse(accounts.isEmpty())
        );
    }

    @Test
    @DisplayName("Pomyślne wyszukiwanie po imieniu ignorując wielkość liter")
    public void shouldSearchByNameIgnoreCase() {
        String responseBody = requestFunctions.sendGetRequest(
                200,
                "/api/account?phrase=aNNa&page=0&size=10",
                adminHeaders
        );

        List<String> names = JsonPath.from(responseBody).getList("content.account.name", String.class);
        List<String> logins = JsonPath.from(responseBody).getList("content.account.login", String.class);

        assertAll(
                () -> assertFalse(names.isEmpty()),
                () -> assertTrue(names.stream().anyMatch(n -> n.equals("Anna")) || logins.stream().anyMatch(l -> l.contains("anna")))
        );
    }

    @Test
    @DisplayName("Pomyślne wyszukiwanie po nazwisku")
    public void shouldSearchBySurname() {
        String responseBody = requestFunctions.sendGetRequest(
                200,
                "/api/account?phrase=Wiacek&page=0&size=10",
                adminHeaders
        );

        List<String> surnames = JsonPath.from(responseBody).getList("content.account.surname", String.class);

        assertAll(
                () -> assertFalse(surnames.isEmpty()),
                () -> assertTrue(surnames.stream().allMatch(s -> s.contains("Wiacek")))
        );
    }

    @Test
    @DisplayName("Pomyślne wyszukiwanie po adresie e-mail (częściowe)")
    public void shouldSearchByEmailPartial() {
        String responseBody = requestFunctions.sendGetRequest(
                200,
                "/api/account?phrase=@p.lodz.pl&page=0&size=10",
                adminHeaders
        );

        List<String> emails = JsonPath.from(responseBody).getList("content.account.email", String.class);

        assertAll(
                () -> assertFalse(emails.isEmpty()),
                () -> assertTrue(emails.size() >= 4),
                () -> assertTrue(emails.stream().allMatch(e -> e.endsWith("@p.lodz.pl")))
        );
    }

    @Test
    @DisplayName("Zwrócenie wszystkich wyników w przypadku przekazania samej spacji jako frazy")
    public void shouldReturnAllAccountsForWhitespacePhrase() {
        String responseBody = requestFunctions.sendGetRequest(
                200,
                "/api/account?phrase=   &page=0&size=10",
                adminHeaders
        );

        int totalElements = JsonPath.from(responseBody).getInt("totalElements");

        assertTrue(totalElements >= 4);
    }

    @Test
    @DisplayName("Zwrócenie pustej listy dla frazy bez dopasowań we wszystkich polach")
    public void shouldReturnEmptyListForUnmatchedPhrase() {
        String responseBody = requestFunctions.sendGetRequest(
                200,
                "/api/account?phrase=NIE_MA_TAKIEGO_TUTAJ&page=0&size=10",
                adminHeaders
        );

        List<Object> accounts = JsonPath.from(responseBody).getList("content");

        assertTrue(accounts.isEmpty());
    }

    @Test
    @DisplayName("Zwrócenie pustej strony w przypadku zażądania strony wykraczającej poza limit")
    public void shouldReturnEmptyPageForOutOfRangePageNumber() {
        String responseBody = requestFunctions.sendGetRequest(
                200,
                "/api/account?page=9999&size=10",
                adminHeaders
        );

        List<Object> accounts = JsonPath.from(responseBody).getList("content");

        assertTrue(accounts.isEmpty());
    }

    @Test
    @DisplayName("Odrzucenie próby pobrania listy bez uprawnień administratora")
    public void shouldFailWithStudentToken() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        requestFunctions.sendGetRequest(
                403,
                "/api/account?page=0&size=10",
                studentHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby pobrania listy bez tokena uwierzytelniającego")
    public void shouldFailWithoutToken() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        requestFunctions.sendGetRequest(
                403,
                "/api/account?page=0&size=10",
                unauthenticatedHeaders
        );
    }
}