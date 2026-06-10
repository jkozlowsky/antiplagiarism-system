package pl.lodz.p.it.ssbd2026.ssbd01.finautotestfunc;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.SeleniumTestSetup;

public class MOK8_changeOwnPasswordTest extends SeleniumTestSetup {
    @Test
    public void testChangeOwnPassword() {
        teacherLogin();
        driver.get(FRONTEND_URL + "/profile");

        WebElement changePassBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("changeOwnPasswordBtn")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", changePassBtn);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("oldPasswordInput"))).sendKeys("adminssbd");
        driver.findElement(By.id("newPasswordInput")).sendKeys("ias6464IAS!!!!!");
        driver.findElement(By.id("confirmNewPasswordInput")).sendKeys("ias6464IAS!!!!!");

        WebElement formSaveBtn = driver.findElement(By.id("submitOwnPasswordBtn"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", formSaveBtn);

        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("confirmPopupBtn")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmBtn);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("confirmPopupBtn")));
    }
}