package pl.lodz.p.it.ssbd2026.ssbd01.finautotestfunc;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.SeleniumTestSetup;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MOK13_changeUserAccountTest extends SeleniumTestSetup {
    @Test
    public void testChangeUserAccount() {
        adminLogin();
        clickEditForLogin("test_student");

        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstNameInput")));
        nameInput.click();
        while (!nameInput.getAttribute("value").isEmpty()) {
            nameInput.sendKeys(Keys.BACK_SPACE);
        }
        nameInput.sendKeys("Norbert");

        WebElement surnameInput = driver.findElement(By.id("lastNameInput"));
        surnameInput.click();
        while (!surnameInput.getAttribute("value").isEmpty()) {
            surnameInput.sendKeys(Keys.BACK_SPACE);
        }
        surnameInput.sendKeys("Gierczak");

        WebElement saveBtn = driver.findElement(By.xpath("//span[@id='saveProfileBtn']//button"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", saveBtn);

        try {
            saveBtn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);
        }

        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("confirmPopupBtn")));
        try {
            confirmBtn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmBtn);
        }

        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(translate(., 'POMYŚLNIE', 'pomyślnie'), 'pomyślnie') or contains(translate(., 'ZAKTUALIZOWANO', 'zaktualizowano'), 'zaktualizowano') or contains(translate(., 'SUCCESS', 'success'), 'success')]"))).isDisplayed());
    }
}