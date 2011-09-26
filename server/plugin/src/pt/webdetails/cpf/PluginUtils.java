/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cpf;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 *
 * @author pdpi
 */
public class PluginUtils
{

  private static final Log logger = LogFactory.getLog(PluginUtils.class);

  public static String callPlugin(String pluginName, String method, Map<String, Object> params)
  {
    IParameterProvider requestParams = new SimpleParameterProvider(params);
    return callPlugin(pluginName, method, requestParams);

  }

  public static String callPlugin(String pluginName, String method, IParameterProvider params)
  {

    IPentahoSession userSession = PentahoSessionHolder.getSession();
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, userSession);
    IContentGenerator contentGenerator;
    try
    {
      contentGenerator = pluginManager.getContentGenerator(pluginName, userSession);
    }
    catch (Exception e)
    {
      logger.error("Failed to acquire " + pluginName + " plugin: " + e.toString());
      return null;
    }
    return callPlugin(userSession, contentGenerator, method, params);
  }


  
  public static String callPlugin(IPentahoSession userSession, IContentGenerator contentGenerator, String method, Map<String, Object> params)
  {
    IParameterProvider requestParams = new SimpleParameterProvider(params);
    return callPlugin(userSession, contentGenerator, method, requestParams);
  }
  public static String callPlugin(IPentahoSession userSession, IContentGenerator contentGenerator, String method, IParameterProvider params)
  {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    IParameterProvider requestParams = params;
    Map<String, Object> pathMap = new HashMap<String, Object>();
    pathMap.put("path", "/" + method);
    IParameterProvider pathParams = new SimpleParameterProvider(pathMap);
    Map<String, IParameterProvider> paramProvider = new HashMap<String, IParameterProvider>();
    paramProvider.put(IParameterProvider.SCOPE_REQUEST, requestParams);
    paramProvider.put("path", pathParams);


    return callPlugin(userSession, contentGenerator, outputStream, paramProvider);
  }
  public static String callPlugin(IPentahoSession userSession, IContentGenerator cda, OutputStream outputStream, Map<String, IParameterProvider> paramProvider)
  {
    IOutputHandler outputHandler = new SimpleOutputHandler(outputStream, false);
    try
    {
      cda.setSession(userSession);
      cda.setOutputHandler(outputHandler);
      cda.setParameterProviders(paramProvider);
      cda.createContent();
      return outputStream.toString();
    }
    catch (Exception e)
    {
      logger.error("Failed to execute call to plugin: " + e.toString());
      return null;
    }
  }
}
