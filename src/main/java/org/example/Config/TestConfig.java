package org.example.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = TestConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) throw new RuntimeException("Файл config.properties не найден");
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки config.properties", e);
        }
    }

    public static String getBasePassword() {
        return getProperty("user.password");
    }

    public static String getDepositPromocode() {
        return getProperty("promocode.deposit.reg");
    }

    private static String getProperty(String key) {
        String value = props.getProperty(key);
        if (value == null) throw new RuntimeException("Свойство " + key + " не найдено");
        return value;
    }
}