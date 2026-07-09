package com.practo.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.practo.utils.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HomePage {

    private WebDriver driver;
    private WebDriverWait wait;



    // ================== Home Page ===================

    @FindBy(xpath = "//input[@placeholder='Search location']")
    WebElement locationBox;

    @FindBy(xpath = "//input[@placeholder='Search doctors, clinics, hospitals, etc.']")
    WebElement searchBox;

    @FindBy(xpath = "//div[contains(text(),'Bangalore')]")
    WebElement bangaloreSuggestion;

    @FindBy(xpath = "//div[contains(text(),'Hospital')]")
    WebElement hospitalSuggestion;

    @FindBy(className = "practo_logo_new")
    WebElement pageTitle;

    @FindBy(css = ".downarrow.icon-ic_down_cheveron")
    WebElement corporatePlanButton;

    @FindBy(css = "a[event='Nav Provider Marketing:Interacted:Plus Corporate']")
    WebElement healthWellnessButton;

    // ================= Hospital Names ==================

    @FindBy(css = ".c-omni-suggestion-item__content__title .c-omni-suggestion-item__right ")
    private List<WebElement> hospitalNames;

    // Locates the entire container card for each hospital result
    @FindBy(xpath = "//div[@class= 'c-estb-card']")
    private List<WebElement> hospitalCards;

    public HomePage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void goToCoporatePage(){
        wait.until(ExpectedConditions.elementToBeClickable(corporatePlanButton));
        corporatePlanButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(healthWellnessButton));
        healthWellnessButton.click();

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

    public void searchHospital(String keyword) {
        // 1. Standard search flow
        wait.until(ExpectedConditions.visibilityOf(searchBox));
        searchBox.click();
        searchBox.clear();
        searchBox.sendKeys(keyword);

        // 2. Normalize inputs for the XPath criteria
        // If you pass "Hospital", expectedTitle is "Hospital".
        // We can hardcode "TYPE" or convert it dynamically depending on what Practo expects.
        String expectedTitle = keyword;
        String expectedTag = "TYPE"; // Handled internally by the backend code

        // 3. Dynamic XPath matching the data-qa-id markers
        String dynamicXPath = String.format(
                "//div[contains(@class, 'c-omni-suggestion-item') and .//div[@data-qa-id='omni-suggestion-main' and text()='%s'] and .//span[@data-qa-id='omni-suggestion-right' and text()='%s']]",
                expectedTitle, expectedTag
        );

        // 4. Locate and click
        WebElement matchingSuggestion = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(dynamicXPath))
        );
        matchingSuggestion.click();
    }

    //2. Apply fitlers
    public List<String> getFilteredHospitals() {
        List<String> matchingHospitals = new ArrayList<>();

        for (WebElement card : hospitalCards) {
            try {
                // 1. Extract Hospital Name
                String hospitalName = card.findElement(By.cssSelector("h2.line-1")).getText().trim();

                // 2. Extract and Parse Hospital Rating (e.g., "4.5")
                double hospitalRating = 0.0;
                try {
                    String ratingText = card.findElement(By.cssSelector("div.c-feedback span.u-bold")).getText().trim();
                    hospitalRating = Double.parseDouble(ratingText);
                } catch (NoSuchElementException e) {
                    // If a hospital has no ratings, default to 0.0
                    hospitalRating = 0.0;
                }

                // 3. Check Timing Info (Looking for "Open 24x7")
                boolean isOpen247 = false;
                try {
                    String timingText = card.findElement(By.cssSelector("span.pd-right-2px-text-green")).getText().trim();
                    if (timingText.equalsIgnoreCase("Open 24x7")) {
                        isOpen247 = true;
                    }
                } catch (NoSuchElementException e) {
                    isOpen247 = false;
                }
                if (hospitalRating > 3.5 && isOpen247) {
                    matchingHospitals.add(hospitalName);
                }} catch (Exception e) {
                // Safe fallback to skip malformed components or clean ad cards
                continue;
            }
        }

        return matchingHospitals;
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

