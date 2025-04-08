package org.example.Utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotManager {
    private static ScreenshotManager instance;
    private final WebDriver driver;
    private Path basePath;
    private String filePattern;

    private ScreenshotManager(WebDriver driver) {
        this(driver, "screenshots", "yyyyMMdd_HHmmssSSS");
    }

    private ScreenshotManager(WebDriver driver, String directory, String timePattern) {
        this.driver = driver;
        this.basePath = Paths.get(directory);
        this.filePattern = timePattern;
        createDirectoryIfNeeded();
    }

    public static synchronized ScreenshotManager getInstance(WebDriver driver) {
        if (instance == null) {
            instance = new ScreenshotManager(driver);
        }
        return instance;
    }

    public void takeScreenshot(String screenshotName) {
        try {
            String filename = generateFilename(screenshotName);
            Path destination = basePath.resolve(filename);

            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(destination, screenshot);

            System.out.println("Скриншот сохранён: " + destination.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Ошибка сохранения скриншота: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Общая ошибка: " + e.getMessage());
        }
    }

    private void createDirectoryIfNeeded() {
        try {
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                System.out.println("Создана директория для скриншотов: " + basePath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Ошибка создания директории: " + e.getMessage());
        }
    }

    private String generateFilename(String baseName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(filePattern);
        String timestamp = LocalDateTime.now().format(formatter);
        return String.format("%s_%s.png", baseName, timestamp);
    }

    public void setFilePattern(String newPattern) {
        this.filePattern = newPattern;
    }

    public void setBaseDirectory(String newDirectory) {
        this.basePath = Paths.get(newDirectory);
        createDirectoryIfNeeded();
    }
}