package pt.webdetails.cdf.dd;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.PentahoLegacyInterPluginCall;
import pt.webdetails.cpf.plugin.CorePlugin;

/**
 * temporary arrangement for inter plugin calls
 */
public class InterPluginBroker {

  public static String getCdfIncludes(String dashboard, String type, boolean debug, String absRoot, String scheme) throws Exception {
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

    PentahoLegacyInterPluginCall pluginCall = new PentahoLegacyInterPluginCall( );
    pluginCall.init(CorePlugin.CDF, "GetHeaders", params);

    return pluginCall.call();
  }

  public static String getDataSourceDefinitions(String pluginId, String service, String method, boolean forceRefresh) throws Exception {
    PentahoCdeEnvironment.env().getPluginCall( pluginId, service, method );
    InterPluginCall listDATypesCall = new InterPluginCall(new InterPluginCall.Plugin(pluginId), method);
    // listDATypesCall.setSession(PentahoSessionHolder.getSession()); //TODO: why?
    listDATypesCall.putParameter( "refreshCache", Boolean.toString( forceRefresh ) );
    return listDATypesCall.call();
  }
}
