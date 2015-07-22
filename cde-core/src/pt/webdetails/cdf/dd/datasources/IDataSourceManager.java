package pt.webdetails.cdf.dd.datasources;

import java.util.List;

import org.json.JSONObject;

public interface IDataSourceManager {
		
	public List<IDataSourceProvider> getProviders();
	
	public IDataSourceProvider getProvider(String id);
	
	public JSONObject getProviderJsDefinition(String providerId);
	
	public JSONObject getProviderJsDefinition(String providerId, boolean bypassCacheRead);
	
	public void refresh();

}
