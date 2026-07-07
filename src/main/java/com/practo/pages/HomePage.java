package com.practo.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.practo.utils.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HomePage {

    private WebDriver driver;
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));



    // ================== Home Page ===================

    @FindBy(xpath = "//input[@placeholder='Search location']")
    WebElement locationBox;

    @FindBy(xpath = "//input[@placeholder='Search doctors, clinics, hospitals, etc.']")
    WebElement searchBox;

    @FindBy(xpath = "//div[contains(text(),'Bangalore')]")
    WebElement bangaloreSuggestion;

    @FindBy(xpath = "//div[contains(text(),'Hospital')]")
    WebElement hospitalSuggestion;

    // ================== Filters ===================

    @FindBy(xpath = "//span[contains(text(),'All Filters')]")
    WebElement allFilters;

    @FindBy(xpath = "//label[contains(.,'Open 24 Hours')]")
    WebElement open24Hours;

    @FindBy(xpath = "//label[contains(.,'Parking')]")
    WebElement parkingFacility;

    @FindBy(xpath = "//label[contains(.,'Above 3.5')]")
    WebElement ratingAbove35;

    @FindBy(xpath = "//button[contains(text(),'Apply')]")
    WebElement applyButton;

    @FindBy(className = "practo_logo_new")
    WebElement pageTitle;

    // ================= Hospital Names ==================

    @FindBy(xpath = "//h2[contains(@data-qa-id,'hospital_name')]")
    List<WebElement> hospitalNames;

    public HomePage() {
        this.driver = DriverFactory.getDriver();
        PageFactory.initElements(driver, this);
    }

    public boolean isDisplayed() {
        return pageTitle.isDisplayed();
    }

    //Methods
    //1. Select a city and hospital
    public void searchCity(String City){
        // This line pauses just a moment for the page to be ready before typing
        wait.until(ExpectedConditions.visibilityOf(locationBox));

        locationBox.clear();
        locationBox.sendKeys(City);
        wait.until(ExpectedConditions.visibilityOf(bangaloreSuggestion));
        bangaloreSuggestion.click();
    }

    public void searchHospital(String keyword){
        wait.until(ExpectedConditions.visibilityOf(searchBox));
        searchBox.clear();
        searchBox.sendKeys(keyword);
        wait.until(ExpectedConditions.visibilityOf(hospitalSuggestion));
        hospitalSuggestion.click();
    }

    //2. Apply fitlers
    public void applyFitlers(){

    }

    //3. Get list of hospital names
    //4. Print out the names
    public List<String> getHospitalNames() {

        new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.visibilityOfAllElements(hospitalNames));

        List<String> hospitals = new ArrayList<>();

        for(WebElement hospital : hospitalNames) {
            hospitals.add(hospital.getText());
        }

        return hospitals;
    }

    public void printHospitalNames() {

        List<String> hospitals = getHospitalNames();

        System.out.println("Hospitals matching the filters:");

        for(String hospital : hospitals) {
            System.out.println(hospital);
        }
    }

}

