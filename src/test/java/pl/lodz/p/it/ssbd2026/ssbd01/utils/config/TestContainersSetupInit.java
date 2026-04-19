package pl.lodz.p.it.ssbd2026.ssbd01.utils.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestContainersSetupInit {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:18.3")
            .withDatabaseName(System.getenv("PG_DATABASE"))
            .withUsername(System.getenv("PG_USERNAME"))
            .withPassword(System.getenv("PG_PASSWORD"));

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.admin.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.admin.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.admin.password", postgreSQLContainer::getPassword);
    }
}
