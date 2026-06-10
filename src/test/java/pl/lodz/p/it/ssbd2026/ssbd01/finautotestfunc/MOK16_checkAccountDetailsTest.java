package pl.lodz.p.it.ssbd2026.ssbd01.finautotestfunc;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.SeleniumTestSetup;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MOK16_checkAccountDetailsTest extends SeleniumTestSetup {
    @Test
    public void testCheckAccountDetails() {
        studentLogin();
        driver.get(FRONTEND_URL + "/profile");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstNameInput")));

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("test_student"));
    }
}