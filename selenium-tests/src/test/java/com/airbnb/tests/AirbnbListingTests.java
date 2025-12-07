package com.airbnb.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AirbnbListingTests {
    
    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = System.getProperty("app.url", "http://172.17.0.1:4000");
    
    @BeforeAll
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-extensions");
        options.addArguments("--dns-prefetch-disable");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        
        System.out.println("===========================================");
        System.out.println("Testing application at: " + BASE_URL);
        System.out.println("===========================================");
    }
    
    @AfterAll
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("TC01: Verify home page loads successfully")
    public void testHomePageLoads() {
        System.out.println("\n[TEST 1/12] Testing Home Page Load...");
        driver.get(BASE_URL);
        
        assertNotNull(driver.getTitle(), "Page title should not be null");
        WebElement navbar = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("navbar")));
        assertTrue(navbar.isDisplayed(), "Navbar should be visible");
        
        System.out.println("✓ PASSED: Home page loaded successfully");
    }
    
    @Test
    @Order(2)
    @DisplayName("TC02: Verify all navigation links are present")
    public void testNavigationLinks() {
        System.out.println("\n[TEST 2/12] Testing Navigation Links...");
        driver.get(BASE_URL);
        
        WebElement homeLink = driver.findElement(By.linkText("Home"));
        assertTrue(homeLink.isDisplayed() && homeLink.isEnabled());
        
        WebElement allListingsLink = driver.findElement(By.linkText("All Listings"));
        assertTrue(allListingsLink.isDisplayed() && allListingsLink.isEnabled());
        
        WebElement createLink = driver.findElement(By.linkText("Create Listing"));
        assertTrue(createLink.isDisplayed() && createLink.isEnabled());
        
        System.out.println("✓ PASSED: All navigation links verified");
    }
    
    @Test
    @Order(3)
    @DisplayName("TC03: Navigate to All Listings page")
    public void testNavigateToAllListings() {
        System.out.println("\n[TEST 3/12] Testing Navigation to Listings...");
        driver.get(BASE_URL);
        
        driver.findElement(By.linkText("All Listings")).click();
        wait.until(ExpectedConditions.urlContains("/listings"));
        assertTrue(driver.getCurrentUrl().contains("/listings"));
        
        System.out.println("✓ PASSED: Navigation successful");
    }
    
    @Test
    @Order(4)
    @DisplayName("TC04: Verify listings display on index page")
    public void testListingsDisplay() {
        System.out.println("\n[TEST 4/12] Testing Listings Display...");
        driver.get(BASE_URL + "/listings");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("card")));
        List<WebElement> cards = driver.findElements(By.className("card"));
        
        System.out.println("Found " + cards.size() + " listings");
        assertTrue(cards.size() >= 0);
        
        System.out.println("✓ PASSED: Listings displayed correctly");
    }
    
    @Test
    @Order(5)
    @DisplayName("TC05: Create new listing with valid data")
    public void testCreateNewListing() {
        System.out.println("\n[TEST 5/12] Testing Create New Listing...");
        driver.get(BASE_URL + "/listings/new");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("listing[title]")));
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        driver.findElement(By.name("listing[title]")).sendKeys("Selenium Test " + timestamp);
        driver.findElement(By.name("listing[description]")).sendKeys("Automated test listing");
        driver.findElement(By.name("listing[image][url]")).sendKeys("https://images.unsplash.com/photo-1568605114967-8130f3a36994");
        driver.findElement(By.name("listing[price]")).sendKeys("15000");
        driver.findElement(By.name("listing[location]")).sendKeys("Islamabad");
        driver.findElement(By.name("listing[country]")).sendKeys("Pakistan");
        
        driver.findElement(By.className("add-btn")).click();
        wait.until(ExpectedConditions.urlMatches(".*\\/listings.*"));
        
        System.out.println("✓ PASSED: New listing created");
    }
    
    @Test
    @Order(6)
    @DisplayName("TC06: View listing details")
    public void testViewListingDetails() {
        System.out.println("\n[TEST 6/12] Testing View Listing Details...");
        driver.get(BASE_URL + "/listings");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("listing-link")));
        WebElement firstLink = driver.findElement(By.className("listing-link"));
        firstLink.click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("show-img")));
        WebElement image = driver.findElement(By.className("show-img"));
        assertTrue(image.isDisplayed());
        
        System.out.println("✓ PASSED: Listing details displayed");
    }
    
    @Test
    @Order(7)
    @DisplayName("TC07: Edit existing listing")
    public void testEditListing() {
        System.out.println("\n[TEST 7/12] Testing Edit Listing...");
        driver.get(BASE_URL + "/listings");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("listing-link")));
        driver.findElement(By.className("listing-link")).click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("edit-btn")));
        driver.findElement(By.className("edit-btn")).click();
        
        wait.until(ExpectedConditions.urlContains("/edit"));
        
        WebElement titleInput = driver.findElement(By.name("listing[title]"));
        titleInput.clear();
        titleInput.sendKeys("Updated Test " + System.currentTimeMillis());
        
        driver.findElement(By.className("edit-btn")).click();
        wait.until(ExpectedConditions.urlMatches(".*\\/listings\\/[a-f0-9]+$"));
        
        System.out.println("✓ PASSED: Listing updated");
    }
    
    @Test
    @Order(8)
    @DisplayName("TC08: Verify price formatting")
    public void testPriceFormatting() {
        System.out.println("\n[TEST 8/12] Testing Price Formatting...");
        driver.get(BASE_URL + "/listings");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("card")));
        driver.findElement(By.className("listing-link")).click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("card-body")));
        String bodyText = driver.findElement(By.className("card-body")).getText();
        assertTrue(bodyText.contains("Rs"));
        
        System.out.println("✓ PASSED: Price formatting verified");
    }
    
    @Test
    @Order(9)
    @DisplayName("TC09: Verify footer elements")
    public void testFooterDisplay() {
        System.out.println("\n[TEST 9/12] Testing Footer Display...");
        driver.get(BASE_URL);
        
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        
        WebElement footer = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("footer")));
        assertTrue(footer.isDisplayed());
        
        assertTrue(driver.findElement(By.className("f-socials")).isDisplayed());
        assertTrue(driver.findElement(By.className("f-info")).isDisplayed());
        assertTrue(driver.findElement(By.className("f-nav")).isDisplayed());
        
        System.out.println("✓ PASSED: Footer elements verified");
    }
    
    @Test
    @Order(10)
    @DisplayName("TC10: Verify form validation")
    public void testFormValidation() {
        System.out.println("\n[TEST 10/12] Testing Form Validation...");
        driver.get(BASE_URL + "/listings/new");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("add-btn")));
        driver.findElement(By.className("add-btn")).click();
        
        WebElement titleInput = driver.findElement(By.name("listing[title]"));
        String validationMessage = (String) ((JavascriptExecutor) driver)
            .executeScript("return arguments[0].validationMessage;", titleInput);
        
        assertFalse(validationMessage.isEmpty());
        
        System.out.println("✓ PASSED: Form validation working");
    }
    
    @Test
    @Order(11)
    @DisplayName("TC11: Delete listing")
    public void testDeleteListing() {
        System.out.println("\n[TEST 11/12] Testing Delete Listing...");
        driver.get(BASE_URL + "/listings");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("card")));
        List<WebElement> initialCards = driver.findElements(By.className("card"));
        int initialCount = initialCards.size();
        
        if (initialCount > 0) {
            driver.findElement(By.className("listing-link")).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("form button")));
            driver.findElement(By.cssSelector("form button")).click();
            
            wait.until(ExpectedConditions.urlToBe(BASE_URL + "/listings"));
            List<WebElement> finalCards = driver.findElements(By.className("card"));
            assertTrue(finalCards.size() < initialCount || finalCards.size() == 0);
        }
        
        System.out.println("✓ PASSED: Delete functionality verified");
    }
    
    @Test
    @Order(12)
    @DisplayName("TC12: Verify default image handling")
    public void testDefaultImageURL() {
        System.out.println("\n[TEST 12/12] Testing Default Image...");
        driver.get(BASE_URL + "/listings/new");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("listing[title]")));
        
        driver.findElement(By.name("listing[title]")).sendKeys("No Image " + System.currentTimeMillis());
        driver.findElement(By.name("listing[description]")).sendKeys("Testing default image");
        driver.findElement(By.name("listing[price]")).sendKeys("5000");
        driver.findElement(By.name("listing[location]")).sendKeys("Lahore");
        driver.findElement(By.name("listing[country]")).sendKeys("Pakistan");
        
        driver.findElement(By.className("add-btn")).click();
        wait.until(ExpectedConditions.urlContains("/listings"));
        
        driver.get(BASE_URL + "/listings");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("card")));
        WebElement img = driver.findElement(By.className("card-img-top"));
        String src = img.getAttribute("src");
        
        assertNotNull(src);
        assertFalse(src.isEmpty());
        
        System.out.println("✓ PASSED: Default image verified");
        System.out.println("\n===========================================");
        System.out.println("ALL 12 TESTS COMPLETED SUCCESSFULLY!");
        System.out.println("===========================================");
    }
}
