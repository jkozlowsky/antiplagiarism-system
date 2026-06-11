package pl.lodz.p.it.ssbd2026.ssbd01.finautotestfunc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.SeleniumTestSetup;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MOK1_registerUserTest extends SeleniumTestSetup {

    @Test
    @DisplayName("Test valid registering of a user in the system")
    void POS01_registerUserTest() {
        driver.get(FRONTEND_URL + "/register");
        String login = "test_user_" + cf.generateRandomStringOfLength(10);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("name"))).sendKeys("Andrzej");
        driver.findElement(By.name("surname")).sendKeys("Wazon");
        driver.findElement(By.name("email")).sendKeys(login + "@ias64.pl");
        driver.findElement(By.name("login")).sendKeys(login);

        driver.findElement(By.name("password")).sendKeys("Ias6464Ias123!");
        driver.findElement(By.name("confirmPassword")).sendKeys("Ias6464Ias123!");

        driver.findElement(By.xpath("//button[@type='submit']")).click();

        WebElement successViewButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(@href, '/login')]")));

        assertTrue(successViewButton.isDisplayed());
    }

    @Test
    @DisplayName("Verify, if providing password not matching the rules is handled properly")
    void NEG01_registerUserTest() {
        driver.get(FRONTEND_URL + "/register");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("name"))).sendKeys("Andrzej");
        driver.findElement(By.name("surname")).sendKeys("Wazon");
        driver.findElement(By.name("email")).sendKeys(cf.generateRandomStringOfLength(10) + "@ias64.pl");
        driver.findElement(By.name("login")).sendKeys(cf.generateRandomStringOfLength(10));

        driver.findElement(By.name("password")).sendKeys("ias>reszta");
        driver.findElement(By.name("confirmPassword")).sendKeys("ias>reszta");

        driver.findElement(By.xpath("//button[@type='submit']")).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(@class, 'text-danger')]")));

        assertTrue(error.isDisplayed());
    }

    @Test
    @DisplayName("Verify, if not unique login is handled properly")
    void NEG02_registerUserTest() {
        driver.get(FRONTEND_URL + "/register");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("name"))).sendKeys("Andrzej");
        driver.findElement(By.name("surname")).sendKeys("Wazon");
        driver.findElement(By.name("email")).sendKeys(cf.generateRandomStringOfLength(10) + "@ias64.pl");
        driver.findElement(By.name("login")).sendKeys("jkowalski");
        driver.findElement(By.name("password")).sendKeys("hasloHASLO123!");
        driver.findElement(By.name("confirmPassword")).sendKeys("hasloHASLO123!");

        driver.findElement(By.xpath("//button[@type='submit']")).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'bg-danger-subtle')]")));

        assertTrue(error.isDisplayed());
    }

    @Test
    @DisplayName("Verify, if wrong regex provided is handled properly")
    void NEG03_registerUserTest() {
        driver.get(FRONTEND_URL + "/register");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("name"))).sendKeys("Ias64!");
        driver.findElement(By.name("surname")).sendKeys("Ias64!");
        driver.findElement(By.name("email")).sendKeys("ias64@ias.pl.pl");
        driver.findElement(By.name("login")).sendKeys("i_a_s");
        driver.findElement(By.name("password")).sendKeys("hasloHASLO123!");
        driver.findElement(By.name("confirmPassword")).sendKeys("hasloHASLO123!");

        driver.findElement(By.xpath("//button[@type='submit']")).click();

        WebElement errorName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@name='name']/parent::div/following-sibling::p[contains(@class, 'text-danger')]")));

        WebElement errorSurname = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@name='surname']/parent::div/following-sibling::p[contains(@class, 'text-danger')]")));

        WebElement errorLogin = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@name='login']/parent::div/following-sibling::p[contains(@class, 'text-danger')]")));

        assertAll(
                () -> assertTrue(errorName.isDisplayed()),
                () -> assertTrue(errorSurname.isDisplayed()),
                () -> assertTrue(errorLogin.isDisplayed())
        );
    }
}