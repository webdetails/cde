package pt.webdetails.cdf.dd.datasources;

import net.sf.json.JSON;

public interface IDataSourceProvider {
	
	public JSON getDataSourceDefinitions(boolean refresh);
	
	public String getId();
}
