package Tests;

import org.example.Config.TestConfig;
import org.example.Gmail.GmailService;
import org.example.Pages.DepositPage;
import org.example.Pages.LoyaltyBonusesPage;
import org.example.Pages.ProfilePage;
import org.example.Pages.SecondProfileRegistrationModal;
import org.example.Utils.ScreenshotManager;
import org.example.Utils.WebDriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.time.Duration;

import static java.lang.Thread.sleep;

public class PromocodeRegTest {
    private WebDriver driver;
    private GmailService gmailService;
    private ScreenshotManager screenshotManager;

    private static final String BASE_URL = "https://test-devcasino.egamings.com/en";
    private static final String EMAIL_BASE = "danzyx47+";

    private static final String PROFILE_BONUSES_PAGE = "/profile/loyalty-bonuses";
    private static final String DEPOSIT_PAGE = "/profile/cash";
    private static final String PROFILE_PAGE = "/profile/edit";

    @BeforeMethod
    public void setup() throws GeneralSecurityException, IOException {
        driver = WebDriverFactory.createChromeDriver();
        DevTools devTools = ((ChromeDriver) driver).getDevTools();
        devTools.createSession();
        driver.get(BASE_URL);
        gmailService = new GmailService();
    }

    @Test
    public void testPromoRegistrationWithEmailConfirmation() {
        try {
            // 1. Подготовка данных
            final String testEmail = EMAIL_BASE + System.currentTimeMillis() + "@gmail.com";
            final String expectedPromo = TestConfig.getDepositPromocode();

            // 2. Регистрация
            SecondProfileRegistrationModal regModal = new SecondProfileRegistrationModal(driver);
            driver.get(BASE_URL);

            regModal.openModal();
            regModal.proceedWithoutBonus();
            regModal.fillRegistrationForm(testEmail, TestConfig.getBasePassword(), expectedPromo);
            regModal.checkCheckboxes();
            regModal.submitRegistration();

            // 3. Подтверждение email
            String confirmationLink = gmailService.waitForConfirmationLink(testEmail, 3);

            // Переход по ссылке подтверждения
            ((JavascriptExecutor)driver).executeScript("window.location.href = '" + confirmationLink + "'");
            sleep(3000);
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.not(ExpectedConditions.urlToBe(confirmationLink)));
            sleep(5000);

            // 4. Переход на страницу бонусов
            driver.get(BASE_URL + PROFILE_BONUSES_PAGE);
            sleep(4000);

            // 5. Проверка плашки Subscribed на карточке бонуса
            LoyaltyBonusesPage bonusesPage = new LoyaltyBonusesPage(driver);
            bonusesPage.verifyPromocodedepHasSubscribedTag();

            // 6. Переход на страницу профиля
            driver.get(BASE_URL + PROFILE_PAGE);
            sleep(4000);

            // 7. Заполнение полей
            ProfilePage profilePage = new ProfilePage(driver);
            profilePage.fillProfileForm();
            profilePage.submitProfileForm();

            // 8. Переход на страницу депозита
            driver.get(BASE_URL + DEPOSIT_PAGE);
            sleep(4000);

            // 9. Выбор платежки, бонуса и инициация депозита
            DepositPage depositPage = new DepositPage(driver);
            depositPage.initDeposit();

        } catch (Exception e) {
            ScreenshotManager.getInstance(driver).takeScreenshot("PromocodeDep");
            Assert.fail("Ошибка выполнения теста: " + e.getMessage(), e);
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}