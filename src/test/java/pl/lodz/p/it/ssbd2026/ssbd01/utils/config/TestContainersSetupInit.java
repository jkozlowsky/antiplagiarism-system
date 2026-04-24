package pl.lodz.p.it.ssbd2026.ssbd01.utils.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class TestContainersSetupInit {

    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:18.3")
            .withDatabaseName(dotenv.get("PG_DATABASE"))
            .withUsername(dotenv.get("PG_USERNAME"))
            .withPassword(dotenv.get("PG_PASSWORD"));

    static {
        postgreSQLContainer.start();
        try {
            postgreSQLContainer.execInContainer(
                    "psql", "-U", dotenv.get("PG_USERNAME"), "-d", dotenv.get("PG_DATABASE"),
                    "-c", "CREATE ROLE " + dotenv.get("MOK_USERNAME") + " WITH LOGIN PASSWORD '" + dotenv.get("MOK_PASSWORD") + "';"
            );
            postgreSQLContainer.execInContainer(
                    "psql", "-U", dotenv.get("PG_USERNAME"), "-d", dotenv.get("PG_DATABASE"),
                    "-c", "CREATE ROLE " + dotenv.get("MPP_USERNAME") + " WITH LOGIN PASSWORD '" + dotenv.get("MPP_PASSWORD") + "';"
            );
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas uruchamiania Testcontainers: ", e);
        }
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        String jdbcUrl = postgreSQLContainer.getJdbcUrl();

        registry.add("spring.flyway.url", () -> jdbcUrl);
        registry.add("spring.flyway.user", () -> dotenv.get("PG_USERNAME"));
        registry.add("spring.flyway.password", () -> dotenv.get("PG_PASSWORD"));

        registry.add("spring.datasource.admin.url", () -> jdbcUrl);
        registry.add("spring.datasource.admin.username", () -> dotenv.get("PG_USERNAME"));
        registry.add("spring.datasource.admin.password", () -> dotenv.get("PG_PASSWORD"));

        registry.add("spring.datasource.mok.url", () -> jdbcUrl);
        registry.add("spring.datasource.mok.username", () -> dotenv.get("MOK_USERNAME"));
        registry.add("spring.datasource.mok.password", () -> dotenv.get("MOK_PASSWORD"));

        registry.add("spring.datasource.mpp.url", () -> jdbcUrl);
        registry.add("spring.datasource.mpp.username", () -> dotenv.get("MPP_USERNAME"));
        registry.add("spring.datasource.mpp.password", () -> dotenv.get("MPP_PASSWORD"));
    }
}