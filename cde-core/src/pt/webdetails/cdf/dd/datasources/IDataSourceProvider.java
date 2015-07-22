package pt.webdetails.cdf.dd.datasources;

import org.json.JSONObject;

public interface IDataSourceProvider {
	
	public JSONObject getDataSourceDefinitions(boolean refresh);
	
	public String getId();
}
