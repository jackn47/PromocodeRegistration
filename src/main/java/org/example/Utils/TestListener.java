package org.example.Utils;

import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        Object testInstance = result.getInstance();

        try {
            var field = testInstance.getClass().getDeclaredField("driver");
            field.setAccessible(true);
            WebDriver driver = (WebDriver) field.get(testInstance);

            ScreenshotManager.getInstance(driver)
                    .takeScreenshot(result.getMethod().getMethodName());

        } catch (Exception e) {
            System.err.println("Ошибка при создании скриншота: " + e.getMessage());
        }
    }
}