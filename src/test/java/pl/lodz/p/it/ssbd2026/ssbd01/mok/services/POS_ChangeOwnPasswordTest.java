package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class POS_ChangeOwnPasswordTest {

    @LocalServerPort
    private int port;

    private static final String API_PREFIX = "/api";
}
