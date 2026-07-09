package com.practo.pages;

import com.practo.utils.DriverFactory;
import org.openqa.selenium.By;
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

    @FindBy(xpath = "//button[@type='submit' and contains(text(), 'Schedule a demo')]")
    private WebElement scheduleDemoButton;

    @FindBy(css = ".u-text--bold.text-alpha")
    private WebElement thankYouMessage;

    // --- Action Method ---
    public void fillAndSubmitForm(String name, String company, String email, String phone, String sizeText, String cityName) {
        wait.until(ExpectedConditions.visibilityOf(nameField));

        nameField.sendKeys(name);
        companyField.sendKeys(company);
        emailField.sendKeys(email);
        phoneField.sendKeys(phone);

        // Handling Dropdown 1: Company Size
        Select selectSize = new Select(organizationSize);
        selectSize.selectByVisibleText(sizeText); // e.g., "100-500 employees"

        // Handling Dropdown 2: City
        Select selectCity = new Select(interestedIn);
        selectCity.selectByVisibleText(cityName); // e.g., "Bangalore"

        wait.until(ExpectedConditions.elementToBeClickable(scheduleDemoButton));
        scheduleDemoButton.click();
    }

    public boolean confirmMessage(){

        try{
            WebDriverWait manualwaitCaptcha = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOf( thankYouMessage));
            return  thankYouMessage.isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }
}