package hisa.util
{
	import flash.display.Sprite;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	import flash.external.ExternalInterface;
	
	/**
	 * navigateToURL may be intercepted by some browsers like ie7.
	 * So for different browsers, use different way to open a new url, 
	 * and go through the popup window interceptions.
	 */
	public class OpenWindowUtil
	{
		public static function openWindow(url:String, window:String="_blank", features:String=""):void{
			  
			var WINDOW_OPEN_FUNCTION:String = "window.open";
			var myURL:URLRequest = new URLRequest(url);			
			var browserName:String = getBrowserName();
			
			if(getBrowserName() == "Firefox"){
				ExternalInterface.call(WINDOW_OPEN_FUNCTION, url, window, features);
			}
			//If IE, 
			else if(browserName == "IE"){
				ExternalInterface.call(WINDOW_OPEN_FUNCTION, url, window, features);
				//ExternalInterface.call("function setWMWindow() {window.open(&apos;" + url + "&apos;);}");
			}
			//If Safari 
			else if(browserName == "Safari"){			  
				navigateToURL(myURL, window);
			}
			//If Opera 
			else if(browserName == "Opera"){	
				navigateToURL(myURL, window); 
			} else {
				navigateToURL(myURL, window);
			}
			
			/*Alternate methodology...
			   var popSuccess:Boolean = ExternalInterface.call(WINDOW_OPEN_FUNCTION, url, window, features); 
			if(popSuccess == false){
				navigateToURL(myURL, window);
			}*/
			 
		}
		private static function getBrowserName():String{
			var browser:String;
			
			//Uses external interface to reach out to browser and grab browser useragent info.
			var browserAgent:String = ExternalInterface.call("function getBrowser(){return navigator.userAgent;}");
		
			 //  Debug.text += "Browser Info: [" + browserAgent + "]";
			
			//Determines brand of browser using a find index. If not found indexOf returns (-1).
			if(browserAgent != null && browserAgent.indexOf("Firefox") >= 0) {
				browser = "Firefox";
			} 
			else if(browserAgent != null && browserAgent.indexOf("Safari") >= 0){
				browser = "Safari";
			}			 
			else if(browserAgent != null && browserAgent.indexOf("MSIE") >= 0){
				browser = "IE";
			}		 
			else if(browserAgent != null && browserAgent.indexOf("Opera") >= 0){
				browser = "Opera";
			}
			else {
				browser = "Undefined";
			}
			return browser;
		}
	}
}