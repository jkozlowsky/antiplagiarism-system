package pl.lodz.p.it.ssbd2026.ssbd01.utils.functions;

import io.restassured.RestAssured;
import io.restassured.http.Headers;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
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
