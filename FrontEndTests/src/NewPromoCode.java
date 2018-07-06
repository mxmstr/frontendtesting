import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;


public class NewPromoCode extends FrontEndTest {
	
	private void addNewPromoCode(String name, String percent, String code) throws InterruptedException {
		
		click(select.ControlPanel_Coupon);
		click(select.ControlPanel_Coupon_Add);
		
		Thread.sleep(1000);
	    
		clear(select.ControlPanel_Coupon_Name);
		sendKeys(select.ControlPanel_Coupon_Name, name);
	    clear(select.ControlPanel_Coupon_Percent);
	    sendKeys(select.ControlPanel_Coupon_Percent, percent);
	    clear(select.ControlPanel_Coupon_Code);
	    sendKeys(select.ControlPanel_Coupon_Code, code);
	    
	    click(select.ControlPanel_Coupon_Create);
	    
	}
	
	@Test
	public void run() throws InterruptedException {
		
		System.out.println("//");
		System.out.println("// Testing Add Promo Code");
		System.out.println("//");
		
		click(select.Header_Account);
		
    	Thread.sleep(1000);
    	
    	click(select.Account_ControlPanel);
		click(select.ControlPanel_Coupon);
		
		Thread.sleep(1000);
		
		addNewPromoCode(
				System.getProperty("codeName"), 
				System.getProperty("codePercent"), 
				System.getProperty("codeValue")
				);
		
		Thread.sleep(1000);
		
		clickJS(select.Header_Logo_ControlPanel);
		
		
		System.out.println("//");
		System.out.println("// Testing Valid Promo Code");
		System.out.println("//");
		
		add1ItemToCart();
		openCart();
		
		Thread.sleep(1000);
    	
		String subtotal1 = getElement(select.Cart_Subtotal).getText();
    	redeemPromoCode(System.getProperty("codeValue"));
    	
    	Thread.sleep(1000);
    	
    	String subtotal2 = getElement(select.Cart_Subtotal).getText();
    	
    	Thread.sleep(1000);
    	
    	Assert.assertTrue("New code was not accepted!", !textOnPage("Code is invalid or already used."));
    	
    	Assert.assertTrue(
    			"Discount was not applied!", 
    			Double.parseDouble(subtotal2.replaceAll("[$ ]","")) / 
    			Double.parseDouble(subtotal1.replaceAll("[$ ]","")) < 1.0
    			);
    	
    	sendEscapeKey();
    	
    	
    	System.out.println("//");
		System.out.println("// Testing Remove Promo Code");
		System.out.println("//");
    	
    	driver.findElement(By.linkText("Account")).click();
    	Thread.sleep(1000);
    	
    	driver.findElement(By.linkText("Control Panel")).click();
		driver.findElement(By.linkText("Coupons")).click();
		
		Thread.sleep(1000);
    	
    	removeTableElement(System.getProperty("codeName"));
    	
    	Thread.sleep(1000);
    	
    	click(select.Header_Logo_ControlPanel);
    	
    	
    	add10ItemsToCart();
    	
    	Thread.sleep(1000);
    	
    	openCart();
    	
		Thread.sleep(1000);
    	
    	redeemPromoCode(System.getProperty("codeValue"));
    	
    	Thread.sleep(1000);
    	
    	Assert.assertTrue("Old code was accepted!", textOnPage("Code is invalid or already used."));
    	
    	sendEscapeKey();
		
	}
	
}
