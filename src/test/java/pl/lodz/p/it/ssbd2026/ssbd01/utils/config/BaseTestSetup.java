package pl.lodz.p.it.ssbd2026.ssbd01.utils.config;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.consts.OauthData;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableConfigurationProperties(OauthData.class)
public abstract class BaseTestSetup extends TestContainersSetupInit {

    @Autowired
    private RestSetupInit restSetup;

    @LocalServerPort
    private int port;

    @BeforeAll
    public void setUp() {
        restSetup.initRestAssured();
        RestAssured.port = port;
        //restSetup.generateToken();
        // dopóki nie mamy gotowego uwierzytelniania, pozostaje zakomentowane
    }

    @AfterAll
    public void tearDown() {
        restSetup.clearRestAssured();
    }
}
