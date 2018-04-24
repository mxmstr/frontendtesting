import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;


public class NutreTests {
	
	private static String driverPath = "/users/ericlynch/documents/chromedriver";
	private static String homePage = "http://dev.gonutre.com";
	private static String email = "lynch.er18@gmail.com";
	private static String password = "123123";
	
	private WebDriver driver;
	private ArrayList<AsyncTester> threads;
	
	
	private boolean isElementPresent(By by) {
		
		try {
			driver.findElement(by);
			return true;
	    } 
		catch (NoSuchElementException e) {
			return false;
	    }
		
	}
	
	private void addThread(Thread t) {
		
		AsyncTester test = new AsyncTester(t);
        test.start();
        threads.add(test);
		
	}
	
	private void joinThreads() {
		
		for (AsyncTester test : threads) {
			try {
				test.join();
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			catch (java.lang.AssertionError e) {
				System.out.println("Assertion error");
				fail();
			}
		}
		
	}
	
	private void signIn(){
		
	    try {
	    	driver.findElement(By.linkText("Log In")).click();
		    driver.findElement(By.cssSelector("input[type='email']")).clear();
		    driver.findElement(By.cssSelector("input[type='email']")).sendKeys(email);
		    driver.findElement(By.cssSelector("input[type='password']")).clear();
		    driver.findElement(By.cssSelector("input[type='password']")).sendKeys(password);
		    driver.findElement(By.xpath("//button[contains(.,'Log In')]")).click();
		    
			Thread.sleep(500);
		} 
	    catch (InterruptedException e) {
			e.printStackTrace();
		}
	    
	}
	
	@Before
	public void setUp() throws Exception {
		
		System.setProperty("webdriver.chrome.driver", driverPath);
		
		threads = new ArrayList<AsyncTester>();
		
		while (true) {
			
			try {
				driver = new ChromeDriver();
				break;
			}
			catch (WebDriverException e) {}
			
		}
        
        driver.manage().window().maximize();
        driver.manage().window().setSize(
        		new Dimension(
        				1440,
        				driver.manage().window().getSize().getHeight()));
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        driver.get(homePage);
        
        signIn();
	
	}
	
	//@Test
	public void HeaderFooter() {
		
        addThread(new Search(driver, homePage));
        joinThreads();
		
	}
	
	private void removePaymentInfo() {
		
		driver.findElement(By.linkText("Account")).click();
	    driver.findElement(By.linkText("Payment Information")).click();
	    
	    if (isElementPresent(By.xpath("//div[@id='root']/div/div/div[2]/div/div/div[2]/div[3]/div/span"))) {
	    	driver.findElement(By.xpath("//div[@id='root']/div/div/div[2]/div/div/div[2]/div[3]/div/span")).click();
	    	driver.findElement(By.xpath("//div[@id='root']/div/div/div[2]/div/div/div[2]/div[3]/div/div[3]/table/tbody/tr/td[5]/button")).click();
	    }
	    	
	}
	
	private void add10ItemsToCart() throws InterruptedException {
		
	    driver.findElement(By.linkText("A la Carte")).click();
	    driver.findElement(By.xpath("//label[contains(.,'Dinner')]")).click();
	    
	    Thread.sleep(1000);
	    
	    new Select(driver.findElement(By.cssSelector("select"))).selectByVisibleText("5");
	    new Select(driver.findElement(By.xpath("//div[@id='root']/div/div/div[2]/div[2]/div[2]/div[2]/div/div/div[2]/div/div/select"))).selectByVisibleText("5");
	    
	    Thread.sleep(1000);
	    
	    driver.findElement(By.cssSelector("button[name=\"Soup 1\"]")).click();
	    driver.findElement(By.cssSelector("button[name=\"Pizza\"]")).click();
	    
	}
	
	private void redeemPromoCode(String code) {
			
		driver.findElement(By.cssSelector("input.pt-input.pt-fill")).clear();
	    driver.findElement(By.cssSelector("input.pt-input.pt-fill")).sendKeys(code);
	    driver.findElement(By.cssSelector("button.cart__promocode-btn")).click();
    	
	}
	
	@Test
	public void checkoutItems() throws InterruptedException {
		
		String bodyText;
		
		removePaymentInfo();
		
    	add10ItemsToCart();
    	Thread.sleep(1000);
    	
    	driver.findElement(By.cssSelector(
				"button.pt-button.pt-minimal.pt-icon-shopping-cart.topbar__cart-btn.topbar__ma-top-8")).click();
    	Thread.sleep(1000);
    	
    	
    	redeemPromoCode("123123");
    	Thread.sleep(1000);
    	
    	bodyText = driver.findElement(By.tagName("body")).getText();
    	Assert.assertTrue("Used code accepted!", bodyText.contains("Code is invalid or already used."));
    	
    	
    	redeemPromoCode("abc");
    	Thread.sleep(1000);
    	
    	bodyText = driver.findElement(By.tagName("body")).getText();
    	Assert.assertTrue("Invalid code accepted!", bodyText.contains("Code is invalid or already used."));
		
    	
    	Assert.assertTrue(
    			"Checkout without shipping info!",
    			!driver.findElement(By.cssSelector("button.cart__checkout-btn")).isEnabled());
    	
    	
    	driver.findElement(By.cssSelector(".pt-intent-orange")).click();
    	Thread.sleep(1000);
	    
    	driver.findElement(By.xpath("//label[contains(.,'Free Pick up')]")).click();
    	driver.findElement(By.xpath("//label[contains(.,'Free Pick up')]")).click();
    	driver.findElement(By.cssSelector("button.pt-button:nth-child(8)")).click();
	    Thread.sleep(1000);
    	
        driver.findElement(By.cssSelector("button.cart__checkout-btn")).click();
        Thread.sleep(1000);
        
        //driver.findElement(By.cssSelector("div.StripeElement.StripeElement--empty")).click();
        //driver.findElement(By.cssSelector("div.StripeElement.StripeElement--empty")).clear();
        
        //driver.findElement(By.cssSelector(".__PrivateStripeElement > iframe:nth-child(1)")).click();
        
        driver.switchTo().frame(driver.findElement(By.cssSelector(
        		".__PrivateStripeElement > iframe:nth-child(1)")));
        
        driver.findElement(By.name("cardnumber")).clear();
        driver.findElement(By.name("cardnumber")).sendKeys("4242 4242 4242 4242");
        driver.findElement(By.name("exp-date")).clear();
        driver.findElement(By.name("exp-date")).sendKeys("‎02 / ‎20");
        driver.findElement(By.name("cvc")).clear();
        driver.findElement(By.name("cvc")).sendKeys("123");
        driver.findElement(By.name("postal")).clear();
        driver.findElement(By.name("postal")).sendKeys("02116");
        
        driver.switchTo().defaultContent();
        
        driver.findElement(By.xpath("//form/div/div/button")).click();
    	
        Thread.sleep(5000);
        
	}

	private void changeAccountName(String name) {
		
		driver.findElement(By.xpath("//div[@id='root']/div/div/div[2]/div/div/div[2]/h2/span")).click();
		
	    WebElement nameField = driver.findElement(By.cssSelector("div.content__input-ma:nth-child(1) > div:nth-child(2) > input:nth-child(1)"));
	    nameField.clear();
	    nameField.sendKeys(name);
	    
	    driver.findElement(By.cssSelector("button.nu-button-h.content__button")).click();
	    driver.findElement(By.xpath("//div[@id='root']/div/div/div[2]/div/div/div[2]/h2/span")).click();
		
	}
	
	//@Test
	public void changeAccountDetails() throws InterruptedException {
		
		driver.findElement(By.linkText("Account")).click();
    	Thread.sleep(1000);
		
	    changeAccountName("Joe");
	    Thread.sleep(1000);
	    
	    Assert.assertTrue(
	    		"Edit module did not update!",
	    		driver.findElement(By.cssSelector(
	    				"div.row:nth-child(3) > div:nth-child(2)")).getText().contains("Joe"));
	    
	    changeAccountName("John");
	    
	    driver.findElement(By.linkText("Sign Out")).click();
	    Thread.sleep(1000);
	    
	    Assert.assertTrue("Sign out access not downgraded!", !isElementPresent(By.linkText("Account")));
	    
	}
	
	private void changeItemPrice(String price) {
		
        driver.findElement(By.cssSelector("span.app__psedo-link")).click();
        driver.findElement(By.cssSelector("div.pt-input-group.pt-large > input.pt-input")).clear();
        driver.findElement(By.cssSelector("div.pt-input-group.pt-large > input.pt-input")).sendKeys(price);
        driver.findElement(By.xpath("//div[12]/button")).click();
		
	}
	
	
	private void testEditModule() throws InterruptedException {
		
		changeItemPrice("10.00");
        Thread.sleep(1000);
        
        Assert.assertTrue(
        		"Edit module did not update!",
	    		driver.findElement(By.cssSelector(
		        		".pt-table > tbody:nth-child(2) > tr:nth-child(1) > td:nth-child(3)")).getText().contains("10.00"));
    	
        changeItemPrice("9.00");
        Thread.sleep(1000);
		
	}
	
	private void testMealPanel() throws InterruptedException {
		
		String bodyText;
		
		driver.findElement(By.xpath("//div[@id='root']/div/div/div/div/div/div/div[2]/button")).click();
		driver.findElement(By.cssSelector("input.pt-fill:nth-child(2)")).clear();
		driver.findElement(By.cssSelector("input.pt-fill:nth-child(2)")).sendKeys("New Meal");
	    driver.findElement(By.cssSelector("div.pt-input-group.pt-large > input.pt-input")).clear();
	    driver.findElement(By.cssSelector("div.pt-input-group.pt-large > input.pt-input")).sendKeys("20.00");
	    new Select(driver.findElement(By.cssSelector("select"))).selectByVisibleText("Dinner");
	    driver.findElement(By.xpath("//div[8]/label/textarea")).clear();
	    driver.findElement(By.xpath("//div[8]/label/textarea")).sendKeys("a, b, c");
	    driver.findElement(By.xpath("//div[12]/button")).click();
		
	    bodyText = driver.findElement(By.tagName("body")).getText();
    	Assert.assertTrue("Meal wasn't added!", bodyText.contains("New Meal"));
		
    	
    	WebElement baseTable = driver.findElement(By.cssSelector(".pt-table"));
    	List<WebElement> tableRows = baseTable.findElements(By.tagName("tr"));
    	
    	for (WebElement e : tableRows) {
    		if (e.getText().contains("New Meal")) {

    			((JavascriptExecutor)driver).executeScript("arguments[0].click();", e);
    			driver.findElement(By.cssSelector("button.pt-button:nth-child(4)")).click();
    			driver.findElement(By.cssSelector("button.pt-intent-danger:nth-child(1)")).click();
    			Thread.sleep(1000);
    			break;
    			
    		}
    	}
    	
	}
	
	private void addNewPromoCode() throws InterruptedException {
		
		driver.findElement(By.linkText("Coupons")).click();
	    driver.findElement(By.cssSelector("button.pt-button:nth-child(1)")).click();
		Thread.sleep(1000);
	    
		driver.findElement(By.cssSelector(
				"div.col-6:nth-child(1) > label:nth-child(1) > input:nth-child(2)")).clear();
		driver.findElement(By.cssSelector(
				"div.col-6:nth-child(1) > label:nth-child(1) > input:nth-child(2)")).sendKeys("test");
	    driver.findElement(By.cssSelector("div.pt-input-group.pt-large > input.pt-input")).clear();
	    driver.findElement(By.cssSelector("div.pt-input-group.pt-large > input.pt-input")).sendKeys("10");
	    driver.findElement(By.cssSelector(
	    		"div.col-6:nth-child(4) > label:nth-child(1) > input:nth-child(2)")).clear();
	    driver.findElement(By.cssSelector(
	    		"div.col-6:nth-child(4) > label:nth-child(1) > input:nth-child(2)")).sendKeys("ABC123");
	    
	    driver.findElement(By.xpath("//div[11]/button")).click();
	    
	}
	
	private void removePromoCode() throws InterruptedException {
		
		driver.findElement(By.linkText("Coupons")).click();
		Thread.sleep(1000);
	    
		
		WebElement baseTable = driver.findElement(By.cssSelector(".pt-table"));
    	List<WebElement> tableRows = baseTable.findElements(By.tagName("tr"));
    	
    	for (WebElement e : tableRows) {
    		if (e.getText().contains("test")) {

    			((JavascriptExecutor)driver).executeScript("arguments[0].click();", e);
    			driver.findElement(By.cssSelector("button.pt-button:nth-child(4)")).click();
    			driver.findElement(By.cssSelector("button.pt-intent-danger:nth-child(1)")).click();
    			Thread.sleep(1000);
    			break;
    			
    		}
    	}
	    
	}
	
	private void testNewPromoCode() throws InterruptedException {
		
		String bodyText;
		
		addNewPromoCode();
		Thread.sleep(1000);
		
		((JavascriptExecutor)driver).executeScript(
	    		"arguments[0].click();", 
	    		driver.findElement(By.cssSelector(".app-topbar__logo")));
		
		
    	add10ItemsToCart();
    	Thread.sleep(1000);
    	
    	driver.findElement(By.cssSelector(
				"button.pt-button.pt-minimal.pt-icon-shopping-cart.topbar__cart-btn.topbar__ma-top-8")).click();
    	Thread.sleep(1000);
    	
    	redeemPromoCode("abc123");
    	Thread.sleep(1000);
    	
    	bodyText = driver.findElement(By.tagName("body")).getText();
    	Assert.assertTrue("New code was not accepted!", !bodyText.contains("Code is invalid or already used."));
    	
    	Actions action = new Actions(driver);
    	action.sendKeys(Keys.ESCAPE).build().perform();
    	
    	
    	driver.findElement(By.linkText("Account")).click();
    	Thread.sleep(1000);
    	
    	driver.findElement(By.linkText("Control Panel")).click();
    	
    	removePromoCode();
    	Thread.sleep(1000);
    	
    	((JavascriptExecutor)driver).executeScript(
	    		"arguments[0].click();", 
	    		driver.findElement(By.cssSelector(".app-topbar__logo")));
    	
    	
    	add10ItemsToCart();
    	Thread.sleep(1000);
    	
    	driver.findElement(By.cssSelector(
				"button.pt-button.pt-minimal.pt-icon-shopping-cart.topbar__cart-btn.topbar__ma-top-8")).click();
    	Thread.sleep(1000);
    	
    	redeemPromoCode("abc123");
    	Thread.sleep(1000);
    	
    	bodyText = driver.findElement(By.tagName("body")).getText();
    	Assert.assertTrue("Old code was accepted!", bodyText.contains("Code is invalid or already used."));
    	
    	action.sendKeys(Keys.ESCAPE).build().perform();
    	Thread.sleep(1000);
		
	}
	
	private void addDeliveryZone() throws InterruptedException {
		
		driver.findElement(By.linkText("Delivery Zone")).click();
		driver.findElement(By.xpath("//div[@id='root']/div/div/div/div/div/div/div/button")).click();
		Thread.sleep(1000);
		
		driver.findElement(By.cssSelector("input.pt-fill")).clear();
		driver.findElement(By.cssSelector("input.pt-fill")).sendKeys("New Delivery");
		driver.findElement(By.cssSelector("div.pt-input-group.pt-large > input.pt-input")).clear();
		driver.findElement(By.cssSelector("div.pt-input-group.pt-large > input.pt-input")).sendKeys("50");
		driver.findElement(By.cssSelector("textarea.pt-input.pt-fill")).clear();
		driver.findElement(By.cssSelector("textarea.pt-input.pt-fill")).sendKeys("12345");
		driver.findElement(By.cssSelector("button.pt-large")).click();
		
	}
	
	private void removeNewDelivery() throws InterruptedException {
		
		driver.findElement(By.linkText("Delivery Zone")).click();
		Thread.sleep(1000);
	    
		
		WebElement baseTable = driver.findElement(By.cssSelector(".pt-table"));
    	List<WebElement> tableRows = baseTable.findElements(By.tagName("tr"));
    	
    	for (WebElement e : tableRows) {
    		if (e.getText().contains("New Delivery")) {

    			((JavascriptExecutor)driver).executeScript("arguments[0].click();", e);
    			driver.findElement(By.cssSelector("button.pt-button:nth-child(4)")).click();
    			driver.findElement(By.cssSelector("button.pt-intent-danger:nth-child(1)")).click();
    			Thread.sleep(1000);
    			break;
    			
    		}
    	}
	    
	}
	
	private void validateDeliveryZone() throws InterruptedException {
		
		driver.findElement(By.linkText("Account")).click();
	    driver.findElement(By.linkText("Delivery Information")).click();
	    
	    ((JavascriptExecutor)driver).executeScript(
	    		"arguments[0].click();", 
	    		driver.findElement(By.cssSelector(".account__edit")));
	    Thread.sleep(1000);
	    driver.findElement(By.cssSelector("a.pt-button:nth-child(1)")).click();
	    Thread.sleep(1000);
	    
	    
	    driver.findElement(By.cssSelector(
	    		"div.account__box:nth-child(4) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(1)")).clear();
	    driver.findElement(By.cssSelector(
	    		"div.account__box:nth-child(4) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(1)")).sendKeys("Street");
	    driver.findElement(By.cssSelector(
	    		"div.content__input-ma:nth-child(2) > div:nth-child(2) > label:nth-child(1) > input:nth-child(1)")).clear();
	    driver.findElement(By.cssSelector(
	    		"div.content__input-ma:nth-child(2) > div:nth-child(2) > label:nth-child(1) > input:nth-child(1)")).sendKeys("City");
	    driver.findElement(By.cssSelector(
	    		"label.pt-label:nth-child(3) > input:nth-child(1)")).clear();
	    driver.findElement(By.cssSelector(
	    		"label.pt-label:nth-child(3) > input:nth-child(1)")).sendKeys("12345");
	    driver.findElement(By.cssSelector(
	    		"div.content__input-value > input.pt-input.pt-large")).clear();
	    driver.findElement(By.cssSelector(
	    		"div.content__input-value > input.pt-input.pt-large")).sendKeys("(555) 555-5555");
	    driver.findElement(By.xpath("//label[contains(.,'Set as default')]")).click();
	    driver.findElement(By.cssSelector("button.nu-button-h.content__button")).click();
	    
	    driver.findElement(By.cssSelector(".pt-button")).click();
	    driver.findElement(By.cssSelector(".pt-intent-orange")).click();
	    
	    Thread.sleep(1000);
	    driver.findElement(By.cssSelector("div.row:nth-child(5) > div:nth-child(2) > label:nth-child(1)")).click();
	    driver.findElement(By.cssSelector("button.pt-button:nth-child(8)")).click();
	    Thread.sleep(1000);
	    
	    String bodyText = driver.findElement(By.tagName("body")).getText();
    	Assert.assertTrue("New zip code not accepted!", !bodyText.contains("Sorry, we're not servicing your area at this time."));
    	
    	Actions action = new Actions(driver);
    	action.sendKeys(Keys.ESCAPE).build().perform();
    	Thread.sleep(1000);
	    
	}
	
	private void testNewDeliveryZone() throws InterruptedException {
	
		addDeliveryZone();
		
		((JavascriptExecutor)driver).executeScript(
	    		"arguments[0].click();", 
	    		driver.findElement(By.cssSelector(".app-topbar__logo")));
		Thread.sleep(1000);
    	
		
    	validateDeliveryZone();
    	
    	driver.findElement(By.linkText("Control Panel")).click();
		Thread.sleep(1000);
    	
		
		removeNewDelivery();
		
	}
	
	
	//@Test
	public void testControlPanel() throws InterruptedException {
		
		driver.findElement(By.linkText("Account")).click();
    	Thread.sleep(1000);
    	
    	driver.findElement(By.linkText("Control Panel")).click();
    	
    	//testEditModule();
    	//testMealPanel();
    	//testNewPromoCode();
    	testNewDeliveryZone();
        
	}
	
	@After
	public void tearDown() {
		
		driver.quit();
		
	}

}
