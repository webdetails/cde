package pt.webdetails.cdf.dd;

import java.util.Locale;

import pt.webdetails.cdf.dd.bean.factory.ICdeBeanFactory;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cpf.IPluginCall;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.resources.IResourceLoader;
import pt.webdetails.cpf.utils.IPluginUtils;


public interface ICdeEnvironment {
	
	public void init() throws InitializationException;
	
	public void init(ICdeBeanFactory factory) throws InitializationException;
	
	public void refresh();
		
	public String getApplicationBaseUrl();
	
	/**
	 * solution folder for custom components, styles and templates
	 */
	public String getSolutionBaseDir();
	
	public Locale getLocale();
	
	public IPluginUtils getPluginUtils();
	
	public IPluginCall getInterPluginCall();
	
	public IResourceLoader getResourceLoader();
	
	public IRepositoryAccess getRepositoryAccess();
	
	public IDataSourceManager getDataSourceManager();
	
	public IPluginResourceLocationManager getPluginResourceLocationManager();
}