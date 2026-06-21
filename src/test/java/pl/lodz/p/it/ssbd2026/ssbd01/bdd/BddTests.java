package pl.lodz.p.it.ssbd2026.ssbd01.bdd;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.RestSetupInit;

import static org.assertj.core.api.Assertions.assertThat;

@CucumberContextConfiguration
public class BddTests extends BaseTestSetup {

    @LocalServerPort
    private int port;

    @Autowired
    private RestSetupInit restSetupInit;

    private String lastResponseBody;
    private int lastStatusCode;

    @Given("the API application is running and properly configured")
    public void apiApplicationIsRunningAndProperlyConfigured() {
        restSetupInit.initRestAssured(port);
    }

    @Given("I am logged in as an administrator")
    public void iAmLoggedInAsAnAdministrator() {
        restSetupInit.generateToken();
    }

    @Given("I am not logged in")
    public void iAmNotLoggedIn() {
        restSetupInit.clearRestAssured();
        restSetupInit.initRestAssured(port);
    }

    @When("I send a request to block the account with UUID {string}")
    public void iSendARequestToBlockTheAccountWithUUID(String accountUUID) {
        String etag = fetchVersionHash(accountUUID);
        Response response = RestAssured.given()
                .header("If-Match", etag)
                .contentType("application/json")
                .when()
                .post("/api/account/block/" + accountUUID);

        lastStatusCode = response.getStatusCode();
        lastResponseBody = response.getBody().asString();
    }

    @When("I send a request to unblock the account with UUID {string}")
    public void iSendARequestToUnblockTheAccountWithUUID(String accountUUID) {
        String etag = fetchVersionHash(accountUUID);
        Response response = RestAssured.given()
                .header("If-Match", etag)
                .contentType("application/json")
                .when()
                .post("/api/account/unblock/" + accountUUID);

        lastStatusCode = response.getStatusCode();
        lastResponseBody = response.getBody().asString();
    }

    @Then("I receive a {int} status code")
    public void iReceiveAStatusCode(int expectedStatusCode) {
        assertThat(lastStatusCode)
                .as("Status code of the API response")
                .isEqualTo(expectedStatusCode);
    }

    @And("the response should contain the message {string}")
    public void theResponseShouldContainTheMessage(String expectedMessage) {
        assertThat(lastResponseBody)
                .as("Response body")
                .contains(expectedMessage);
    }

    @And("the response must not be empty")
    public void theResponseMustNotBeEmpty() {
        assertThat(lastResponseBody)
                .as("Response body")
                .isNotNull()
                .isNotEmpty();
    }

    private String fetchVersionHash(String accountUUID) {
        Response getResp = RestAssured.given()
                .contentType("application/json")
                .get("/api/account/" + accountUUID);

        if (getResp.getStatusCode() != 200) {
            return "";
        }

        String hash = getResp.jsonPath().getString("account.versionHash");
        if (hash == null) {
            hash = getResp.jsonPath().getString("versionHash");
        }
        return hash != null ? hash : "";
    }
}