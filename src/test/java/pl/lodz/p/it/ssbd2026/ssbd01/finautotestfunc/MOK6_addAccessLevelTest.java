package pl.lodz.p.it.ssbd2026.ssbd01.finautotestfunc;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.SeleniumTestSetup;

public class MOK6_addAccessLevelTest extends SeleniumTestSetup {
    @Test
    public void testAddAccessLevel(){
        adminLogin();
        clickEditForLogin("test_teacher");

        WebElement studentRoleCard = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("roleCard-STUDENT")));
        if (!studentRoleCard.getAttribute("class").contains("bg-active")) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'}); arguments[0].click();", studentRoleCard);
        }

        WebElement saveBtn = driver.findElement(By.xpath("//span[@id='saveProfileBtn']//button"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", saveBtn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);

        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("confirmPopupBtn")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmBtn);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("confirmPopupBtn")));
    }
}