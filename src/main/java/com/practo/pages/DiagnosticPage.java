package com.practo.pages;

import com.practo.utils.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class DiagnosticPage {

    private WebDriver driver;
    private static WebDriverWait wait;

    public DiagnosticPage() {
        this.driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    //Find elements

    @FindBy(css = ".dg-inner-wrapper")
    WebElement topCitiesWrapper;

    @FindBy(css = ".icon-ic_dropdown c-search__dropdown-icon")
    WebElement locationDropBox;

    @FindBy(css = ".u-margint--standard.o-f-color--primary")
    private List<WebElement> topCities;


    public void openTopCitiesWrapper() {
        wait.until(ExpectedConditions.elementToBeClickable(locationDropBox));
        locationDropBox.click();
    }

    public boolean viewTopCities(){
        return topCitiesWrapper.isDisplayed();
    }

    public List<String> getAllTopCities(){
        List<String> cityNames = new ArrayList<>();
        wait.until(ExpectedConditions.visibilityOfAllElements(topCities));
        for (WebElement cityElement : topCities){
            String name = cityElement.getText().trim();
            cityNames.add(name);
            System.out.println("- " + name);
        }
        System.out.println("-------------------------");

        return cityNames;
    }


}
