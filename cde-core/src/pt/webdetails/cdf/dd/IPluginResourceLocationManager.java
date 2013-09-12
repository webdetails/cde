package pt.webdetails.cdf.dd;

import pt.webdetails.cpf.repository.api.IReadAccess;

public interface IPluginResourceLocationManager {
	
	public String getMessagePropertiesResourceLocation();
	
	public String getStyleResourceLocation(String styleName);
	
	public IReadAccess[] getAllCustomComponentsResourceLocations();
}
