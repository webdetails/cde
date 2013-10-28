package pt.webdetails.cdf.dd.plugin.resource;

import java.io.UnsupportedEncodingException;

import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.webdetails.cpf.resources.IResourceLoader;

public class ResourceLoader implements IResourceLoader{

	protected Logger logger = LoggerFactory.getLogger(getClass());
	private IPluginResourceLoader pluginResourceLoader;
	
	public ResourceLoader(IPluginResourceLoader pluginResourceLoader){
		this.pluginResourceLoader = pluginResourceLoader;
	}
	
	@Override
	public String getPluginSetting(Class<?> arg0, String arg1) {
		return pluginResourceLoader.getPluginSetting(arg0, arg1);
	}

	@Override
	public String getResourceAsString(Class<? extends Object> arg0, String arg1) {
		try {
			return pluginResourceLoader.getResourceAsString(arg0, arg1);
		} catch (UnsupportedEncodingException e) {
			logger.error("ResourceLoader.getResourceAsString()", e);
		}
		return null;
	}
}
