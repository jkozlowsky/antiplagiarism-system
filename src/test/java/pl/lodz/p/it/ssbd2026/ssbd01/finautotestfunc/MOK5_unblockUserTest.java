package pl.lodz.p.it.ssbd2026.ssbd01.finautotestfunc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.SeleniumTestSetup;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MOK5_unblockUserTest extends SeleniumTestSetup {

    @Test
    @DisplayName("Verify, if unblocking user works.")
    void POS01_unblockUserTest() {
        adminLogin();
        clickEditForLogin("test_student");

        WebElement blockBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id='blockUserBtn' or (contains(@class,'rounded-full') and contains(@class,'cursor-pointer') and contains(@class,'h-6'))]")));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'}); arguments[0].click();", blockBtn);

        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("confirmPopupBtn")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmBtn);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("confirmPopupBtn")));

        WebElement activeLabel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@id='userBlockStatusLabel' or normalize-space(text())='Aktywny' or normalize-space(text())='Active']")));

        assertTrue(activeLabel.isDisplayed());
    }
}