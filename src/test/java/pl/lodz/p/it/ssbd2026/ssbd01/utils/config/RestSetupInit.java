package pl.lodz.p.it.ssbd2026.ssbd01.utils.config;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestComponent;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.consts.TestConsts;

import static io.restassured.RestAssured.oauth2;

@TestComponent
public class RestSetupInit {

    @Value("${oauth.url}")
    private String oauthUrl;

    @Value("${oauth.username}")
    private String oauthUsername;

    @Value("${oauth.password}")
    private String oauthPassword;

    @Value("${oauth.client-id}")
    private String oauthclientId;

    @Value("${oauth.client-secret}")
    private String oauthclientSecret;

    public RequestSpecification initRequestSpec() {
        RequestSpecBuilder builder = new RequestSpecBuilder();

        builder.setContentType(ContentType.JSON);
        builder.setAccept(ContentType.JSON);

        return builder.build();
    }

    public void initRestAssured() {
        RestAssured.baseURI = TestConsts.BASE_ENDPOINT.getKeyName();
        RestAssured.basePath = "/api";
        RestAssured.requestSpecification = initRequestSpec();
        RestAssured.filters(new CredentialsFilter());
    }

    public void generateToken() {
        String tokenString = RestAssured.given()
                .contentType("application/x-www-form-urlencoded;charset=UTF-8")
                .formParam("grant_type", "password")
                .formParam("username", oauthUsername)
                .formParam("password", oauthPassword)
                .formParam("client_id", oauthclientId)
                .formParam("client_secret", oauthclientSecret)
                .when()
                .post(oauthUrl)
                .then()
                .statusCode(200)
                .extract()
                .path("access_token");

        RestAssured.authentication = oauth2(tokenString);
    }

    public void clearRestAssured() {
        RestAssured.reset();
    }
}
