package pl.lodz.p.it.ssbd2026.ssbd01.finautotestfunc;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.SeleniumTestSetup;

public class MOK9_changeUserPasswordTest extends SeleniumTestSetup {
    @Test
    public void testChangeUserPassword() {
        adminLogin();
        clickEditForLogin("test_teacher");

        WebElement changePassBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("changePasswordModalBtn")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", changePassBtn);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("newPasswordInput"))).sendKeys("IAS64enjoyers!");
        driver.findElement(By.id("confirmNewPasswordInput")).sendKeys("IAS64enjoyers!");

        WebElement formSubmitBtn = driver.findElement(By.id("submitOtherPasswordBtn"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", formSubmitBtn);

        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("confirmPopupBtn")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmBtn);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("confirmPopupBtn")));
    }
}