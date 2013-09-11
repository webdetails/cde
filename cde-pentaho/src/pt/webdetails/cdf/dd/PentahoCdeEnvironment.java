package pt.webdetails.cdf.dd;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;

import pt.webdetails.cdf.dd.bean.factory.ICdeBeanFactory;
import pt.webdetails.cdf.dd.datasources.DataSourceManager;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cdf.dd.plugin.resource.PluginResourceLocationManager;
import pt.webdetails.cdf.dd.plugin.resource.ResourceLoader;
import pt.webdetails.cpf.IPluginCall;
import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.plugins.Plugin;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.resources.IResourceLoader;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpf.utils.PluginUtils;

public class PentahoCdeEnvironment implements ICdeEnvironment {
	
	protected static Log logger = LogFactory.getLog(PentahoCdeEnvironment.class);
	
	private IPluginCall interPluginCall;
	private IPluginUtils pluginUtils;
    private IRepositoryAccess repositoryAccess;
    private IResourceLoader resourceLoader;
    private IPluginResourceLocationManager pluginResourceLocationManager;
    private ICdeBeanFactory factory;
	
    @Override
	public void init() throws InitializationException {		
		resourceLoader = new ResourceLoader(PentahoSystem.get(IPluginResourceLoader.class, null));
		pluginResourceLocationManager = new PluginResourceLocationManager();
		pluginUtils = new PluginUtils();
	}
    
    @Override
    public void init(ICdeBeanFactory factory) throws InitializationException {
    	this.factory = factory;
    	
    	init();
    	
    	if(factory != null){
    		
    		if(factory.containsBean(IRepositoryAccess.class.getSimpleName())){    		
    			repositoryAccess = (IRepositoryAccess)factory.getBean(IRepositoryAccess.class.getSimpleName());
    			
    			if(repositoryAccess != null){
    				
    				PluginsAnalyzer pluginsAnalyzer = new PluginsAnalyzer();
    		        pluginsAnalyzer.refresh();
    		        String pluginName = pluginUtils.getPluginName();
    		        for (Plugin plgn : pluginsAnalyzer.getInstalledPlugins()) {
    		            if (plgn.getName().equalsIgnoreCase(pluginName) || plgn.getId().equalsIgnoreCase(pluginName)) {
    		                plgn.setName(pluginName);
    		                //IReadAccess readAccess = PentahoPluginEnvironment.getInstance().getOtherPluginSystemAccess(plgn.getId(), null);
    		                //readAccess.
    		                //repositoryAccess.setPlugin(plgn);
    		                break;
    		            }
    		        }
    			}
    		}
    		
    		if(factory.containsBean(IPluginCall.class.getSimpleName())){    		
    			interPluginCall = (IPluginCall)factory.getBean(IPluginCall.class.getSimpleName());
    		}
    	}
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
	public String getSolutionBaseDir() {
		return DashboardDesignerContentGenerator.SOLUTION_DIR;
	}

}
