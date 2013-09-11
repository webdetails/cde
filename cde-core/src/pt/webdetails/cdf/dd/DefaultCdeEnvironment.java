package pt.webdetails.cdf.dd;

import java.util.Locale;

import pt.webdetails.cdf.dd.bean.factory.ICdeBeanFactory;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cpf.IPluginCall;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.resources.IResourceLoader;

public class DefaultCdeEnvironment implements ICdeEnvironment {

	@Override
	public void init() throws InitializationException {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void init(ICdeBeanFactory factory) throws InitializationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getApplicationBaseUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSolutionBaseDir() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPluginCall getInterPluginCall() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResourceLoader getResourceLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDataSourceManager getDataSourceManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPluginResourceLocationManager getPluginResourceLocationManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContentAccessFactory getContentAccessFactory() {
		// TODO Auto-generated method stub
		return null;
	}
}
