package pt.webdetails.cdf.dd;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import pt.webdetails.cpf.PentahoLegacyInterPluginCall;
import pt.webdetails.cpf.plugin.CorePlugin;

/**
 * at least put cdf stuff here
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
}
