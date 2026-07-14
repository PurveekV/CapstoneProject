package com.tests.practo;

import com.practo.pages.HomePage;
import com.practo.utils.ConfigReader;
import com.practo.utils.DriverFactory;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class HospitalFinder extends BaseTest { // Fixed inheritance

    private static final Logger logger = LogManager.getLogger(HospitalFinder.class);

    @BeforeMethod
    public void setup(){
        DriverFactory.createDriver("chrome", false);

        String url = ConfigReader.getProperty("baseUrl");
        DriverFactory.getDriver().get(url);
    }

    @Test
    public void correctPageLoaded(){
        logger.info("Asserting that home page is loaded");
        getExtentTest().info("The correct page is loaded");
        Allure.description("The correct page is loaded");
        Assert.assertTrue(DriverFactory.getDriver().getTitle().contains("Practo"));
    }

    @Test
    public void testSearchFunctionality() {
        logger.info("Starting search functionality test...");
        HomePage homePage = new HomePage();
        homePage.searchCity("Bangalore");
        homePage.searchHospital("Hospital");

        List<String> filteredHospitalsList = homePage.getFilteredHospitals();
        logger.info("The hospitals with parking, rating > 3.5, and open 24/7 are: " + filteredHospitalsList);
        getExtentTest().info("The hospitals with parking, rating > 3.5, and open 24/7 are: " + filteredHospitalsList);
        Allure.addAttachment(
                "Filtered Hospitals",
                String.join("\n", filteredHospitalsList)
        );
    }


    @AfterMethod
    public void tearDown() throws InterruptedException{
        Thread.sleep(1000);
        DriverFactory.quitDriver();
    }
}
