package pl.lodz.p.it.ssbd2026.ssbd01.finautotestfunc;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.SeleniumTestSetup;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MOK14_logoutTest extends SeleniumTestSetup {
    @Test
    public void testLogout() {
        studentLogin();

        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("logoutBtn")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logoutBtn);

        assertTrue(wait.until(ExpectedConditions.urlToBe(FRONTEND_URL + "/login")));
    }
}