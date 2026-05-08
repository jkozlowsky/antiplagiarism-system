
## 2.7. Testowanie integracyjne

Testowanie integracyjne aplikacji zostało rozwiązane poprzez wykorzystanie bibliotek RestAssured oraz Mockito. W ramach realizacji rozszerzenia z infrastrukturą testową (S2_INFRTEST) utworzone zostały kontenery testowe Testcontainers umożliwiające testowanie aplikacji bez konieczności jej produkcyjnego wdrożenia na serwerze. Dodatkowo, testy wykorzystują wyżej wspomniane atrapy (mocki) z biblioteki Mockito.

### Struktura testowa

W celu realizacji części testowej aplikacji, dokonano konfiguracji projektu z wykorzystaniem RestAssured. Każdy z testów z założenia dziedziczy po klasie `BaseTestSetup` z uwagi na zdefiniowaną tam konfigurację. Natomiast sama konfiguracja RestAssured znajduje się w klasie `RestSetupInit` i wygląda następująco:

```java
package pl.lodz.p.it.ssbd2026.ssbd01.utils.config;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestComponent;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.consts.TestConsts;

import java.util.List;

@TestComponent
public class RestSetupInit {

    @Autowired
    private JwtAuthService jwtAuthService;

    public RequestSpecification initRequestSpec(int port) {
        RequestSpecBuilder builder = new RequestSpecBuilder();

        builder.setPort(port);
        builder.setContentType(ContentType.JSON);
        builder.setAccept(ContentType.JSON);
        builder.setPort(port);
        return builder.build();
    }

    public void initRestAssured(int port) {
        RestAssured.baseURI = TestConsts.BASE_ENDPOINT.getKeyName();
        RestAssured.requestSpecification = initRequestSpec(port);
        RestAssured.filters(new CredentialsFilter());
    }

    public void generateToken() {
        String tokenString = jwtAuthService.generateToken("admin", List.of("ADMIN", "STUDENT", "TEACHER"));
        RestAssured.requestSpecification.header("Authorization", "Bearer " + tokenString);
    }

    public void clearRestAssured() {
        RestAssured.reset();
    }
}
```

Przygotowane zostało typowe połączenie testowe - zdefiniowany odpowiedni port i podstawowy URL. Dodatkowo nagłówki takie jak Content-Type czy Accept header również zostały podane w celu uproszczenia dalszego testowania. Z uwagi na występowanie ról w aplikacji, funkcja `generateToken()` ma na celu utworzyć żeton w celu umożliwienia uwierzytelnienia użytkownikowi testowemu jako każda z ról. W przypadku, gdy użytkownik chce przetestować tylko jedną z ról np. Studenta, należy nadpisać obecną konfigurację w bezpośrednio w klasie testowej. 

Sama klasa `BaseTestSetup` wykorzystuje komponent `RestSetupInit` oraz tak jak zostało to wspomniane, jest początkowym elementem wszystkich testów w projekcie. Wygląda ona następująco:

```java
package pl.lodz.p.it.ssbd2026.ssbd01.utils.config;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.consts.OauthData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "server.tomcat.accesslog.enabled=false",
                "spring.web.error.include-message=always",
                "spring.web.error.include-stacktrace=always",
                "spring.main.allow-bean-definition-overriding=true"
        })
        
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableConfigurationProperties(OauthData.class)
@Import(RestSetupInit.class)
@EnableJpaRepositories(
        basePackages = {
                "pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories"
        },
        entityManagerFactoryRef = "adminEntityManagerFactory",
        transactionManagerRef = "adminTransactionManager")
public abstract class BaseTestSetup extends TestContainersSetupInit {

    @Autowired
    private RestSetupInit restSetup;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        restSetup.initRestAssured(port);
        RestAssured.port = port;
        restSetup.generateToken();
    }

    @AfterAll
    public void tearDown() {
        restSetup.clearRestAssured();
    }
}
```

Należy tutaj podkreślić, że z uwagi na wykorzystanie Testcontainers numer portu jest losowy i trzeba zadbać o jego poprawne przypisanie.

Kolejnym ważnym elementem jest klasa `RequestFunctions`, która jest następnym elementem konfiguracyjnym RestAssured. Klasa ta zawiera wszystkie wykorzystywane w aplikacji typy żądań tj. POST, GET, PUT, PATCH i dzięki temu aby wywołać żądanie np. POST wystarczy ograniczyć się jedynie do wprowadzenia body, oczekiwanego kodu zwracanego przez aplikację, adresu URL (a dokładnie dalszej części po bazowo skonfigurowanym w poprzednich krokach np. /account) oraz nagłówków. Jest to przejrzysty i poprawny sposób testowania aplikacji dla całego zespołu. Zdefiniowane funkcje wyglądają następująco:

```java
package pl.lodz.p.it.ssbd2026.ssbd01.utils.functions;

import io.restassured.RestAssured;
import io.restassured.http.Headers;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@NoArgsConstructor
@Component
public class RequestFunctions {

    public String sendPostRequest(String body, int expectedStatusCode, String url, Headers headers) {
        return RestAssured.given()
                .headers(headers)
                .body(body)
                .when()
                .post(url)
                .then()
                .statusCode(expectedStatusCode)
                .extract()
                .body()
                .asString();
    }

    public String sendGetRequest(int expectedStatusCode, String url, Headers headers, Map<String, String> params) {
        return RestAssured.given()
                .headers(headers)
                .queryParams(params)
                .when()
                .get(url)
                .then()
                .statusCode(expectedStatusCode)
                .extract()
                .body()
                .asString();
    }

    public String sendGetRequest(int expectedStatusCode, String url, Headers headers) {
        return RestAssured.given()
                .headers(headers)
                .when()
                .get(url)
                .then()
                .statusCode(expectedStatusCode)
                .extract()
                .body()
                .asString();
    }

    public String sendPatchRequest(String body, int expectedStatusCode, String url, Headers headers) {
        return RestAssured.given()
                .headers(headers)
                .body(body)
                .when()
                .patch(url)
                .then()
                .statusCode(expectedStatusCode)
                .extract()
                .body()
                .asString();
    }
}
```

Dodatkowym aspektem są funkcje w pliku `CommonFunctions`, które przydatne są do szybkiego testowania wyrażeń regularnych (regex) poprzez wybranie odpowiedniej, wcześniej zdefiniowanej funkcji:

```java
package pl.lodz.p.it.ssbd2026.ssbd01.utils.config;

import com.github.curiousoddman.rgxgen.RgxGen;

public class CommonFunctions {

    public String generateRandomStringOfLength(int length) {
        return RgxGen.parse("[A-Za-z0-9]{" + length + "}").generate();
    }

    public String generateRandomNumberfLength(int length) {
        return RgxGen.parse("[1-9]{1}[0-9]{" + (length - 1) + "}").generate();
    }

    public String generateRandomLettersOfLength(int length) {
        return RgxGen.parse("[A-Za-z]{" + length + "}").generate();
    }

    public String generateStringNotMatchingRegex(String regex) {
        return RgxGen.parse(regex).generateNotMatching();
    }

    public String generateRandomEmail() {
        return RgxGen.parse("[a-z0-9]{5,12}@[a-z]{3,8}\\.[a-z]{2,3}").generate();
    }
}
```

Jesli chodzi o powtarzające się fragmenty kodu np. endpointy są one docelowo przetrzymywane w pliku `TestConsts`:

```java
package pl.lodz.p.it.ssbd2026.ssbd01.utils.consts;

public enum TestConsts {
    BASE_ENDPOINT("http://localhost"),
    ACCOUNT_BLOCK("/api/account/block/id"),
    ACCOUNT_UNBLOCK("/api/account/unblock/id");

    private final String keyName;

TestConsts(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }
}
```

### Sposób wdrażania aplikacji

Środowisko testowe zostało skonfigurowane z wykorzystaniem Testcontainers działając na obrazie relacyjnej bazy danych PostgreSQL w wersji 18.3. Środowisko z wykorzystaniem kontenerów ma na celu umożliwienie testowania aplikacji bez potrzeby rzeczywistego wdrażania aplikacji na serwer produkcyjny. Dlatego też został utworzony odpowiedni plik z wykorzystaniem Docker Compose, który umożliwia łatwe i powtarzalne uruchamianie kompletnej aplikacji w środowisku lokalnym (deweloperskim). Jest to szerzej opisane w rozdziale 2.8 w części z rozszerzeniem *S2_INFRTEST*. Rozszerzenie zostało wykonane w celu uniknięcia jedynej możliwości testowania aplikacji na środowisku produkcyjnym, co nie jest dobrym pomysłem. Dlatego nie posiadając docelowych środowisk testowych, zostały skonfigurowane kontenery testowe Testcontainers. Sama konfiguracja kontenera wygląda następująco:

```java
package pl.lodz.p.it.ssbd2026.ssbd01.utils.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class TestContainersSetupInit {

    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private static String readSecret(String filePath) {
        try {
            return Files.readString(Paths.get(filePath)).trim();
        } catch (Exception e) {
            throw new RuntimeException("Nie można odczytać pliku z hasłem: " + filePath, e);
        }
    }

    private static final String PG_DB = dotenv.get("PG_DATABASE");
    private static final String PG_USER = dotenv.get("PG_USERNAME");
    private static final String MOK_USER = dotenv.get("MOK_USER");
    private static final String MPP_USER = dotenv.get("MPP_USER");

    private static final String PG_PASS = readSecret("secrets/pg_password.txt");
    private static final String MOK_PASS = readSecret("secrets/mok_password.txt");
    private static final String MPP_PASS = readSecret("secrets/mpp_password.txt");

    public static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:18.3")
            .withDatabaseName(PG_DB)
            .withUsername(PG_USER)
            .withPassword(PG_PASS)
            .withCommand("-c", "max_connections=1000");;

    static {
        postgreSQLContainer.start();
        try {
            postgreSQLContainer.execInContainer(
                    "psql", "-U", PG_USER, "-d", PG_DB,
                    "-c", "CREATE ROLE " + MOK_USER + " WITH LOGIN PASSWORD '" + MOK_PASS + "';"
            );
            postgreSQLContainer.execInContainer(
                    "psql", "-U", PG_USER, "-d", PG_DB,
                    "-c", "CREATE ROLE " + MPP_USER + " WITH LOGIN PASSWORD '" + MPP_PASS + "';"
            );
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas uruchamiania Testcontainers: ", e);
        }
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        String jdbcUrl = postgreSQLContainer.getJdbcUrl();

        registry.add("spring.flyway.url", () -> jdbcUrl);
        registry.add("spring.flyway.user", () -> PG_USER);
        registry.add("spring.flyway.password", () -> PG_PASS);

        registry.add("spring.datasource.admin.url", () -> jdbcUrl);
        registry.add("spring.datasource.admin.username", () -> PG_USER);
        registry.add("spring.datasource.admin.password", () -> PG_PASS);

        registry.add("spring.datasource.mok.url", () -> jdbcUrl);
        registry.add("spring.datasource.mok.username", () -> MOK_USER);
        registry.add("spring.datasource.mok.password", () -> MOK_PASS);

        registry.add("spring.datasource.mpp.url", () -> jdbcUrl);
        registry.add("spring.datasource.mpp.username", () -> MPP_USER);
        registry.add("spring.datasource.mpp.password", () -> MPP_PASS);

        registry.add("spring.main.allow-bean-definition-overriding", () -> "true");
        registry.add("spring.datasource.mok.hikari.maximum-pool-size", () -> "5");
    }
}
```


### Zapewnienie powtarzalnego zestawu danych początkowych dla testów

Z uwagi na wykorzystanie narzędzia Flyway do zapewnienia możliwości migracji bazy danych, kontenery testowe są zintegrowane z kwerendami `.sql` obsługiwanymi przez to rozwiązanie i w taki sposób możemy uzyskać przy każdym uruchomieniu taki sam zestaw danych testowych. Poniżej przykład fragmentu jednego z zestawów kwerend z uzupełnionymi danymi testowymi:

```sql
INSERT INTO public.account (id, login, email, password_hash, name, surname, language_version_id, is_blocked, version, created_by, created_at)
        VALUES (admin_id, 'admin', 'admin@ssbd.edu.pl', '$2a$12$NqL7C7wYxGz4mFhX1Rj8dO2Bw4oF7h.v5.1oZq4e6xZ2o2X0X3z6e', 'Adam', 'Root', lang_pl_id, false, 0, admin_id, czas_obecny);
```

### Pokrycie kodu testami

W aplikacji wykorzystujemy narzędzie JaCoCo, które po wygenerowaniu raportu wskazuje stan pokrycia kodu testami. Na ten moment jest to 69% co jest wynikiem dobrym, ponieważ 100% pokrycie jest nie możliwe, a wynik satysfakcjonujący możemy przyjąć już od około 70%. Wartość ta w przyszłości powinna przynajmniej utrzymać się na tym poziomie wraz z dodaniem kolejnych przypadków użycia do aplikacji. Sam raport wygląda następująco:

![alt text](etap2/img/jacoco.png)

### Przykład testów integracyjnych - MOK.1 Rejestracja konta użytkownika

Poniżej przedstawiamy kod testów przypadku użycia `Rejestracja konta użytkownika` - wykorzystujemy dziedziczenie po `BaseTestSetup` oraz wyżej wspomniane mocki. Każdy z testów posiada adnotację `@DisplayName`, dzięki której w łatwy sposób można rozpoznać cel testu.

```java
package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok1;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.RegisterAccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Student;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MOK01_RegistrationAndActivation extends BaseTestSetup {

    @Autowired
    private AccountTestRepository accountTestRepository;

    @Autowired
    private pl.lodz.p.it.ssbd2026.ssbd01.mok.schedulers.AccountScheduler accountScheduler;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    @Qualifier("adminTransactionManager")
    private org.springframework.transaction.PlatformTransactionManager adminTransactionManager;

    @Autowired
    private RequestFunctions requestFunctions;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POS01: Powinien poprawnie zarejestrować i aktywować konto")
    public void POS01_shouldCompleteFullRegistrationAndActivationFlow() throws Exception {

        RegisterAccountDTO registerDto = new RegisterAccountDTO();
        registerDto.setName("Jan");
        registerDto.setSurname("Testowy");
        registerDto.setEmail("jan.testowy.api@example.com");
        registerDto.setLogin("jantestowy123");
        registerDto.setPassword("Password123!");

        String requestBody = objectMapper.writeValueAsString(registerDto);
        Headers headers = new Headers(new Header("Content-Type", "application/json"));

        requestFunctions.sendPostRequest(requestBody, 202, "/api/account/register", headers);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);

        String token = txTemplate.execute(status -> {
            Optional<Account> savedAccountOpt = accountTestRepository.findByLogin("jantestowy123");
            assertTrue(savedAccountOpt.isPresent(), "Konto powinno zostać utworzone w bazie");

            Account savedAccount = savedAccountOpt.get();

            assertAll("Weryfikacja stanu konta po udanej rejestracji",
                    () -> assertFalse(savedAccount.getIsVerified(), "Konto po rejestracji musi być niezweryfikowane"),
                    () -> assertNotNull(savedAccount.getActivationToken(), "System musi wygenerować token aktywacyjny"),
                    () -> assertTrue(savedAccount.getAccessLevelList().stream().anyMatch(al -> al instanceof Student),
                            "Konto musi mieć przypisaną rolę Studenta")
            );
            return savedAccount.getActivationToken();
        });

        verify(emailService, times(1)).sendActivationEmail(eq("jan.testowy.api@example.com"), eq(token), any());

        String activationUrl = "/api/account/activate?token=" + token;
        requestFunctions.sendPostRequest("", 200, activationUrl, headers);

        txTemplate.executeWithoutResult(status -> {
            Account activatedAccount = accountTestRepository.findByLogin("jantestowy123").get();

            assertAll("Weryfikacja stanu konta po aktywacji",
                    () -> assertTrue(activatedAccount.getIsVerified(), "Konto musi zmienić status na zweryfikowane (true)"),
                    () -> assertNull(activatedAccount.getActivationToken(), "Token po użyciu musi zostać wyczyszczony na null")
            );
        });
    }

    @Test
    @DisplayName("POS02: Scheduler powinien wysłać przypomnienie po 12h od rejestracji")
    public void POS02_scheduler_ShouldSendReminderAfter12h() throws Exception {
        RegisterAccountDTO registerDto = new RegisterAccountDTO();
        registerDto.setName("Test");
        registerDto.setSurname("Reminder");
        registerDto.setEmail("reminder@example.com");
        registerDto.setLogin("reminder123");
        registerDto.setPassword("Password123!");

        String requestBody = objectMapper.writeValueAsString(registerDto);
        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        requestFunctions.sendPostRequest(requestBody, 202, "/api/account/register", headers);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);

        String token = txTemplate.execute(status -> {
            Account account = accountTestRepository.findByLogin("reminder123").get();
            account.setCreationDate(java.time.LocalDateTime.now().minusHours(13));
            accountTestRepository.save(account);
            return account.getActivationToken();
        });

        accountScheduler.processInactiveAccounts();

        verify(emailService, times(1)).sendActivationReminderEmail(eq("reminder@example.com"), eq(token), any());

        txTemplate.executeWithoutResult(status -> {
            Account updatedAccount = accountTestRepository.findByLogin("reminder123").get();
            assertTrue(updatedAccount.isSecondEmailSent(), "Flaga secondEmailSent powinna zmienić się na true");
            assertFalse(updatedAccount.getIsVerified());
        });
    }

    @Test
    @DisplayName("POS03: Scheduler powinien usunąć konto po 24h od rejestracji")
    public void POS03_scheduler_ShouldDeleteAccountAfter24h() throws Exception {
        RegisterAccountDTO registerDto = new RegisterAccountDTO();
        registerDto.setName("Test");
        registerDto.setSurname("Delete");
        registerDto.setEmail("delete@example.com");
        registerDto.setLogin("delete123");
        registerDto.setPassword("Password123!");

        String requestBody = objectMapper.writeValueAsString(registerDto);
        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        requestFunctions.sendPostRequest(requestBody, 202, "/api/account/register", headers);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);

        txTemplate.executeWithoutResult(status -> {
            Account account = accountTestRepository.findByLogin("delete123").get();
            account.setCreationDate(java.time.LocalDateTime.now().minusHours(25));
            account.setSecondEmailSent(true);
            accountTestRepository.save(account);
        });

        accountScheduler.processInactiveAccounts();

        verify(emailService, times(1)).sendAccountDeletedEmail(eq("delete@example.com"), any());

        txTemplate.executeWithoutResult(status -> {
            assertTrue(accountTestRepository.findByLogin("delete123").isEmpty(), "Konto powinno zostać fizycznie usunięte z bazy danych");
        });
    }

    @Test
    @DisplayName("NEG01: Rejestracja powinna zwrócić błąd (400), gdy hasło nie spełnia polityki bezpieczeństwa")
    public void NEG01_register_ShouldReturn400_WhenPasswordIsTooWeak() throws Exception {
        RegisterAccountDTO registerDto = new RegisterAccountDTO();
        registerDto.setName("Jan");
        registerDto.setSurname("Testowy");
        registerDto.setEmail("janek@example.com");
        registerDto.setLogin("janek123");
        registerDto.setPassword("slabehaslo");

        String requestBody = objectMapper.writeValueAsString(registerDto);
        Headers headers = new Headers(new Header("Content-Type", "application/json"));

        requestFunctions.sendPostRequest(requestBody, 400, "/api/account/register", headers);

        assertTrue(accountTestRepository.findByLogin("janek123").isEmpty());
    }

    @Test
    @DisplayName("NEG02: Rejestracja powinna zwrócić konflikt (409), gdy login jest już zajęty")
    public void NEG02_register_ShouldReturn409_WhenLoginAlreadyExists() throws Exception {
        RegisterAccountDTO firstUser = new RegisterAccountDTO();
        firstUser.setName("Anna");
        firstUser.setSurname("Kowalska");
        firstUser.setEmail("anna@example.com");
        firstUser.setLogin("zajetylogin");
        firstUser.setPassword("Password123!");

        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        requestFunctions.sendPostRequest(objectMapper.writeValueAsString(firstUser), 202, "/api/account/register", headers);

        RegisterAccountDTO secondUser = new RegisterAccountDTO();
        secondUser.setName("Celina");
        secondUser.setSurname("Dabrowska");
        secondUser.setEmail("celina@example.com");
        secondUser.setLogin("zajetylogin");
        secondUser.setPassword("Password123!");

        requestFunctions.sendPostRequest(objectMapper.writeValueAsString(secondUser), 409, "/api/account/register", headers);
    }

    @Test
    @DisplayName("NEG03: Aktywacja powinna zwrócić błąd (400), gdy podano niewłaściwy token")
    public void NEG03_activate_ShouldReturn400_WhenTokenIsInvalid() {
        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        String invalidActivationUrl = "/api/account/activate?token=zmyslony-falszywy-token-123";

        requestFunctions.sendPostRequest("", 400, invalidActivationUrl, headers);
    }

    @Test
    @DisplayName("NEG04: System powinien zablokować współbieżną próbę aktywacji tego samego konta (Optimistic Locking)")
    public void NEG04_activate_ShouldHandleRaceCondition() throws Exception {
        RegisterAccountDTO registerDto = new RegisterAccountDTO();
        registerDto.setName("Race");
        registerDto.setSurname("Condition");
        registerDto.setEmail("race@example.com");
        registerDto.setLogin("race123");
        registerDto.setPassword("Password123!");

        String requestBody = objectMapper.writeValueAsString(registerDto);
        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        requestFunctions.sendPostRequest(requestBody, 202, "/api/account/register", headers);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);
        String token = txTemplate.execute(status -> {
            return accountTestRepository.findByLogin("race123").get().getActivationToken();
        });

        String activationUrl = "/api/account/activate?token=" + token;

        int threadCount = 2;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger conflictCount = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    io.restassured.response.Response response = io.restassured.RestAssured.given()
                            .headers(headers)
                            .post(activationUrl);

                    if (response.getStatusCode() == 200) {
                        successCount.incrementAndGet();
                    } else if (response.getStatusCode() == 409 || response.getStatusCode() == 400 || response.getStatusCode() == 500) {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);

        assertEquals(1, successCount.get(), "Dokładnie jedno żądanie powinno zakończyć się sukcesem (200 OK)");
        assertEquals(1, conflictCount.get(), "Drugie żądanie powinno zostać odrzucone z powodu wyścigu (zmiana wersji)");
    }

    @Test
    @DisplayName("NEG05: Rejestracja powinna odrzucić próbę SQL Injection oraz niepoprawne formaty danych (400 Bad Request)")
    public void NEG05_register_ShouldReturn400_WhenDataIsInvalidOrContainsSQLi() throws Exception {
        RegisterAccountDTO registerDto = new RegisterAccountDTO();
        registerDto.setName("Hacker");
        registerDto.setSurname("SQL");
        registerDto.setEmail("to-nie-jest-email");
        registerDto.setLogin("'; DROP TABLE account; --");
        registerDto.setPassword("Password123!");

        String badRequestBody = objectMapper.writeValueAsString(registerDto);
        Headers headers = new Headers(new Header("Content-Type", "application/json"));

        io.restassured.response.Response response = io.restassured.RestAssured.given()
                .headers(headers)
                .body(badRequestBody)
                .post("/api/account/register");

        assertNotEquals(202, response.getStatusCode(), "Żądanie z błędnymi danymi powinno zostać odrzucone (zły mail i login)");

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);
        txTemplate.executeWithoutResult(status -> {
            assertTrue(accountTestRepository.findByLogin("'; DROP TABLE account; --").isEmpty(),
                    "Złośliwe konto NIE powinno zostać zarejestrowane");
        });
    }

    @Test
    @DisplayName("NEG06: System powinien odrzucić próbę aktywacji już aktywnego konta (Replay Attack)")
    public void NEG06_activate_ShouldReturn400_WhenAccountIsAlreadyActivated() throws Exception {
        RegisterAccountDTO registerDto = new RegisterAccountDTO();
        registerDto.setName("Replay");
        registerDto.setSurname("Attack");
        registerDto.setEmail("replay@example.com");
        registerDto.setLogin("replay123");
        registerDto.setPassword("Password123!");

        String requestBody = objectMapper.writeValueAsString(registerDto);
        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        requestFunctions.sendPostRequest(requestBody, 202, "/api/account/register", headers);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);
        String token = txTemplate.execute(status -> {
            return accountTestRepository.findByLogin("replay123").get().getActivationToken();
        });

        String activationUrl = "/api/account/activate?token=" + token;

        requestFunctions.sendPostRequest("", 200, activationUrl, headers);

        requestFunctions.sendPostRequest("", 400, activationUrl, headers);
    }
}
```