package pt.webdetails.cdf.dd;

public interface IPluginResourceLocationManager {
	
	public String getMessagePropertiesResourceLocation();
	
	public String getStyleResourceLocation(String styleName);
	
	public String[] getAllCustomComponentsResourceLocations();
}
