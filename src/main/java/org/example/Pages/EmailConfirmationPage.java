package org.example.Pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class EmailConfirmationPage extends BasePage<EmailConfirmationPage> {

    public EmailConfirmationPage(WebDriver driver) {
        super(driver);
    }

    public void openConfirmationLink(String link) {
        ((JavascriptExecutor) driver).executeScript("window.location.href = '" + link + "'");
        sleep(3000);
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.not(ExpectedConditions.urlToBe(link)));
        sleep(5000);
    }
}