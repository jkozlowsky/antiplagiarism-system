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
import org.springframework.test.context.TestPropertySource;
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
@TestPropertySource(properties = {
        "GITEA_URL=http://localhost:3000",
        "gitea.url=http://localhost:3000"
})
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
