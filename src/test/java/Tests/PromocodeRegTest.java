package Tests;

import org.example.Config.TestConfig;
import org.example.Gmail.GmailService;
import org.example.Pages.*;
import org.example.Utils.WebDriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.Listeners;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static java.lang.Thread.sleep;

@Listeners(org.example.Utils.TestListener.class)
public class PromocodeRegTest {
    private WebDriver driver;
    private GmailService gmailService;

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
        // 1. Подготовка данных
        final String testEmail = EMAIL_BASE + System.currentTimeMillis() + "@gmail.com";
        final String expectedPromo = TestConfig.getDepositPromocode();

        // 2. Регистрация
        new SecondProfileRegistrationModal(driver)
                .openModal()
                .proceedWithoutBonus()
                .fillRegistrationForm(testEmail, TestConfig.getBasePassword(), expectedPromo)
                .checkCheckboxes()
                .submitRegistration();

        // 3. Подтверждение email
        String confirmationLink = gmailService.waitForConfirmationLink(testEmail, 3);

        // Переход по ссылке подтверждения
        new EmailConfirmationPage(driver)
                .openConfirmationLink(confirmationLink);

        // 4. Переход на страницу бонусов
        driver.get(BASE_URL + PROFILE_BONUSES_PAGE);

        // 5. Проверка плашки Subscribed на карточке бонуса
        new LoyaltyBonusesPage(driver)
                .waitForPageToLoad(7)
                .verifyPromocodedepHasSubscribedTag();

        // 6. Переход на страницу профиля
        driver.get(BASE_URL + PROFILE_PAGE);

        // 7. Заполнение полей
        new ProfilePage(driver)
                .waitForPageToLoad(7)
                .fillProfileForm()
                .submitProfileForm();

        // 8. Переход на страницу депозита
        driver.get(BASE_URL + DEPOSIT_PAGE);

        // 9. Выбор платежки, бонуса и инициация депозита
        new DepositPage(driver)
                .waitForPageToLoad(7)
                .initDeposit();
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}