package org.example.Utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Arrays;

public class WebDriverFactory {
    public static WebDriver createChromeDriver() {
        WebDriverManager.chromedriver().driverVersion("134.0.6998.165").setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--start-maximized",
                "--disable-dev-shm-usage",
                "--remote-allow-origins=*",
                "--no-sandbox",
                "--disable-blink-features=AutomationControlled", // Отключение флагов автоматизации
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        );
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));

        return new ChromeDriver(options);
    }

}