package pl.lodz.p.it.ssbd2026.ssbd01.finautotestfunc;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.SeleniumTestSetup;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MOK15_getUsersInSystemTest extends SeleniumTestSetup {
    @Test
    public void testGetUsersInSystemAsAdmin() {
        adminLogin();
        waitForUsersTableLoaded();
        assertTrue(driver.findElements(By.xpath("//tbody/tr")).size() > 0);
    }

    @Test
    public void testGetUsersInSystemAsStudentNegative() {
        studentLogin();
        driver.get(FRONTEND_URL + "/users");
        assertTrue(wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(FRONTEND_URL + "/users"))));
    }
}