package com.tests.practo;


import com.practo.pages.HomePage;
import com.practo.pages.CorporateFormPage;
import com.practo.utils.ConfigReader;
import com.practo.utils.DriverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class invalidDetailsTest {

    private static final Logger logger = LogManager.getLogger(HospitalFinder.class);

    @BeforeMethod
    public void setup(){
        DriverFactory.createDriver("chrome", false);

        String url = ConfigReader.getProperty("baseUrl");
        DriverFactory.getDriver().get(url);
        HomePage homePage = new HomePage();
        homePage.goToCoporatePage();
    }

    @Test
    public void enterInvalidDetails_shouldDisplayError() throws InterruptedException {
        CorporateFormPage formPage = new CorporateFormPage();
        Thread.sleep(1500);
        formPage.fillAndSubmitForm("Tom","Jobs ltd", "tom@exmaple.com","07412548624", "<500", "Taking a demo");

        Assert.assertTrue(formPage.confirmMessage());
        System.out.print("Confirmed that thank you message pops up");

    }

    @AfterMethod
    public void tearDown() throws InterruptedException {
        Thread.sleep(1000);
        DriverFactory.quitDriver();
    }
}
