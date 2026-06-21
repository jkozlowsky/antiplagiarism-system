package pl.lodz.p.it.ssbd2026.ssbd01.utils.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
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
        return builder.build();
    }

    public void initRestAssured(int port) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> mapper)
        );

        RestAssured.baseURI = TestConsts.BASE_ENDPOINT.getKeyName();
        RestAssured.requestSpecification = initRequestSpec(port);
        RestAssured.filters(new CredentialsFilter());
    }

    public void generateToken() {
        String tokenString = jwtAuthService.generateToken("admin", List.of("ADMIN", "STUDENT", "TEACHER"), false);
        RestAssured.requestSpecification.header("Authorization", "Bearer " + tokenString);
    }

    public void clearRestAssured() {
        RestAssured.reset();
    }
}