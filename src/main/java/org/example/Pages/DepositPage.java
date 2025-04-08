package org.example.Pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class DepositPage extends BasePage {
    private static final By ROYAL_PAY = By.cssSelector("button[data-wlc-element='block_profile-deposit'][title='Royal pay']");

    @FindBy(css = "[data-wlc-element='block_bonus_id-801523']")
    private WebElement bonusBlockPromocodeDep;

    private static final By BONUS_BLOCK_PROMOCODEDEP = By.cssSelector("[data-wlc-element='block_bonus_id-801523']");

    @FindBy(css = "[data-wlc-element='input_amount']")
    private WebElement amountInput;

    private static final By INPUT_AMOUNT = By.cssSelector("[data-wlc-element='input_amount']");

    private static final By BUTTON_DEPOSIT = By.cssSelector("[data-wlc-element='button_deposit']");

    public DepositPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public void initDeposit() {
        try {
            // 1. Клик по Royal Pay
            jsClick(ROYAL_PAY, "Royal Pay");

            // 2. Проверка, что бонус выбран
            verifyElementIsChosen(BONUS_BLOCK_PROMOCODEDEP, "Блок бонуса PROMOCODEDEP");

            // 3. Ввод суммы
            wait.until(ExpectedConditions.visibilityOf(amountInput));
            amountInput.clear();
            amountInput.sendKeys("10");

            // 4. Поиск и клик по кнопке Deposit
            jsClick(BUTTON_DEPOSIT, "Кнопка Депозита");

            System.out.println("Deposit initiated successfully.");
        } catch (Exception e) {
            throw new AssertionError("Ошибка выполнения теста: Deposit failed: " + e.getMessage(), e);
        }
    }

    private void checkBonusIsChosen() {
        wait.until(ExpectedConditions.visibilityOf(bonusBlockPromocodeDep));
        String classAttr = bonusBlockPromocodeDep.getAttribute("class");
        if (!classAttr.contains("chosen")) {
            throw new AssertionError("Бонус не выбран: отсутствует класс 'chosen'");
        }
        System.out.println("Бонус выбран корректно (класс содержит 'chosen').");
    }
}