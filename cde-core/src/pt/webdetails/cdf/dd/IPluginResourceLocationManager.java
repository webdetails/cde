package pt.webdetails.cdf.dd;

import java.util.List;

import pt.webdetails.cdf.dd.packager.PathOrigin;

public interface IPluginResourceLocationManager {
	
	public String getMessagePropertiesResourceLocation();
	
	public String getStyleResourceLocation(String styleName);
	
	List<PathOrigin> getCustomComponentsLocations();
}
