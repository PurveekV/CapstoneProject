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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HomePage {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final Logger logger = LogManager.getLogger(HomePage.class);



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

    @FindBy(css = "[title='tests']")
    WebElement labTestsButton;

    @FindBy(id = "read_more_info")
    WebElement clickReadMore;




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
        logger.info("HomePage initialized successfully");
    }

    public void goToCoporatePage(){
        logger.info("Navigating to Corporate Page");
        wait.until(ExpectedConditions.elementToBeClickable(corporatePlanButton));
        logger.debug("Corporate Plan button is clickable");
        corporatePlanButton.click();
        logger.debug("Corporate Plan button clicked");
        
        wait.until(ExpectedConditions.elementToBeClickable(healthWellnessButton));
        logger.debug("Health & Wellness button is clickable");
        healthWellnessButton.click();
        logger.info("Successfully navigated to Health & Wellness page");
    }

    public void goToDiagnosticsPage(){
        logger.info("Navigating to Diagnostics Page");
        wait.until(ExpectedConditions.elementToBeClickable(labTestsButton));
        logger.debug("Lab Tests button is clickable");
        labTestsButton.click();
        logger.info("Successfully navigated to Diagnostics page");
    }

    public boolean isDisplayed() {
        logger.debug("Checking if HomePage is displayed");
        boolean isPageDisplayed = pageTitle.isDisplayed();
        logger.info("HomePage display status: {}", isPageDisplayed);
        return isPageDisplayed;
    }

    //Methods
    //1. Select a city and hospital
    public void searchCity(String City){
        logger.info("Searching for city: {}", City);
        // This line pauses just a moment for the page to be ready before typing
        wait.until(ExpectedConditions.visibilityOf(locationBox));
        logger.debug("Location box is visible");

        locationBox.clear();
        logger.debug("Location box cleared");
        
        locationBox.sendKeys(City);
        logger.debug("Entered city: {}", City);
        
        wait.until(ExpectedConditions.visibilityOf(bangaloreSuggestion));
        logger.debug("Bangalore suggestion is visible");
        
        bangaloreSuggestion.click();
        logger.info("City {} selected successfully", City);
    }

    public void searchHospital(String keyword) {
        logger.info("Searching for hospital with keyword: {}", keyword);
        // 1. Standard search flow
        wait.until(ExpectedConditions.visibilityOf(searchBox));
        logger.debug("Search box is visible");
        
        searchBox.click();
        logger.debug("Search box clicked");
        
        searchBox.clear();
        logger.debug("Search box cleared");
        
        searchBox.sendKeys(keyword);
        logger.debug("Entered keyword: {}", keyword);

        String expectedTitle = keyword;
        String expectedTag = "TYPE"; // Handled internally by the backend code

        // 3. Dynamic XPath matching the data-qa-id markers
        String dynamicXPath = String.format(
                "//div[contains(@class, 'c-omni-suggestion-item') and .//div[@data-qa-id='omni-suggestion-main' and text()='%s'] and .//span[@data-qa-id='omni-suggestion-right' and text()='%s']]",
                expectedTitle, expectedTag
        );
        logger.debug("Waiting for matching suggestion with XPath");

        // 4. Locate and click
        WebElement matchingSuggestion = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(dynamicXPath))
        );
        logger.debug("Matching suggestion found and clickable");
        
        matchingSuggestion.click();
        logger.info("Hospital suggestion clicked for keyword: {}", keyword);
    }

    //2. Apply filters (Including 24/7, Rating > 3.5, and Multi-Tab Parking Validation)
    public List<String> getFilteredHospitals() {
        logger.info("Starting to get filtered hospitals");
        WebDriverWait pageWait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 1. Explicitly wait for the cards to load in the DOM
        pageWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[@class='c-estb-card']")));
        logger.debug("Hospital cards loaded in DOM");

        List<String> matchingHospitals = new ArrayList<>();
        String mainTab = driver.getWindowHandle();

        for (WebElement card : hospitalCards) {
            // Stop looking completely once we have successfully captured our first 10 matches
            if (matchingHospitals.size() >= 10) {
                logger.info("🎯 Reached target limit of 10 qualified hospitals. Stopping search loop.");
                System.out.println("🎯 Reached target limit of 10 qualified hospitals. Stopping search loop.");
                break;
            }

            String hospitalName = "unknown";
            try {
                // 1. Extract Hospital Name
                WebElement titleElement = card.findElement(By.cssSelector("h2.line-1"));
                hospitalName = titleElement.getText().trim();
                logger.debug("Extracted hospital name: {}", hospitalName);

                // 2. Extract and Parse Hospital Rating (e.g., "4.5")
                double hospitalRating = 0.0;
                try {
                    String ratingText = card.findElement(By.cssSelector("div.c-feedback span.u-bold")).getText().trim();
                    hospitalRating = Double.parseDouble(ratingText);
                    logger.debug("Hospital {} rating: {}", hospitalName, hospitalRating);
                } catch (NoSuchElementException e) {
                    hospitalRating = 0.0;
                    logger.debug("Could not find rating for: {}", hospitalName);
                    System.out.println("Could not find rating for: " + hospitalName);
                }

                // 3. Check Timing Info (Flexible matching pattern)
                boolean isOpen247 = false;
                try {
                    String timingText = card.findElement(By.cssSelector("span.pd-right-2px-text-green")).getText().trim().toLowerCase();
                    if (timingText.contains("24x7") || timingText.contains("24 hours") || timingText.contains("open 24")) {
                        isOpen247 = true;
                    }
                    logger.debug("Hospital {} 24/7 status: {}", hospitalName, isOpen247);
                } catch (NoSuchElementException e) {
                    isOpen247 = false;
                    logger.debug("Could not find timing info for: {}", hospitalName);
                    System.out.println("Could not find timing info for: " + hospitalName);
                }

                // 4. If basic criteria pass, open detail view to check for Parking
                if (hospitalRating > 3.5 && isOpen247) {
                    logger.info("⏳ {} passed Gate 1. Opening tab to verify parking description...", hospitalName);
                    System.out.println("⏳ " + hospitalName + " passed Gate 1. Opening tab to verify parking description...");

                    WebElement linkElement = card.findElement(By.cssSelector("a[href*='/hospital/']"));
                    String targetUrl = linkElement.getAttribute("href");
                    logger.debug("Target URL for {}: {}", hospitalName, targetUrl);

                    // Open the tab via JavaScript
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript("window.open(arguments[0], '_blank');", targetUrl);
                    logger.debug("New tab opened for: {}", hospitalName);

                    // 1. Wait for the new tab to be recognized
                    pageWait.until(ExpectedConditions.numberOfWindowsToBe(2));

                    // 2. BULLETPROOF TAB SWITCH: Always grab the absolute newest handle
                    List<String> handles = new ArrayList<>(driver.getWindowHandles());
                    String newTabHandle = handles.get(handles.size() - 1); // The last one opened
                    driver.switchTo().window(newTabHandle);
                    logger.debug("Switched to new tab for hospital: {}", hospitalName);

                    // =================== TWIN-STRATEGY COOKIE BYPASS BLOCK ===================
                    try {
                        WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(3));

                        // STRATEGY A: Try clicking it directly in the root page DOM (No Iframe)
                        try {
                            WebElement consentBtn = popupWait.until(ExpectedConditions.elementToBeClickable(
                                    By.cssSelector("button.fc-cta-consent.fc-primary-button")
                            ));
                            js.executeScript("arguments[0].click();", consentBtn);
                            logger.debug("Cookie consent overlay bypassed directly in main DOM");
                            System.out.println("   Cookie consent overlay bypassed directly in main DOM.");
                        }
                        // STRATEGY B: Fallback to Iframe context if Strategy A fails
                        catch (TimeoutException e) {
                            popupWait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                                    By.cssSelector("iframe[title*='consent'], iframe[id*='googlefc'], iframe[src*='fundingchoices'], iframe.googlefc-iframe")
                            ));
                            WebElement iframeConsentBtn = popupWait.until(ExpectedConditions.elementToBeClickable(
                                    By.cssSelector("button.fc-cta-consent.fc-primary-button")
                            ));
                            js.executeScript("arguments[0].click();", iframeConsentBtn);
                            logger.debug("Cookie consent overlay bypassed inside iframe container");
                            System.out.println("   Cookie consent overlay bypassed inside iframe container.");
                            driver.switchTo().defaultContent(); // Escape iframe back to root document
                        }
                    } catch (Exception e) {
                        // Safe fallback cleanup to keep execution context clear
                        driver.switchTo().defaultContent();
                        logger.debug("Cookie banner notice not visible or already absent");
                        System.out.println("   Cookie banner notice not visible or already absent.");
                    }
                    // =========================================================================

                    boolean hasParking = false;
                    try {
                        WebDriverWait tabWait = new WebDriverWait(driver, Duration.ofSeconds(5));

                        // 3. Dynamic "Read More" check specific to this child window DOM
                        By readMoreLocator = By.cssSelector("[data-qa-id='read_more_info']");

                        try {
                            WebElement tabReadMore = tabWait.until(ExpectedConditions.elementToBeClickable(readMoreLocator));
                            js.executeScript("arguments[0].click();", tabReadMore);
                            logger.debug("Clicked 'Read More' to expand description text for: {}", hospitalName);
                            System.out.println("   Clicked 'Read More' to expand description text.");
                        } catch (TimeoutException e) {
                            logger.debug("No 'Read More' button present for: {} (text already fully expanded)", hospitalName);
                            System.out.println("   No 'Read More' button present (text already fully expanded).");
                        }

                        // 4. Scan the text elements now that the panel is open
                        By detailsLocator = By.cssSelector("[data-qa-id='amenity_item'], .p-entity__item, .c-description__text, .c-amenities__item");
                        List<WebElement> detailsElements = tabWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(detailsLocator));

                        for (WebElement element : detailsElements) {
                            String text = element.getText().toLowerCase().trim();
                            if (text.contains("parking") || text.contains("valet")) {
                                hasParking = true;
                                logger.debug("Parking found in details for: {}", hospitalName);
                                break;
                            }
                        }

                    } catch (Exception e) {
                        logger.error("Error checking expanded layout details for: {} - {}", hospitalName, e.getMessage());
                        System.out.println("   ❌ Error checking expanded layout details for: " + hospitalName);
                    }

                    // 5. CRITICAL: Destroy the child tab and bring driver context back to the master list
                    driver.close();
                    driver.switchTo().window(mainTab);
                    logger.debug("Closed detail tab and switched back to main tab");

                    if (hasParking) {
                        matchingHospitals.add(hospitalName);
                        logger.info("✅ SUCCESS MATCH ({}/10): {}", matchingHospitals.size(), hospitalName);
                        System.out.println("✅ SUCCESS MATCH (" + matchingHospitals.size() + "/10): " + hospitalName);
                    }
                }


            } catch (Exception e) {
                // Safeguard against missing structural attributes or shadow components
                logger.error("Skipped card [{}] due to error: {}", hospitalName, e.toString());
                System.out.println(" 🛑 Skipped card [" + hospitalName + "] due to error: " + e.toString());
                e.printStackTrace();

                // Clean up tab safety net if error happens while focus is still stuck on a detail tab
                if (driver.getWindowHandles().size() > 1) {
                    logger.debug("Cleaning up extra tabs after error");
                    driver.close();
                    driver.switchTo().window(mainTab);
                }
                continue;
            }
        }
        logger.info("Filtered hospital search completed. Found {} hospitals matching criteria", matchingHospitals.size());
        return matchingHospitals;
    }


    //3. Get list of hospital names
    //4. Print out the names
    public List<String> getHospitalNames() {
        logger.info("Retrieving hospital names");
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.visibilityOfAllElements(hospitalNames));
        logger.debug("Hospital name elements are visible");

        List<String> hospitals = new ArrayList<>();

        for(WebElement hospital : hospitalNames) {
            String hospitalName = hospital.getText();
            hospitals.add(hospitalName);
            logger.debug("Added hospital: {}", hospitalName);
        }

        logger.info("Retrieved {} hospital names", hospitals.size());
        return hospitals;
    }

    public void printHospitalNames() {
        logger.info("Printing hospital names");
        List<String> hospitals = getHospitalNames();

        System.out.println("Hospitals matching the filters:");
        logger.info("Hospitals matching the filters:");

        for(String hospital : hospitals) {
            System.out.println(hospital);
            logger.debug("Hospital: {}", hospital);
        }
        logger.info("Completed printing {} hospitals", hospitals.size());
    }

}

