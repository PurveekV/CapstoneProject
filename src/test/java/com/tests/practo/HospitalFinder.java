package com.tests.practo;

import com.practo.pages.HomePage;
import com.practo.utils.ConfigReader;
import com.practo.utils.DriverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    // You MUST have at least one @Test method for IntelliJ to run the class
    @Test
    public void testSearchFunctionality() {
        logger.info("Starting search functionality test...");
        HomePage homePage = new HomePage();
        homePage.searchCity("Bangalore");
        homePage.searchHospital("Hospital");

        System.out.println("Current URL after search: " + DriverFactory.getDriver().getCurrentUrl());

        List<String> filteredHospitalsList = homePage.getFilteredHospitals();
        System.out.println("The hospitals with parking, rating > 3.5, and open 24/7 are: "+filteredHospitalsList);

    }

    @AfterMethod
    public void tearDown() throws InterruptedException{
        Thread.sleep(1000);
        DriverFactory.quitDriver();
    }
}
