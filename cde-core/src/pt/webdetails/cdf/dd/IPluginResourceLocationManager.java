package pt.webdetails.cdf.dd;

public interface IPluginResourceLocationManager {
	
	public String getPluginLocation();
	
	public String getMessagePropertiesResourceLocation();
	
	//public String[] getAllStylesResourceLocations();
	
	public String getStyleResourceLocation(String styleName);
	
	public String[] getAllCustomComponentsResourceLocations();
	
	//public String getCustomComponentResourceLocation(String customComponentName);
}
