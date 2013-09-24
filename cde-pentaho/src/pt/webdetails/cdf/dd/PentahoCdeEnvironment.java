package pt.webdetails.cdf.dd;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;

import pt.webdetails.cdf.dd.bean.factory.ICdeBeanFactory;
import pt.webdetails.cdf.dd.datasources.DataSourceManager;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cdf.dd.plugin.resource.PluginResourceLocationManager;
import pt.webdetails.cpf.IPluginCall;
import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.resources.IResourceLoader;

public class PentahoCdeEnvironment extends PentahoPluginEnvironment implements ICdeEnvironment {
	
	protected static Log logger = LogFactory.getLog(PentahoCdeEnvironment.class);
	
	private static final String PLUGIN_REPOSITORY_DIR = "cde";
	
	private ICdeBeanFactory factory;
	private IPluginCall interPluginCall;
	private IResourceLoader resourceLoader;
	
    private IPluginResourceLocationManager pluginResourceLocationManager;
    
    public void init(ICdeBeanFactory factory) throws InitializationException {
    	this.factory = factory;
    	
    	pluginResourceLocationManager = new PluginResourceLocationManager();
    		
		if(factory.containsBean(IResourceLoader.class.getSimpleName())){    		
			resourceLoader = (IResourceLoader)factory.getBean(IResourceLoader.class.getSimpleName());
		}
		
		if(factory.containsBean(IPluginCall.class.getSimpleName())){    		
			interPluginCall = (IPluginCall)factory.getBean(IPluginCall.class.getSimpleName());
		}
    	
		super.init(this);
    }
    
    @Override
	public void refresh() {	
    	try {
			init(this.factory);
		} catch (InitializationException e) {
			logger.error("PentahoCdeEnvironment.refresh()", e);
		}
	}
    
	@Override
	public String getApplicationBaseUrl() {
		return PentahoSystem.getApplicationContext().getBaseUrl();
	}

	@Override
	public IDataSourceManager getDataSourceManager() {
		return DataSourceManager.getInstance();
	}

	@Override
	public IPluginCall getInterPluginCall() {
		return interPluginCall;
	}

	@Override
	public Locale getLocale() {
		return LocaleHelper.getLocale();
	}

	@Override
	public IPluginResourceLocationManager getPluginResourceLocationManager() {
		return pluginResourceLocationManager;
	}
	
	@Override
	public IResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Override
	public String getPluginRepositoryDir() {
	  return PLUGIN_REPOSITORY_DIR;
	}
	
	@Override
	public String getPluginId(){
		return super.getPluginId();
	}
}
