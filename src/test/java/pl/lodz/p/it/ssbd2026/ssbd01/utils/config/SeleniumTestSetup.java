package pl.lodz.p.it.ssbd2026.ssbd01.utils.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}