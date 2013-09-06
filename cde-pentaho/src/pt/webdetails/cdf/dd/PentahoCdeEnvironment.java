package pt.webdetails.cdf.dd;

import java.util.Locale;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;

import pt.webdetails.cdf.dd.datasources.DataSourceManager;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cpf.IPluginCall;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.PentahoInterPluginCall;
import pt.webdetails.cpf.plugin.CorePlugin;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;
import pt.webdetails.cpf.resources.IResourceLoader;
import pt.webdetails.cpf.utils.IPluginUtils;

public class PentahoCdeEnvironment implements ICdeEnvironment {
	
	private IPluginCall interPluginCall;
	
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
		
		if(interPluginCall == null){ 
			interPluginCall = new PentahoInterPluginCall();
		}
		
		return interPluginCall;
	}

	@Override
	public Locale getLocale() {
		return LocaleHelper.getLocale();
	}

	@Override
	public IPluginResourceLocationManager getPluginResourceLocationManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPluginUtils getPluginUtils() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRepositoryAccess getRepositoryAccess() {
		return (PentahoRepositoryAccess) PentahoRepositoryAccess.getRepository(PentahoSessionHolder.getSession());
	}

	@Override
	public IResourceLoader getResourceLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() throws InitializationException {
	}

}
