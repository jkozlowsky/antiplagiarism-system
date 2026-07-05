package pl.lodz.p.it.ssbd2026.ssbd01.utils.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class SeleniumTestSetup {
    public WebDriver driver;
    public WebDriverWait wait;
    public final String FRONTEND_URL = "https://localhost";
    public CommonFunctions cf = new CommonFunctions();

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();

        options.setAcceptInsecureCerts(true);
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void adminLogin() {
        driver.get(FRONTEND_URL + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("login"))).sendKeys("test_admin");
        driver.findElement(By.name("password")).sendKeys("adminssbd");
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")));
        submitBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("auth2F"))).sendKeys("00000000");
        WebElement mfaSubmitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")));
        mfaSubmitBtn.click();
        wait.until(ExpectedConditions.urlContains("/users"));
    }

    public void studentLogin() {
        driver.get(FRONTEND_URL + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("login"))).sendKeys("test_student");
        driver.findElement(By.name("password")).sendKeys("adminssbd");
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("auth2F"))).sendKeys("00000000");
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/my-subjects"));
    }

    public void teacherLogin() {
        driver.get(FRONTEND_URL + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("login"))).sendKeys("test_teacher");
        driver.findElement(By.name("password")).sendKeys("IAS64enjoyers!");
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("auth2F"))).sendKeys("00000000");
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/subjects"));
    }

    public void templateLogin(String login, String password, String code) {
        driver.get(FRONTEND_URL + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("login"))).sendKeys(login);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("auth2F"))).sendKeys(code);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
    }

    public void waitForUsersTableLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("table")));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[contains(@class,'backdrop-blur')]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tbody/tr[td]")));
    }

    public void clickEditForLogin(String login) {
        waitForUsersTableLoaded();
        WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//tbody/tr[td[4][normalize-space()='" + login + "']]/td[7]/button")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", editButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editButton);
        wait.until(ExpectedConditions.urlMatches(".*/users/[^/]+$"));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}