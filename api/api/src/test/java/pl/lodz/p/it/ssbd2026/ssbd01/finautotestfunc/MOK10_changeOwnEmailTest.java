package pl.lodz.p.it.ssbd2026.ssbd01.finautotestfunc;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.SeleniumTestSetup;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MOK10_changeOwnEmailTest extends SeleniumTestSetup {
    @Test
    public void testChangeOwnEmail() {
        studentLogin();
        driver.get(FRONTEND_URL + "/profile");

        WebElement requestBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("requestEmailChangeBtn")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'}); arguments[0].click();", requestBtn);

        WebElement resendBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("resendEmailChangeBtn")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", resendBtn);

        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(translate(., 'WYSŁANO PONOWNIE', 'wysłano ponownie'), 'wysłano ponownie') or contains(translate(., 'SENT', 'sent'), 'sent')]"))).isDisplayed());
    }
}