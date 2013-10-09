package pt.webdetails.cdf.dd;

import org.apache.commons.lang.StringUtils;

import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.plugin.CorePlugin;
import pt.webdetails.cpf.plugincall.api.IPluginCall;
import pt.webdetails.cpf.plugincall.base.CallParameters;

/**
 * at least put cdf stuff here
 */
public class InterPluginBroker {

  public static String getCdfIncludes(String dashboard, String type, boolean debug, String absRoot, String scheme) throws Exception {
    CallParameters params = new CallParameters();
    params.put("dashboardContent", dashboard);
    params.put("debug", debug);
    if (type != null) {
      params.put("dashboardType", type);
    }

    if (!StringUtils.isEmpty(absRoot)) {
      params.put("root", absRoot);
    }
    //TODO: instantiate directly
    IPluginCall pluginCall = PluginEnvironment.env().getPluginCall( CorePlugin.CDF.getId(), null, "getHeaders" );
    
    return pluginCall.call( params.getParameters() );

   }
}
