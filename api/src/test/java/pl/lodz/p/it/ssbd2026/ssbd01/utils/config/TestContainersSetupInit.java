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

//    private static String getEnv(String key, String defaultValue) {
//        String value = dotenv.get(key);
//        return value != null ? value : defaultValue;
//    }
//
//    // BEZPIECZNE POBIERANIE SEKRETÓW (DLA PIPELINE CI)
//    private static String readSecret(String filePath, String defaultValue) {
//        try {
//            return Files.readString(Paths.get(filePath)).trim();
//        } catch (Exception e) {
//            System.out.println("Ostrzeżenie: Nie można odczytać pliku " + filePath + ". Użyto domyślnego hasła testowego.");
//            return defaultValue;
//        }
//    }

    private static final String PG_DB = dotenv.get("PG_DATABASE");
    private static final String PG_USER = dotenv.get("PG_USERNAME");
    private static final String MOK_USER = dotenv.get("MOK_USER");
    private static final String MPP_USER = dotenv.get("MPP_USER");

    private static final String PG_PASS = readSecret("secrets/pg_password.txt");
    private static final String MOK_PASS = readSecret("secrets/mok_password.txt");
    private static final String MPP_PASS = readSecret("secrets/mpp_password.txt");

//    private static final String PG_DB = getEnv("PG_DATABASE", "ssbd01_test_db");
//    private static final String PG_USER = getEnv("PG_USERNAME", "admin");
//    private static final String MOK_USER = getEnv("MOK_USER", "mok");
//    private static final String MPP_USER = getEnv("MPP_USER", "mpp");
//
//    private static final String PG_PASS = readSecret("secrets/pg_password.txt", "dummy");
//    private static final String MOK_PASS = readSecret("secrets/mok_password.txt", "dummy");
//    private static final String MPP_PASS = readSecret("secrets/mpp_password.txt", "dummy");

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