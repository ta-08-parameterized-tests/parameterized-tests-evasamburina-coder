package com.softserve.academy;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.chrome.ChromeOptions;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

class GreenCityNegativeRegistrationTest {
    private static WebDriver driver;

    @BeforeAll
    static void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Check if we are running in CI (GitHub Actions)
        if (System.getenv("GITHUB_ACTIONS") != null) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
        }
        
        driver = WebDriverManager.chromedriver().capabilities(options).create();
        driver.manage().window().maximize();
        // At this stage, we are not using complex waits, so we just maximize the window
    }

    @BeforeEach
    void openRegistrationForm() throws InterruptedException {
        // 1. Open the main page
        driver.navigate().to("https://www.greencity.cx.ua/#/greenCity");
        
        // Bad practice: using a delay to allow the page to load completely.
        // This is necessary because the site may load slowly.
        Thread.sleep(5000);

        // 2. Click the "Sign Up" button to open the modal window
        driver.findElement(By.cssSelector(".header_sign-up-btn > span")).click();

        // Bad practice: using a delay to allow the modal window to open.
        Thread.sleep(2000);
    }

    // --- TESTS ---

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "test@.com", "test@domain"})
    void shouldShowErrorForInvalidEmails(String badEmail) throws InterruptedException
    {
        typeEmail(badEmail);

        typeUsername("ValidUsername");
        typePassword("ValidPass123!");
        typeConfirm("ValidPass123!");

        Thread.sleep(1000);

        assertEmailErrorVisible();
        assertSignUpButtonDisabled();
    }


    @ParameterizedTest
    @ValueSource(strings = {"execute_test"})
    void shouldShowErrorsForAllEmptyFields(String dummyParameter) throws InterruptedException {
        driver.findElement(By.id("email")).click();
        driver.findElement(By.id("firstName")).click();
        driver.findElement(By.id("password")).click();

        Thread.sleep(1000);

        assertEmailErrorVisible();
        assertUsernameErrorVisible();
        assertSignUpButtonDisabled();
    }



    static Stream<String> provideEmptyUsernames() {
        return Stream.of("", "   ");
    }

    @ParameterizedTest
    @MethodSource("provideEmptyUsernames")
    void shouldShowErrorForEmptyUsername(String emptyName) throws InterruptedException {
        typeEmail("test@example.com");
        typePassword("ValidPass123!");
        typeConfirm("ValidPass123!");

        typeUsername(emptyName);

        driver.findElement(By.id("email")).click();

        Thread.sleep(1000);

        assertUsernameErrorVisible();
        assertSignUpButtonDisabled();
    }



    static Stream<String> provideInvalidPasswords() {
        return Stream.of("123", "Passw!");
    }

    @ParameterizedTest
    @MethodSource("provideInvalidPasswords")
    void shouldShowErrorForInvalidPasswords(String badPassword) throws InterruptedException {
        typeEmail("test@example.com");
        typeUsername("ValidUsername");
        typePassword(badPassword);
        typeConfirm(badPassword);

        Thread.sleep(1000);

        WebElement passwordError = driver.findElement(By.cssSelector("p.password-not-valid"));
        assertTrue(passwordError.isDisplayed());

        assertSignUpButtonDisabled();
    }



    static Stream<String> providePasswordsWithSpaces() {
        return Stream.of("Pass 123!", " Pa ss123!", "Pa ss1 23! ");
    }

    @ParameterizedTest
    @MethodSource("providePasswordsWithSpaces")
    @DisplayName("Password with space → password rule error")
    void shouldShowErrorForPasswordWithSpace(String passwordWithSpace) throws InterruptedException {
        typeEmail("test@example.com");
        typeUsername("ValidUsername");
        typePassword(passwordWithSpace);
        typeConfirm(passwordWithSpace);

        Thread.sleep(1000);

        WebElement passwordError = driver.findElement(By.cssSelector("p.password-not-valid"));
        assertTrue(passwordError.isDisplayed());

        assertSignUpButtonDisabled();
    }


    @ParameterizedTest
    @CsvSource({
            "ValidPass123!, WrongPass999!",
            "SecurePass1!, SecurePass2!"
    })
    void shouldShowErrorForPasswordMismatch(String password, String confirmPassword) throws InterruptedException {
        typeEmail("test@example.com");
        typeUsername("ValidUsername");
        typePassword(password);
        typeConfirm(confirmPassword);

        driver.findElement(By.id("email")).click();

        Thread.sleep(1000);

        WebElement confirmError = driver.findElement(By.id("confirm-err-msg"));
        assertTrue(confirmError.isDisplayed());

        assertSignUpButtonDisabled();
    }


    // --- HELPERS (Helper methods) ---
    // This is the first step towards structuring code before learning Page Object

    private void typeEmail(String value) {
        WebElement field = driver.findElement(By.id("email"));
        field.clear();
        field.sendKeys(value);
    }

    private void typeUsername(String value) {
        WebElement field = driver.findElement(By.id("firstName"));
        field.clear();
        field.sendKeys(value);
    }

    private void typePassword(String value) {
        WebElement field = driver.findElement(By.id("password"));
        field.clear();
        field.sendKeys(value);
    }

    private void typeConfirm(String value) {
        WebElement field = driver.findElement(By.id("repeatPassword"));
        field.clear();
        field.sendKeys(value);
    }

    private void clickSignUp() {
        driver.findElement(By.cssSelector("button[type='submit'].greenStyle")).click();
    }

    private void assertEmailErrorVisible() {
        WebElement error = driver.findElement(By.id("email-err-msg"));
        assertTrue(error.isDisplayed(), "Email error message should be visible");
        // contains("required") or other text to avoid dependency on the full phrase
       // assertTrue(error.getText().toLowerCase().contains("check") || error.getText().toLowerCase().contains("correctly"));
    }

    private void assertUsernameErrorVisible() {
        WebElement error = driver.findElement(By.xpath("//input[@id='firstName']/following-sibling::div[contains(@class, 'error-message')]"));
        assertTrue(error.isDisplayed(), "Username error message should be visible");
    }


    private void assertSignUpButtonDisabled() {
        WebElement btn = driver.findElement(By.cssSelector("button[type='submit'].greenStyle"));
        assertFalse(btn.isEnabled(), "The 'Sign Up' button should be disabled with invalid data");
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
