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
    }

    public void goToCoporatePage(){
        wait.until(ExpectedConditions.elementToBeClickable(corporatePlanButton));
        corporatePlanButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(healthWellnessButton));
        healthWellnessButton.click();

    }

    public void goToDiagnosticsPage(){
        wait.until(ExpectedConditions.elementToBeClickable(labTestsButton));
        labTestsButton.click();

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

    //2. Apply filters (Including 24/7, Rating > 3.5, and Multi-Tab Parking Validation)
    public List<String> getFilteredHospitals() {
        WebDriverWait pageWait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 1. Explicitly wait for the cards to load in the DOM
        pageWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[@class='c-estb-card']")));

        List<String> matchingHospitals = new ArrayList<>();
        String mainTab = driver.getWindowHandle();

        for (WebElement card : hospitalCards) {
            // Stop looking completely once we have successfully captured our first 10 matches
            if (matchingHospitals.size() >= 10) {
                System.out.println("🎯 Reached target limit of 10 qualified hospitals. Stopping search loop.");
                break;
            }

            String hospitalName = "unknown";
            try {
                // 1. Extract Hospital Name
                WebElement titleElement = card.findElement(By.cssSelector("h2.line-1"));
                hospitalName = titleElement.getText().trim();

                // 2. Extract and Parse Hospital Rating (e.g., "4.5")
                double hospitalRating = 0.0;
                try {
                    String ratingText = card.findElement(By.cssSelector("div.c-feedback span.u-bold")).getText().trim();
                    hospitalRating = Double.parseDouble(ratingText);
                } catch (NoSuchElementException e) {
                    hospitalRating = 0.0;
                    System.out.println("Could not find rating for: " + hospitalName);
                }

                // 3. Check Timing Info (Flexible matching pattern)
                boolean isOpen247 = false;
                try {
                    String timingText = card.findElement(By.cssSelector("span.pd-right-2px-text-green")).getText().trim().toLowerCase();
                    if (timingText.contains("24x7") || timingText.contains("24 hours") || timingText.contains("open 24")) {
                        isOpen247 = true;
                    }
                } catch (NoSuchElementException e) {
                    isOpen247 = false;
                    System.out.println("Could not find timing info for: " + hospitalName);
                }

                // 4. If basic criteria pass, open detail view to check for Parking
                if (hospitalRating > 3.5 && isOpen247) {
                    System.out.println("⏳ " + hospitalName + " passed Gate 1. Opening tab to verify parking description...");

                    WebElement linkElement = card.findElement(By.cssSelector("a[href*='/hospital/']"));
                    String targetUrl = linkElement.getAttribute("href");

                    // Open the tab via JavaScript
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript("window.open(arguments[0], '_blank');", targetUrl);

                    // 1. Wait for the new tab to be recognized
                    pageWait.until(ExpectedConditions.numberOfWindowsToBe(2));

                    // 2. BULLETPROOF TAB SWITCH: Always grab the absolute newest handle
                    List<String> handles = new ArrayList<>(driver.getWindowHandles());
                    String newTabHandle = handles.get(handles.size() - 1); // The last one opened
                    driver.switchTo().window(newTabHandle);

                    // =================== TWIN-STRATEGY COOKIE BYPASS BLOCK ===================
                    try {
                        WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(3));

                        // STRATEGY A: Try clicking it directly in the root page DOM (No Iframe)
                        try {
                            WebElement consentBtn = popupWait.until(ExpectedConditions.elementToBeClickable(
                                    By.cssSelector("button.fc-cta-consent.fc-primary-button")
                            ));
                            js.executeScript("arguments[0].click();", consentBtn);
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
                            System.out.println("   Cookie consent overlay bypassed inside iframe container.");
                            driver.switchTo().defaultContent(); // Escape iframe back to root document
                        }
                    } catch (Exception e) {
                        // Safe fallback cleanup to keep execution context clear
                        driver.switchTo().defaultContent();
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
                            System.out.println("   Clicked 'Read More' to expand description text.");
                        } catch (TimeoutException e) {
                            System.out.println("   No 'Read More' button present (text already fully expanded).");
                        }

                        // 4. Scan the text elements now that the panel is open
                        By detailsLocator = By.cssSelector("[data-qa-id='amenity_item'], .p-entity__item, .c-description__text, .c-amenities__item");
                        List<WebElement> detailsElements = tabWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(detailsLocator));

                        for (WebElement element : detailsElements) {
                            String text = element.getText().toLowerCase().trim();
                            if (text.contains("parking") || text.contains("valet")) {
                                hasParking = true;
                                break;
                            }
                        }

                    } catch (Exception e) {
                        System.out.println("   ❌ Error checking expanded layout details for: " + hospitalName);
                    }

                    // 5. CRITICAL: Destroy the child tab and bring driver context back to the master list
                    driver.close();
                    driver.switchTo().window(mainTab);

                    if (hasParking) {
                        matchingHospitals.add(hospitalName);
                        System.out.println("✅ SUCCESS MATCH (" + matchingHospitals.size() + "/10): " + hospitalName);
                    }
                }


            } catch (Exception e) {
                // Safeguard against missing structural attributes or shadow components
                System.out.println(" 🛑 Skipped card [" + hospitalName + "] due to error: " + e.toString());
                e.printStackTrace();

                // Clean up tab safety net if error happens while focus is still stuck on a detail tab
                if (driver.getWindowHandles().size() > 1) {
                    driver.close();
                    driver.switchTo().window(mainTab);
                }
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

