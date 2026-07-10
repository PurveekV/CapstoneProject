// Defines the package namespace where this class resides
package com.tests.practo;

import com.aventstack.extentreports.ExtentReports;
import io.qameta.allure.Attachment;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.practo.utils.DriverFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Declares a public class that implements TestNG listeners for both tests and suites
public class TestListener implements ITestListener, ISuiteListener {
    // Declares a shared, static ExtentReports instance to manage the overall report
    static ExtentReports extent;
    // Declares a ThreadLocal wrapper for ExtentTest to ensure thread-safety during parallel test execution
    private static final ThreadLocal<ExtentTest> testNode = new ThreadLocal<>();

    @Attachment(value = "{attachmentName}", type = "image/png")
    public byte[] saveAllureScreenshot(String attachmentName) {
        WebDriver driver = DriverFactory.getDriver();
        if (driver != null) {
            // Returns raw bytes which Allure intercepts and renders as a PNG in the report dashboard
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        }
        return new byte[0];
    }

    @Override
    public void onStart(ISuite suite) {
        // Generates a string timestamp using the current date and time formatted as YearMonthDay_HourMinuteSecond
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        // Initializes the Spark reporter and sets the target path for the generated HTML report file
        ExtentSparkReporter spark = new ExtentSparkReporter("target/extent-reports/ExtentReport_" + timestamp + ".html");

        // Instantiates the main ExtentReports orchestrator object
        extent = new ExtentReports();
        // Attaches the configured Spark HTML reporter to the main ExtentReports object
        extent.attachReporter(spark);
        // Adds custom system metadata (Environment = QA) to display on the report dashboard
        extent.setSystemInfo("Environment", "QA");
        // Prints a console message confirming that the reporting engine has started for the suite
        System.out.println("Extent Report Initialized for Suite: " + suite.getName());
    } // Closes the onStart method

    @Override
    public void onFinish(ISuite suite) {
        // Checks if the extent object was successfully initialized to prevent null pointer exceptions
        if (extent != null) {
            // Writes all log data out to the external HTML report file
            extent.flush();
            // Prints a console message confirming the report has been successfully written
            System.out.println("Extent Report Flushed successfully.");
        } // Closes the if block
    } // Closes the onFinish method

    @Override
    public void onTestStart(ITestResult result) {
        // Creates a new test entry inside the Extent Report using the name of the Java test method
        ExtentTest test = extent.createTest(result.getMethod().getMethodName());
        // Stores this newly created ExtentTest instance inside the ThreadLocal container for the current thread
        testNode.set(test);
    } // Closes the onTestStart method

    @Override
    public void onTestSuccess(ITestResult result) {
        // Retrieves the active thread's ExtentTest instance and logs a positive "pass" status message
        testNode.get().pass("Test passed successfully.");

        if (result.getName().equals("enterInvalidFormDetails_shouldDisplayError")) {

            System.out.println(">>> Target test detected! Capturing success evidence screenshots...");

            // Attach to Extent Report
            attachBase64Screenshot("Success Evidence - Red Highlight Rendered");

            // Save to disk and attach to Allure Report
            saveFileAndAttachToAllure(result);
        }
    } // Closes the onTestSuccess method

    @Override
    public void onTestFailure(ITestResult result) {
        // Logs a FAIL status along with the exact exception/stack trace that caused the failure
        testNode.get().log(Status.FAIL, result.getThrowable());

        // Calls the helper method to capture a screenshot and embed it directly into the HTML report as a Base64 string
        attachBase64Screenshot("Failure Screenshot");

        // Calls the helper method to save a physical copy of the screenshot onto the local hard drive
        saveFileScreenshotToDisk(result);
    } // Closes the onTestFailure method

    @Override
    public void onTestSkipped(ITestResult result) {
        // Logs a SKIP status in the report along with the reason or exception behind skipping the test
        testNode.get().log(Status.SKIP, "Test Skipped: " + result.getThrowable());
    } // Closes the onTestSkipped method

    // A visual comment separator to divide the listener hooks from the screenshot utility functions
    // ==========================================
    // Screenshot Utilities
    // ==========================================

    private void saveFileAndAttachToAllure(ITestResult result) {
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

            // Call the Allure utility to embed it cleanly into your Allure Report tree
            saveAllureScreenshot(result.getName() + " - Success Evidence Image");

        } catch (IOException e) {
            System.err.println("Could not save screenshot disk backup: " + e.getMessage());
        }
    }

    // Declares a private helper method to capture a screenshot and convert it into a Base64 text string
    private void attachBase64Screenshot(String title) {
        // Fetches the current thread's active WebDriver instance from the DriverFactory
        WebDriver driver = DriverFactory.getDriver();
        // If the driver instance is null (browser not running), exits the method early to prevent errors
        if (driver == null) return;

        // Opens a try block to handle any unexpected exceptions during the screenshot capture process
        try {
            // Casts the driver to TakesScreenshot and captures the browser view directly as a Base64 encoded string
            String base64 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
            // Attaches the Base64 image string to the current thread's active Extent test node with a title
            testNode.get().addScreenCaptureFromBase64String(base64, title);
            // Catches any exceptions that occur during the screenshot process
        } catch (Exception e) {
            // Logs a warning message into the report instead of crashing the framework if the screenshot fails
            testNode.get().log(Status.WARNING, "Base64 screenshot failed: " + e.getMessage());
        } // Closes the catch block
    } // Closes the attachBase64Screenshot method

    // Declares a private helper method to save a physical screenshot file into a local folder
    private void saveFileScreenshotToDisk(ITestResult result) {
        // Fetches the current thread's active WebDriver instance from the DriverFactory
        WebDriver driver = DriverFactory.getDriver();
        // If the driver instance is null (browser not running), exits the method early to prevent errors
        if (driver == null) return;

        // Opens a try block to handle potential input/output (IO) file system errors
        try {
            // Casts the driver to TakesScreenshot and generates a temporary image file on disk
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            // Defines a directory path pointing to the "target/screenshots" folder
            Path directory = Paths.get("target", "screenshots");
            // Automatically creates the directories on the disk if they do not already exist
            Files.createDirectories(directory);

            // Generates a clean timestamp string based on the exact moment the screenshot is taken
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            // Constructs the final destination path using the test name and the timestamp to ensure uniqueness
            Path destination = directory.resolve(result.getName() + "_" + timestamp + ".png");

            // Copies the temporary source screenshot file to its permanent destination folder, replacing any existing file with the same name
            Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            // Outputs a message to the console confirming where the file backup was saved
            System.out.println("Screenshot backup saved: " + destination.toAbsolutePath());
            // Catches file input/output exceptions specifically
        } catch (IOException e) {
            // Prints an error message to the standard error stream if the file could not be written to disk
            System.err.println("Could not save screenshot disk backup: " + e.getMessage());
        } // Closes the catch block
    } // Closes the saveFileScreenshotToDisk method
} // Closes the TestListener class definition