package pt.webdetails.cdf.dd;

import java.util.List;

import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cpf.repository.api.IReadAccess;

public interface IPluginResourceLocationManager {
	
	public String getMessagePropertiesResourceLocation();
	
	public String getStyleResourceLocation(String styleName);
	
	List<PathOrigin> getCustomComponentsLocations();
}
