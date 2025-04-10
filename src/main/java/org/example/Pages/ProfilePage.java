package org.example.Pages;

import org.example.Config.TestConfig;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.*;

public class ProfilePage extends BasePage<ProfilePage> {

    @FindBy(css = "[data-wlc-element='input_current-password']")
    private WebElement currentPasswordInput;

    @FindBy(css = "[data-wlc-element='button_submit']")
    private WebElement submitButton;

    @FindBy(css = "[data-wlc-element='input_first-name']")
    private WebElement firstNameInput;

    @FindBy(css = "[data-wlc-element='input_last-name']")
    private WebElement lastNameInput;

    @FindBy(css = "[data-wlc-element='select_gender']")
    private WebElement genderDropdown;

    @FindBy(css = "[data-wlc-element='select_country-code']")
    private WebElement countryDropdown;

    @FindBy(css = "[data-wlc-element='input_city']")
    private WebElement cityInput;

    @FindBy(css = "[data-wlc-element='input_address']")
    private WebElement addressInput;

    @FindBy(css = "[data-wlc-element='input_postal-code']")
    private WebElement postalCodeInput;

    @FindBy(css = "[data-wlc-element='input_phone-number']")
    private WebElement mobilePhoneInput;

    @FindBy(css = "[data-wlc-element='select_birth-day']")
    private WebElement birthDayDropdown;

    @FindBy(css = "[data-wlc-element='select_birth-month']")
    private WebElement birthMonthDropdown;

    @FindBy(css = "[data-wlc-element='select_birth-year']")
    private WebElement birthYearDropdown;

    private static final Map<String, String> DEFAULT_VALUES;

    static {
        Map<String, String> values = new HashMap<>();
        values.put("first-name", "TestFirstName");
        values.put("password", TestConfig.getBasePassword());
        values.put("last-name", "TestLastName");
        values.put("country", "Afghanistan");
        values.put("gender", "Female");
        values.put("city", "Kabul");
        values.put("address", "Test Street");
        values.put("postal-code", "123456");
        values.put("phone-number", "9999999999");
        values.put("birth-day", "10");
        values.put("birth-month", "May");
        values.put("birth-year", "1991");
        DEFAULT_VALUES = Collections.unmodifiableMap(values);
    }

    public ProfilePage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public ProfilePage fillProfileForm() {
        sendKeys(firstNameInput, DEFAULT_VALUES.get("first-name"));
        sendKeys(lastNameInput, DEFAULT_VALUES.get("last-name"));

        selectFromCustomDropdown(countryDropdown, DEFAULT_VALUES.get("country"));
        selectGenderFromDropdown(DEFAULT_VALUES.get("gender"));

        sendKeys(cityInput, DEFAULT_VALUES.get("city"));
        sendKeys(addressInput, DEFAULT_VALUES.get("address"));
        sendKeys(postalCodeInput, DEFAULT_VALUES.get("postal-code"));
        sendKeys(mobilePhoneInput, DEFAULT_VALUES.get("phone-number"));
        sendKeys(currentPasswordInput, DEFAULT_VALUES.get("password"));

        selectFromCustomDropdown(birthDayDropdown, DEFAULT_VALUES.get("birth-day"));
        selectFromCustomDropdown(birthMonthDropdown, DEFAULT_VALUES.get("birth-month"));
        selectFromCustomDropdown(birthYearDropdown, DEFAULT_VALUES.get("birth-year"));

        return this;
    }

    private ProfilePage selectFromCustomDropdown(WebElement dropdownButton, String optionText) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                scrollAndClick(dropdownButton, "dropdown");
                waitUntil(driver -> driver.findElements(By.cssSelector(".wlc-select__dropdown-item")).size() > 0);

                List<WebElement> items = waitUntil(driver -> {
                    List<WebElement> elements = driver.findElements(By.cssSelector(".wlc-select__dropdown-item"));
                    for (WebElement item : elements) {
                        try {
                            item.getText(); // check for Stale
                        } catch (StaleElementReferenceException e) {
                            return null;
                        }
                    }
                    return elements;
                });

                for (WebElement item : items) {
                    String itemText;
                    try {
                        itemText = item.findElement(By.cssSelector("span span span")).getText().trim();
                    } catch (Exception e) {
                        itemText = item.getText().trim();
                    }

                    if (itemText.equalsIgnoreCase(optionText)) {
                        scrollAndClick(item, "birth_date_option");
                        System.out.println("Выбрана опция: " + optionText);
                        return this;
                    }
                }

                throw new AssertionError("Опция не найдена: " + optionText);
            } catch (Exception e) {
                System.out.println("Попытка #" + attempt + " не удалась: " + e.getMessage());
                if (attempt == 3) {
                    throw new AssertionError("Ошибка при выборе из дропдауна после 3 попыток: " + optionText, e);
                }
                sleep(500);
            }
        }
        throw new AssertionError("Не удалось выбрать из дропдауна: " + optionText);
    }

    private ProfilePage selectGenderFromDropdown(String genderText) {
        scrollAndClick(genderDropdown, "dropdown");
        waitUntil(driver -> driver.findElements(By.cssSelector(".wlc-select__dropdown-item")).size() > 0);

        List<WebElement> items = driver.findElements(By.cssSelector(".wlc-select__dropdown-item"));
        for (WebElement item : items) {
            if (item.getText().trim().equalsIgnoreCase(genderText)) {
                scrollAndClick(item, "gender_option");
                return this;
            }
        }

        throw new RuntimeException("Не найдена опция гендера: " + genderText);
    }

    public ProfilePage submitProfileForm() {
        scrollAndClick(submitButton, "submit");
        return this;
    }
}