package pt.webdetails.cdf.dd.plugin.resource;

import java.util.List;

import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator;
import pt.webdetails.cdf.dd.FsPluginResourceLocations;
import pt.webdetails.cdf.dd.IPluginResourceLocationManager;
import pt.webdetails.cdf.dd.cdf.CdfStyles;
import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cpf.repository.api.IReadAccess;

public class PluginResourceLocationManager implements IPluginResourceLocationManager{

    private FsPluginResourceLocations componentLocator;

	@Override
	public IReadAccess[] getAllCustomComponentsResourceLocations() {
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

    public synchronized List<PathOrigin> getCustomComponentsLocations() {
      if (componentLocator == null) {
        componentLocator = new FsPluginResourceLocations();
      }
      return componentLocator.getCustomComponentLocations();
    }
}
