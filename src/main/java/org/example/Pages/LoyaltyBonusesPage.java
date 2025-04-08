package org.example.Pages;

import org.example.Utils.ScreenshotManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoyaltyBonusesPage extends BasePage {

    @FindBy(css = "[data-wlc-element='wlc-bonus-item_id-801523']")
    private WebElement bonusPromocodeDep;

    @FindBy(css = "[data-wlc-element='wlc-tag_subscribed']")
    private WebElement subscribedTag;

    public LoyaltyBonusesPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public void verifyPromocodedepHasSubscribedTag() {
        try {
            waitUntilVisible(bonusPromocodeDep);

            WebElement statusTag = wait.until(ExpectedConditions
                    .presenceOfNestedElementLocatedBy(bonusPromocodeDep, By.cssSelector("[data-wlc-element='wlc-tag_subscribed']")));

            waitUntilVisible(statusTag);

            screenshotManager.takeScreenshot("bonus-verified");

            System.out.println("Бонус PROMOCODEDEP имеет статус SUBSCRIBED");
            assertDisplayed(statusTag, "Тег SUBSCRIBED отсутствует у бонуса PROMOCODEDEP");

        } catch (Exception e) {
            throw new AssertionError("Ошибка проверки бонуса PROMOCODEDEP: " + e.getMessage(), e);
        }
    }
}