package com.tests.practo;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.ExtentTest;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import static com.tests.practo.TestListener.extent;

@Listeners(TestListener.class)
public class BaseTest {
    // These must be static so they are shared across all child classes
    public static WebDriver driver;
    public static WebDriverWait wait;
    public static ExtentSparkReporter spark;

    public static ThreadLocal<ExtentTest> testThread = new ThreadLocal<>(); //ThreadLocal ensures tests don't overwrite each other's logs

    protected ExtentTest getExtentTest() {
        return TestListener.getTest();
    }

    @BeforeSuite

    public static WebDriver getDriver(){
        return driver;
    }

    public static String captureScreenshotAsBase64(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    }

    @AfterSuite
    public void tearDownReport() {
        if (extent != null) {
            extent.flush();
        }
    }

    // Helper method to easily grab the current test instance
    public ExtentTest getTest() {
        return testThread.get();
    }
}
