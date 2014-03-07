package pt.webdetails.cdf.dd.datasources;


import java.util.List;

import net.sf.json.JSON;

public interface IDataSourceManager {
		
	public List<IDataSourceProvider> getProviders();
	
	public IDataSourceProvider getProvider(String id);
	
	public JSON getProviderJsDefinition(String providerId);
	
	public JSON getProviderJsDefinition(String providerId, boolean bypassCacheRead);
	
	public void refresh();

}
