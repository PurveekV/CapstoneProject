package com.tests.practo;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.practo.utils.DriverFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

import static com.tests.practo.TestListener.extent;

@Listeners(TestListener.class)
public class BaseTest {

    // Shared reporter
    public static ExtentSparkReporter spark;

    // Thread-safe ExtentTest for parallel execution
    public static ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();

    @BeforeClass(alwaysRun = true)
    @Parameters("browser")
    public void setUp(@Optional("chrome") String browser) {
        DriverFactory.createDriver(browser, false);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws InterruptedException {
        Thread.sleep(1000);
        DriverFactory.quitDriver();
    }

    protected WebDriver getDriver() {
        return DriverFactory.getDriver();
    }

    protected ExtentTest getExtentTest() {
        return TestListener.getTest();
    }

    public static String captureScreenshotAsBase64(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    }

    protected ExtentTest getTest() {
        return testThread.get();
    }
}