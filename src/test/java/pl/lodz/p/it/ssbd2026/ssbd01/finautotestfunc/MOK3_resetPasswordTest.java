package pl.lodz.p.it.ssbd2026.ssbd01.finautotestfunc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.SeleniumTestSetup;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MOK3_resetPasswordTest extends SeleniumTestSetup {
    @Test
    @DisplayName("Verify, if sending email leads to sending reset password link")
    void POS01_resetPasswordTest() {
        driver.get(FRONTEND_URL + "/forgot-password");
        WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        email.sendKeys("pgpool@ias64.pl");
        driver.findElement(By.id("resetPasswordSubmitBtn")).click();

        WebElement response = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("resetPasswordSuccessMsg")));
        assertTrue(response.isDisplayed());
    }

    @Test
    @DisplayName("Verify, if sending empty email is not possible")
    void NEG01_resetPasswordTest() {
        driver.get(FRONTEND_URL + "/forgot-password");
        driver.findElement(By.id("resetPasswordSubmitBtn")).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(@class, 'text-red-500')]")));
        assertTrue(error.isDisplayed());
    }
}