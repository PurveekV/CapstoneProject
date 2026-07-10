package com.practo.pages;

import com.practo.utils.DriverFactory;
import lombok.Getter;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class CorporateFormPage {

    private WebDriver driver;
    private static WebDriverWait wait;

    public CorporateFormPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
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


    @Getter
    @FindBy(xpath = "//button[@type='submit' and contains(text(), 'Schedule a demo')]")
    private WebElement scheduleDemoButton;

    @FindBy(css = ".u-text--bold.text-alpha")
    private WebElement thankYouMessage;

    @FindBy(css = ".corporate-form__input.corporate-form__input--error")
    private WebElement redErrorAlert;

    // --- Action Method ---
    public void fillAndSubmitForm(String name, String company, String email, String phone, String companySize, String reason) {

        try {
            wait.until(ExpectedConditions.visibilityOf(nameField));

            nameField.sendKeys(name);
            companyField.sendKeys(company);
            emailField.sendKeys(email);
            phoneField.sendKeys(phone);

            // Handling Dropdown 1: Company Size
            Select selectSize = new Select(organizationSize);
            selectSize.selectByVisibleText(companySize); // e.g., "100-500 employees"

            // Handling Dropdown 2: City
            Select selectInterestedIn = new Select(interestedIn);
            selectInterestedIn.selectByVisibleText(reason);

            wait.until(ExpectedConditions.elementToBeClickable(scheduleDemoButton));
            scheduleDemoButton.click();
        } catch (TimeoutException e) {
            System.out.println("Error occurred while filling and submitting the form: " + e.getMessage());
        }
    }

    public void fillInvalidForm(String name, String company, String email, String phone, String companySize, String reason) {

        try {
            wait.until(ExpectedConditions.visibilityOf(nameField));

            nameField.sendKeys(name);
            companyField.sendKeys(company);
            emailField.sendKeys(email);
            phoneField.sendKeys(phone);

            // Handling Dropdown 1: Company Size
            Select selectSize = new Select(organizationSize);
            selectSize.selectByVisibleText(companySize); // e.g., "100-500 employees"

            // Handling Dropdown 2: City
            Select selectInterestedIn = new Select(interestedIn);
            selectInterestedIn.selectByVisibleText(reason);

        } catch (TimeoutException e) {
            System.out.println("Error occurred while filling and submitting the form: " + e.getMessage());
        }
    }

    public boolean confirmMessage(){

        try{
            WebDriverWait manualCaptcha = new WebDriverWait(driver, Duration.ofSeconds(10));
            manualCaptcha.until(ExpectedConditions.visibilityOf( thankYouMessage));
            return  thankYouMessage.isDisplayed();
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public boolean alertBox(){

        try{
            return redErrorAlert.isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e){
            return false;}
    }

}