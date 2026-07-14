package com.tests.practo;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.practo.utils.DriverFactory;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestListener implements ITestListener, ISuiteListener {
    static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testNode = new ThreadLocal<>();

    public static ExtentTest getTest() {
        return testNode.get();
    }

    @Override
    public void onStart(ISuite suite) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        ExtentSparkReporter spark = new ExtentSparkReporter("target/extent-reports/ExtentReport_" + timestamp + ".html");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Environment", "QA");
        System.out.println("Extent Report Initialized for Suite: " + suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        if (extent != null) {
            extent.flush();
            System.out.println("Extent Report Flushed successfully.");
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extent.createTest(result.getMethod().getMethodName());
        testNode.set(test);

        // Dynamic description assignment inside Allure
        Allure.description("Automated test execution entry for: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        testNode.get().pass("Test passed successfully.");
        Allure.step("Test executed and passed smoothly.");

        if (result.getName().equals("enterInvalidFormDetails_shouldDisplayError") ||
                result.getName().equals("enterInvalidFormDetails_shouldAccept")) {

            attachBase64Screenshot("Success Evidence - Red Highlight Rendered");
            saveFileScreenshotToDisk(result);

            // Programmatic attach to guarantee exact alignment
            attachDirectToAllure("Success Evidence Screenshot");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        testNode.get().log(Status.FAIL, result.getThrowable());
        Allure.step("Test failed due to: " + result.getThrowable().getMessage());

        attachBase64Screenshot("Failure Screenshot");
        saveFileScreenshotToDisk(result);

        // Programmatic attach to guarantee exact alignment
        attachDirectToAllure("Failure Screenshot Evidence");
    }

    // Replace the old captureAllureScreenshot method with this programmatic one:
    private void attachDirectToAllure(String attachmentName) {
        WebDriver driver = DriverFactory.getDriver();
        if (driver != null) {
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            // This explicitly streams the bytes directly into the current active Allure test context
            Allure.addAttachment(attachmentName, "image/png", new ByteArrayInputStream(screenshotBytes), "png");
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        testNode.get().log(Status.SKIP, "Test Skipped: " + result.getThrowable());
        Allure.step("Test skipped from execution layout.");
    }

    // ==========================================
    // Screenshot Utilities
    // ==========================================

    /**
     * Captures and streams screenshot byte context directly to Allure attachments index.
     */
    @Attachment(value = "{attachmentName}", type = "image/png")
    public byte[] captureAllureScreenshot(String attachmentName) {
        WebDriver driver = DriverFactory.getDriver();
        if (driver != null) {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        }
        return new byte[0];
    }

    private void attachBase64Screenshot(String title) {
        WebDriver driver = DriverFactory.getDriver();
        if (driver == null) return;

        try {
            String base64 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
            testNode.get().addScreenCaptureFromBase64String(base64, title);
        } catch (Exception e) {
            testNode.get().log(Status.WARNING, "Base64 screenshot failed: " + e.getMessage());
        }
    }

    private void saveFileScreenshotToDisk(ITestResult result) {
        WebDriver driver = DriverFactory.getDriver();
        if (driver == null) return;

        try {
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path directory = Paths.get("target", "screenshots");
            Files.createDirectories(directory);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path destination = directory.resolve(result.getName() + "_" + timestamp + ".png");

            Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot backup saved: " + destination.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Could not save screenshot disk backup: " + e.getMessage());
        }
    }
}