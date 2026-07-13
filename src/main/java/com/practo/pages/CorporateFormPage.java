package com.practo.pages;

import com.practo.utils.DriverFactory;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.Duration;

public class CorporateFormPage {

    private WebDriver driver;
    private static WebDriverWait wait;
    private static final Logger logger = LogManager.getLogger(CorporateFormPage.class);

    public CorporateFormPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
        logger.info("CorporateFormPage initialized successfully");
    }

    // --- Inputs ---
    @FindBy(id = "name") private WebElement nameField;
    @FindBy(id = "organizationName") private WebElement companyField;
    @FindBy(id = "officialEmailId") private WebElement emailField;
    @FindBy(id = "contactNumber") private WebElement phoneField;

    // --- Dropdowns ---
    @FindBy(id = "organizationSize") // Replace with actual ID/Xpath
    private WebElement organizationSize;

    @FindBy(id = "interestedIn") // Replace with actual ID/Xpath
    private WebElement interestedIn;


    @FindBy(xpath = "//button[@type='submit' and contains(text(), 'Schedule a demo')]")
    private WebElement scheduleDemoButton;

    @FindBy(css = "div[aria-label='Thank you']")
    private WebElement thankYouMessage;

    @FindBy(css = ".corporate-form__input.corporate-form__input--error")
    private WebElement redErrorAlert;

    // --- Action Method ---
    public void fillAndSubmitForm(String name, String company, String email, String phone, String companySize, String reason) {

        try {
            logger.info("Starting to fill and submit corporate form with data: name={}, company={}, email={}, phone={}, companySize={}, reason={}", 
                name, company, email, phone, companySize, reason);
            
            wait.until(ExpectedConditions.visibilityOf(nameField));
            logger.debug("Form fields are now visible");

            logger.debug("Entering name: {}", name);
            nameField.sendKeys(name);
            
            logger.debug("Entering company: {}", company);
            companyField.sendKeys(company);
            
            logger.debug("Entering email: {}", email);
            emailField.sendKeys(email);
            
            logger.debug("Entering phone: {}", phone);
            phoneField.sendKeys(phone);

            // Handling Dropdown 1: Company Size
            logger.debug("Selecting company size: {}", companySize);
            Select selectSize = new Select(organizationSize);
            selectSize.selectByVisibleText(companySize); // e.g., "100-500 employees"

            // Handling Dropdown 2: City
            logger.debug("Selecting interested in: {}", reason);
            Select selectInterestedIn = new Select(interestedIn);
            selectInterestedIn.selectByVisibleText(reason);

            logger.debug("Waiting for Schedule Demo button to be clickable");
            wait.until(ExpectedConditions.elementToBeClickable(scheduleDemoButton));
            
            logger.info("Clicking Schedule Demo button");
            scheduleDemoButton.click();
            logger.info("Form submitted successfully");
        } catch (TimeoutException e) {
            logger.error("TimeoutException occurred while filling and submitting the form: {}", e.getMessage(), e);
            System.out.println("Error occurred while filling and submitting the form: " + e.getMessage());
        }
    }

    public void fillInvalidForm(String name, String company, String email, String phone, String companySize, String reason) {

        try {
            logger.info("Starting to fill invalid form with data: name={}, company={}, email={}, phone={}, companySize={}, reason={}", 
                name, company, email, phone, companySize, reason);
            
            wait.until(ExpectedConditions.visibilityOf(nameField));
            logger.debug("Form fields are now visible");

            logger.debug("Entering name: {}", name);
            nameField.sendKeys(name);
            
            logger.debug("Entering company: {}", company);
            companyField.sendKeys(company);
            
            logger.debug("Entering email: {}", email);
            emailField.sendKeys(email);
            
            logger.debug("Entering phone: {}", phone);
            phoneField.sendKeys(phone);

            // Handling Dropdown 1: Company Size
            logger.debug("Selecting company size: {}", companySize);
            Select selectSize = new Select(organizationSize);
            selectSize.selectByVisibleText(companySize); // e.g., "100-500 employees"

            // Handling Dropdown 2: City
            logger.debug("Selecting interested in: {}", reason);
            Select selectInterestedIn = new Select(interestedIn);
            selectInterestedIn.selectByVisibleText(reason);
            
            logger.info("Invalid form filled (not submitted)");
        } catch (TimeoutException e) {
            logger.error("TimeoutException occurred while filling invalid form: {}", e.getMessage(), e);
            System.out.println("Error occurred while filling and submitting the form: " + e.getMessage());
        }
    }

    public boolean confirmMessage() {
        try {
            logger.info("Confirming thank you message modal");
            
            // Give the React modal up to 10 seconds to fully inject and render in the DOM
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Target the modal container explicitly using its ARIA role/label attributes
            By modalLocator = By.cssSelector("div[aria-label='Thank you'], .ReactModal__Content");
            logger.debug("Waiting for thank you modal to become visible");

            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalLocator));
            String modalLabel = modal.getAttribute("aria-label");
            logger.info("✓ React modal visibility verified: {}", modalLabel);
            System.out.println("   Verified React modal visibility: " + modalLabel);

            boolean isDisplayed = modal.isDisplayed();
            logger.debug("Modal is displayed: {}", isDisplayed);
            return isDisplayed;
        } catch (org.openqa.selenium.TimeoutException e) {
            logger.warn("✗ React modal did not appear or render within 10 seconds: {}", e.getMessage());
            System.out.println("   ❌ React modal did not appear or render within 10 seconds.");
            return false;
        }
    }

    public boolean alertBox(){
        logger.info("Checking for error alert box");
        try{
            boolean isDisplayed = redErrorAlert.isDisplayed();
            logger.info("Error alert box is displayed: {}", isDisplayed);
            return isDisplayed;
        } catch (org.openqa.selenium.NoSuchElementException e){
            logger.debug("Error alert box not found: {}", e.getMessage());
            return false;
        }
    }

    public WebElement getScheduleButton(){
        logger.debug("Retrieving Schedule Demo button element");
        return scheduleDemoButton;
    }
}