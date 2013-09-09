package pt.webdetails.cdf.dd.plugin.resource;

import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator;
import pt.webdetails.cdf.dd.FsPluginResourceLocations;
import pt.webdetails.cdf.dd.IPluginResourceLocationManager;
import pt.webdetails.cdf.dd.cdf.CdfStyles;

public class PluginResourceLocationManager implements IPluginResourceLocationManager{

	@Override
	public String[] getAllCustomComponentsResourceLocations() {
		return FsPluginResourceLocations.getCustomComponentsRelDirs();
	}

	@Override
	public String getMessagePropertiesResourceLocation() {
		return DashboardDesignerContentGenerator.PLUGIN_PATH +"lang/messages.properties";
	}

	@Override
	public String getStyleResourceLocation(String arg0) {
		return CdfStyles.getInstance().getResourceLocation(arg0);
	}

	@Override
	public String getPluginLocation() {
		return DashboardDesignerContentGenerator.PLUGIN_PATH;
	}

}
