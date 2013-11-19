package pt.webdetails.cdf.dd;

import java.util.List;

import pt.webdetails.cpf.packager.origin.PathOrigin;

public interface IPluginResourceLocationManager {
	
	public String getMessagePropertiesResourceLocation();
	
	public String getStyleResourceLocation(String styleName);
	
	List<PathOrigin> getCustomComponentsLocations();
}
