package pl.lodz.p.it.ssbd2026.ssbd01.finautotestfunc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.SeleniumTestSetup;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MOK2_authUserTest extends SeleniumTestSetup {

    @Test
    @DisplayName("Verify, if logging in works (test_student account)")
    void POS01_authUserTest() {
        studentLogin();
        assertTrue(driver.getCurrentUrl().contains("/my-subjects"));
    }

    @Test
    @DisplayName("Verify, if providing wrong password would not allow to login into account")
    void NEG01_authUserTest() {
        driver.get(FRONTEND_URL + "/login");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("login"))).sendKeys("test_student");
        driver.findElement(By.name("password")).sendKeys("ias64");
        driver.findElement(By.xpath("//button[@type='submit']")).click();

        WebElement globalErrorDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'bg-danger-subtle')]")));
        assertTrue(globalErrorDiv.isDisplayed());
    }

    @Test
    @DisplayName("Verify, if providing wrong 2FA code is not working")
    void NEG02_authUserTest() {
        templateLogin("test_student", "adminssbd", "64646464");

        WebElement globalErrorDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'bg-danger-subtle')]")));
        assertTrue(globalErrorDiv.isDisplayed());
    }
}