package org.example.Pages;

import org.example.Utils.ScreenshotManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.function.Function;

public class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final JavascriptExecutor js;
    protected final ScreenshotManager screenshotManager;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.js = (JavascriptExecutor) driver;
        this.screenshotManager = ScreenshotManager.getInstance(driver);
    }

    public void sendKeys(WebElement element, String text) {
        waitUntilVisible(element);
        element.clear();
        element.sendKeys(text);
    }

    protected void scrollToElement(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView(true);", element);
    }

    /**
     * Надежный метод для клика по элементам, которые могут быть невидимыми
     * @param locator - локатор элемента (By)
     * @param elementName - название элемента для логов
     */
    protected void jsClick(By locator, String elementName) {
        try {
            // 1. Ждем присутствия элемента в DOM
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));

            // 2. Получаем свежую ссылку (на случай StaleElement)
            element = driver.findElement(locator);

            // 3. Логируем состояние элемента
            System.out.printf("Элемент '%s' найден. Видимый: %s, Кликабельный: %s%n",
                    elementName, element.isDisplayed(), element.isEnabled());

            // 4. Кликаем через JS (работает даже для невидимых элементов)
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);

            System.out.println("✅ Клик по '" + elementName + "' выполнен успешно");

        } catch (NoSuchElementException e) {
            System.err.println("❌ Элемент '" + elementName + "' не найден в DOM");
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Ошибка при клике по '" + elementName + "'");
            throw e;
        }
    }

    /**
     * Надёжно заполняет поле ввода с обработкой всех возможных ошибок
     * @param locator Локатор элемента (предпочтительно использовать By вместо WebElement)
     * @param value Значение для ввода
     * @param fieldName Название поля (для логов)
     */
    protected void fillInput(By locator, String value, String fieldName) {
        try {
            // 1. Ожидаем появления элемента в DOM
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));

            // 2. Прокручиваем к элементу с центрированием
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});",
                    element
            );

            // 3. Ожидаем кликабельности
            wait.until(ExpectedConditions.elementToBeClickable(element));

            // 4. Очищаем поле с проверкой
            element.clear();
            wait.until(d -> element.getAttribute("value").isEmpty());

            // 5. Вводим значение
            element.sendKeys(value);

            // 6. Проверяем что значение установилось
            wait.until(d -> value.equals(element.getAttribute("value")));

            System.out.printf("✅ Поле '%s' успешно заполнено значением: %s%n", fieldName, value);

        } catch (StaleElementReferenceException e) {
            System.err.printf("❌ Элемент '%s' устарел (StaleElement)%n", fieldName);
            throw e;
        } catch (ElementNotInteractableException e) {
            System.err.printf("❌ Поле '%s' недоступно для взаимодействия%n", fieldName);
            throw e;
        } catch (Exception e) {
            System.err.printf("❌ Ошибка заполнения поля '%s'%n", fieldName);
            throw e;
        }
    }

    /**
     * Специальный метод для клика по кастомным чекбоксам
     * @param locator Локатор контейнера чекбокса
     * @param elementName Название элемента для логов
     */
    protected void clickCheckbox(By locator, String elementName) {
        try {
            // 1. Ждем появления элемента
            WebElement checkboxContainer = wait.until(ExpectedConditions.presenceOfElementLocated(locator));

            // 2. Прокручиваем к элементу
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});",
                    checkboxContainer
            );

            // 3. Кликаем по контейнеру, а не по иконке
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].click();",
                    checkboxContainer
            );

            // 4. Проверяем состояние после клика
            boolean isChecked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "return arguments[0].getAttribute('aria-checked') === 'true' || " +
                            "arguments[0].classList.contains('checked') || " +
                            "arguments[0].getAttribute('checked') !== null;",
                    checkboxContainer
            );

            System.out.printf("✅ Чекбокс '%s' %s%n",
                    elementName,
                    isChecked ? "отмечен" : "не отмечен после клика"
            );

        } catch (Exception e) {
            System.err.printf("❌ Ошибка при клике по чекбоксу '%s'%n", elementName);
            throw e;
        }
    }

    /**
     * Проверяет, что элемент имеет класс 'chosen' и видим на странице
     * @param locator Локатор элемента для проверки
     * @param elementName Название элемента для логов
     */
    protected void verifyElementIsChosen(By locator, String elementName) {
        try {
            // 1. Ожидаем появления и видимости элемента
            WebElement element = wait.until(driver -> {
                WebElement el = driver.findElement(locator);
                return el.isDisplayed() ? el : null;
            });

            // 2. Комплексная проверка состояния через JS
            boolean isChosen = (boolean) ((JavascriptExecutor) driver).executeScript(
                    "return arguments[0].classList.contains('chosen') || " +
                            "arguments[0].getAttribute('data-chosen') === 'true' || " +
                            "window.getComputedStyle(arguments[0]).getPropertyValue('--chosen-state') === 'active';",
                    element
            );

            // 3. Проверка визуального состояния
            if (!isChosen) {
                String actualState = (String) ((JavascriptExecutor) driver).executeScript(
                        "const el = arguments[0];" +
                                "return `Classes: ${el.className}, ` +" +
                                "`Data-chosen: ${el.getAttribute('data-chosen')}, ` +" +
                                "`Computed state: ${window.getComputedStyle(el).getPropertyValue('--chosen-state')}`;",
                        element
                );

                throw new AssertionError(String.format(
                        "Элемент '%s' не в состоянии 'chosen'. Текущее состояние: %s",
                        elementName,
                        actualState
                ));
            }

            System.out.printf("✅ Элемент '%s' успешно выбран (подтверждено состояние 'chosen')%n", elementName);

        } catch (TimeoutException e) {
            throw new AssertionError(String.format(
                    "Элемент '%s' не найден или не отображается: %s",
                    elementName,
                    e.getMessage()
            ));
        } catch (Exception e) {
            throw new AssertionError(String.format(
                    "Ошибка проверки состояния элемента '%s': %s",
                    elementName,
                    e.getMessage()
            ));
        }
    }

    protected void scrollAndClick(WebElement element, String description) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            scrollToElement(element);
            element.click();
        } catch (Exception e) {
            throw new AssertionError("Не удалось кликнуть по элементу: " + description, e);
        }
    }

    protected WebElement waitUntilVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    public <T> T waitUntil(Function<WebDriver, T> condition) {
        return wait.until(condition);
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }

    protected void assertDisplayed(WebElement element, String errorMessage) {
        Assert.assertTrue(element.isDisplayed(), errorMessage);
    }
}