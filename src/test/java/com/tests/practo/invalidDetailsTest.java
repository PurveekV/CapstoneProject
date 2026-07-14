package com.tests.practo;


import com.practo.pages.HomePage;
import com.practo.pages.CorporateFormPage;
import com.practo.utils.ConfigReader;
import com.practo.utils.DriverFactory;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

public class invalidDetailsTest extends BaseTest{

    private static final Logger logger = LogManager.getLogger(invalidDetailsTest.class);

    @BeforeMethod
    public void setup(){

        String url = ConfigReader.getProperty("baseUrl");
        DriverFactory.getDriver().get(url);
        HomePage homePage = new HomePage();
        homePage.goToCoporatePage();
    }

    @Test
    public void enterInvalidFormDetails_shouldDisplayError() throws TimeoutException {
        CorporateFormPage formPage = new CorporateFormPage();
        formPage.fillInvalidForm("Tom","Jobs ltd", "tom@exmaple.com","123456789", "<500", "Taking a demo"); //The phone number does not compile to the requirements of the form
        getExtentTest().info("The details entered are: Tom, Jobs ltd, tom@exmaple.com, 07412548624, <500, Taking a demo");
        Allure.description("The details entered are: Tom, Jobs ltd, tom@exmaple.com, 07412548624, <500, Taking a demo");

        Assert.assertTrue(formPage.alertBox(), "BUG: The alert box has not appeared");

        WebElement submitBtn = formPage.getScheduleButton();
        WebDriverWait shortWait = new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(5));
        boolean isBtnDisabled = shortWait.until(ExpectedConditions.attributeToBeNotEmpty(submitBtn, "disabled"));

        Assert.assertNotNull(isBtnDisabled, "BUG: The schedule demo button is still enabled/clickable despite invalid details!");

    }
    // The test enters the incorrect and junk details into the form.
    // Ideally, the front-end should reject this to not overwhelm the backend
    // But it does in this case. Even so, there should be filtering on the backend server to reduce junk data.
    //Notice the format of the details; the name is not correctly filled out including firstname and lastname,
    // the organisation name is junk, and the email address is also junk and incorrect domain.
    // I tested using these details and the website accepted it. This tells me that the website is poorly
    // optimised for handling junk data, as confirmed by the Thank you alert. Please note that sometimes there is a
    // popup for CaptCha which can not be resolved without intervention from the developers of the website.
    // In an ideal scenario, you would ask them to turn it off before testing. But we aren't in an ideal scenario
    // and to resolve this, we would break the website.

    @Ignore
    @Test
    public void enterInvalidFormDetails_shouldAccept() throws InterruptedException {
        CorporateFormPage formPage = new CorporateFormPage();
        Thread.sleep(1500);
        formPage.fillAndSubmitForm("Tom","Jobs ltd", "tom@exmaple.com","07412548624", "<500", "Taking a demo");
        getExtentTest().info("The details entered are: Tom, Jobs ltd, tom@exmaple.com, 07412548624, <500, Taking a demo");
        Allure.description("The details entered are: Tom, Jobs ltd, tom@exmaple.com, 07412548624, <500, Taking a demo");

        Assert.assertTrue(formPage.confirmMessage(), "BUG: Confirmation not displayed");
        System.out.print("Confirmed that thank you message pops up");
    }

}
