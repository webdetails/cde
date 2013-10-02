package pt.webdetails.cdf.dd;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.plugins.IPluginFilter;
import pt.webdetails.cpf.plugins.Plugin;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;

import java.util.List;

/**
 * User: diogomariano
 * Date: 05/09/13
 */
public class CdePlugins {
  private static Log logger = LogFactory.getLog(CdePlugins.class);

  public String getCdePlugins(){
    
	  JSONArray pluginsArray = new JSONArray();
	
	PluginsAnalyzer pluginsAnalyzer = new PluginsAnalyzer(CdeEnvironment.getContentAccessFactory(), PentahoSystem.get(IPluginManager.class));
    pluginsAnalyzer.refresh();

    IPluginFilter pluginFilter = new IPluginFilter() {
      public boolean include(Plugin plugin) {
        boolean include = false;
        if(plugin.hasSettingsXML()) {
          include = (plugin.getXmlValue("/settings/cde-compatible", "settings.xml").equals("true")) ? true : false;
        }
        return include;
      }
    };

    List<Plugin> cdePlugins = pluginsAnalyzer.getPlugins(pluginFilter);

    for(Plugin plugin : cdePlugins) {
      try {
        JSONObject pluginObject = new JSONObject();
		String [] split = plugin.getPluginRelativePath().split("/");
        pluginObject.put("title", split[split.length-1]);
        pluginObject.put("description", plugin.getXmlValue("/settings/description", "settings.xml"));
        pluginObject.put("url", plugin.getXmlValue("/settings/url", "settings.xml"));
        pluginObject.put("jsPath", plugin.getXmlValue("/settings/jsPath", "settings.xml"));
        pluginObject.put("pluginId", plugin.getId());

        pluginsArray.add(pluginObject);
      } catch(Exception e) {
        logger.error(e);
      }
    }

    logger.info("Feeding client with CDE-Compatible plugin list");

    return pluginsArray.toString();
  }

}
