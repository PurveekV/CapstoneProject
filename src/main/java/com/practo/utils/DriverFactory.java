package com.practo.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.HashMap;
import java.util.Map;

// this file setups with drivers which allow us to run the tests on specific browsers.
public final class DriverFactory {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    public static void createDriver(String browser, Boolean headless) {
        WebDriver driver;
        switch (browser.toLowerCase()) {
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                if (headless) options.addArguments("-headless");
                driver = new FirefoxDriver(options);
            }
            case "edge" -> {
                EdgeOptions options = new EdgeOptions();
                if (headless) options.addArguments("--headless=new");
                driver = new EdgeDriver(options);
            }
            default -> {
                ChromeOptions options = new ChromeOptions();
                if (headless) options.addArguments("--headless=new");

                // 1. Standard automation and notification blocks
                options.addArguments("--disable-notifications");
                options.addArguments("--autofill-behavior=disabled");
                options.addArguments("--disable-features=PasswordGeneration,AutofillPasswordGeneration");

                // 2. Prevent Chrome from displaying any saving/bubble UIs
                options.addArguments("--disable-save-password-bubble");

                // 🔥 EXTRA FORCE FLAGS: Stop Chrome from using OS-level credential managers
                options.addArguments("--password-store=basic"); // Forces text storage over Windows Hello/Keychain
                options.addArguments("--disable-blink-features=AutomationControlled"); // Hides automated flag
                options.addArguments("--disable-autofill-keyboard-accessory-view"); // Kills mobile/touch layouts

                // 3. Complete preference control map
                Map<String, Object> prefs = new HashMap<>();
                prefs.put("credentials_enable_service", false);
                prefs.put("profile.password_manager_enabled", false);
                prefs.put("autofill.profile_enabled", false);
                prefs.put("autofill.credit_card_enabled", false);

                // 🔥 EXTRA PREFERENCES: Explicitly block password manager and autofill variants
                prefs.put("profile.password_manager_leak_detection", false);
                prefs.put("password_manager_enabled", false);

                options.setExperimentalOption("prefs", prefs);

                driver = new ChromeDriver(options);
            }
        }
        DRIVER.set(driver);
        driver.manage().window().maximize();
    }
    public static WebDriver getDriver() {
        return DRIVER.get();
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }

}
