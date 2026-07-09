package com.tests.practo;

import com.practo.pages.HomePage;
import com.practo.utils.ConfigReader;
import com.practo.pages.DiagnosticPage;
import com.practo.utils.DriverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class DiagnosticPageTest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(DiagnosticPageTest.class);

    @BeforeMethod
    public void setup(){
        DriverFactory.createDriver("chrome", false);

        String url = ConfigReader.getProperty("baseUrl");
        DriverFactory.getDriver().get(url);
        HomePage homePage = new HomePage();
        homePage.goToDiagnosticsPage();
    }

    @Test
    public void diagnosticPage_shouldReturnTopCities_storeInList_displayList(){
        DiagnosticPage diagnosticPage = new DiagnosticPage();

        if (!diagnosticPage.viewTopCities()){
            diagnosticPage.openTopCitiesWrapper();
        }

        List<String> topCities = diagnosticPage.getAllTopCities();
        System.out.println("Top Cities from UI: " + topCities);

        Assert.assertFalse(topCities.isEmpty(), "The topCities list came back empty");
        Assert.assertTrue(topCities.contains("Bangalore"), "List does not contain Bangalore");
        Assert.assertTrue(topCities.contains("Delhi"), "List does not contain Delhi");
        Assert.assertTrue(topCities.contains("Mumbai"), "List does not contain Mumbai");
        Assert.assertTrue(topCities.contains("Chennai"), "List does not contain Chennai");
        Assert.assertTrue(topCities.contains("Hyderabad"), "List does not contain Hyderabad");
        Assert.assertTrue(topCities.contains("Kolkata"), "List does not contain Kolkata");
        Assert.assertTrue(topCities.contains("Pune"), "List does not contain Pune");
        Assert.assertTrue(topCities.contains("Ahmedabad"), "List does not contain Ahmedabad");

    }

    @AfterMethod
    public void tearDown() throws InterruptedException {
        Thread.sleep(1000);
        DriverFactory.quitDriver();


    }

}
