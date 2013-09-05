package pt.webdetails.cdf.dd;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import pt.webdetails.cpf.IPluginCall;

public abstract class AbstractInterPluginCallManager {
	
	public abstract IPluginCall getInterPluginCall();	
	
	public String getCdfIncludes(String dashboard, String type, boolean debug, String absRoot, String scheme) throws IOException {
	    Map<String, Object> params = new HashMap<String, Object>();
	    params.put("dashboardContent", dashboard);
	    params.put("debug", debug);
	    params.put("scheme", scheme);
	    if (type != null) {
	      params.put("dashboardType", type);
	    }
	    if (!StringUtils.isEmpty(absRoot)) {
	      params.put("root", absRoot);
	    }

	    return getInterPluginCall().call();
	 }
	
	

}
