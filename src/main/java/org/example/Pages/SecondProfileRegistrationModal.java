package org.example.Pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SecondProfileRegistrationModal extends BasePage<SecondProfileRegistrationModal> {
    private static final By TEST_BUTTON_REGISTER = By.cssSelector("button[data-wlc-element='button_register-modal']");
    private static final By CHIP_WITHOUT_BONUS = By.xpath("//*[@data-wlc-element='bonus_title-name' and text()='Without bonus']");
    private static final By BUTTON_NEXT = By.xpath("//*[@data-wlc-element='button_next']");
    private static final By INPUT_EMAIL = By.xpath("//*[@data-wlc-element='input_email']");
    private static final By INPUT_PASSWORD = By.xpath("//*[@data-wlc-element='input_password']");
    private static final By INPUT_PROMOCODE = By.xpath("//*[@data-wlc-element='input_registration-promo-code']");
    private static final By CHECKBOX_TS = By.cssSelector("[data-wlc-element='checkbox_agreed-with-terms-and-conditions']");
    private static final By CHECKBOX_AGE = By.xpath("//*[@data-wlc-element='checkbox_age-confirmed']");
    private static final By BUTTON_SUBMIT = By.xpath("//*[@data-wlc-element='button_register-submit']");


    public SecondProfileRegistrationModal(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);  // Инициализация элементов через PageFactory
    }

    // Открыть модальное окно регистрации
    public SecondProfileRegistrationModal openModal() {
        jsClick(TEST_BUTTON_REGISTER, "Тестовая регистрация");
        return this;
    }

    // Выбор бонуса "без бонуса" и переход к следующему шагу
    public SecondProfileRegistrationModal proceedWithoutBonus() {
        jsClick(CHIP_WITHOUT_BONUS, "Фишка без бонуса");
        jsClick(BUTTON_NEXT, "Следующая кнопка");
        return this;
    }

    // Заполнение формы регистрации
    public SecondProfileRegistrationModal fillRegistrationForm(String email, String password, String promocode) {
        fillInput(INPUT_EMAIL, email, "Email");
        fillInput(INPUT_PASSWORD, password, "Password");
        fillInput(INPUT_PROMOCODE, promocode, "Promocode");
        return this;
    }

    // Клик по чекбоксу (с выбором по имени)
    public SecondProfileRegistrationModal checkCheckboxes() {
        clickCheckbox(CHECKBOX_TS, "Чекбокс TS");
        clickCheckbox(CHECKBOX_AGE, "Чекбокс AGE");
        return this;
    }

    // Подтверждение регистрации
    public SecondProfileRegistrationModal submitRegistration() {
        jsClick(BUTTON_SUBMIT, "Кнопка регистрации");
        return this;
    }
}