package pt.webdetails.cdf.dd;

import java.util.Locale;

import pt.webdetails.cdf.dd.bean.factory.ICdeBeanFactory;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cpf.IPluginCall;
import pt.webdetails.cpf.resources.IResourceLoader;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;


public interface ICdeEnvironment {
	
	public void init() throws InitializationException;
	
	public void init(ICdeBeanFactory factory) throws InitializationException;
	
	public void refresh();
		
	public String getApplicationBaseUrl();
	
	public Locale getLocale();
	
	public IPluginCall getInterPluginCall();
	
	public IResourceLoader getResourceLoader();
	
	public IDataSourceManager getDataSourceManager();
	
	public IPluginResourceLocationManager getPluginResourceLocationManager();
	
	public IContentAccessFactory getContentAccessFactory();
}