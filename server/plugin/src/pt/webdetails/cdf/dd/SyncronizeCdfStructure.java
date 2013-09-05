/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import pt.webdetails.cdf.dd.structure.StructureException;
import pt.webdetails.cdf.dd.structure.XmlStructure;
import pt.webdetails.cdf.dd.util.JsonUtils;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

@SuppressWarnings("unchecked")
public class SyncronizeCdfStructure
{

  private static SyncronizeCdfStructure syncronizeCdfStructure = null;
  public static String EMPTY_STRUCTURE_FILE_PATH = PentahoSystem.getApplicationContext().getSolutionPath("system/" + DashboardDesignerContentGenerator.PLUGIN_NAME + "/resources/empty-structure.json");
  public static String EMPTY_WCDF_FILE_PATH = PentahoSystem.getApplicationContext().getSolutionPath("system/" + DashboardDesignerContentGenerator.PLUGIN_NAME + "/resources/empty.wcdf");

  static SyncronizeCdfStructure getInstance()
  {
    if (syncronizeCdfStructure == null)
    {
      syncronizeCdfStructure = new SyncronizeCdfStructure();
    }
    return syncronizeCdfStructure;
  }

  public void syncronize(final IPentahoSession userSession, final OutputStream out, final IParameterProvider requestParams) throws Exception
  {
    // Read parameters
    final Iterator<String> keys = requestParams.getParameterNames();
    final HashMap<String, Object> parameters = new HashMap<String, Object>();
    while (keys.hasNext())
    {
      final String key = keys.next();
      String[] param = requestParams.getStringArrayParameter(key, null);
      if(param == null) 
      {
        continue;
      }
      
      if(param.length > 1) {
        parameters.put(key, param);
      } else {
        parameters.put(key, param[0]);
      }
    }

    final String operation = requestParams.getStringParameter("operation", "").toLowerCase();

    // Set file path
    setFilePath(userSession, parameters);

    // Call sync method
    try
    {
      final XmlStructure dashboardStucture = new XmlStructure(userSession);
      
      final Class<?>[] params = new Class[1];
      params[0] = HashMap.class;
      final Method method = dashboardStucture.getClass().getMethod(operation, params);
      final Object result = method.invoke(dashboardStucture, parameters);
      if (result != null)
      {
        JsonUtils.buildJsonResult(out, true, result);
      }
      else
      {
        JsonUtils.buildJsonResult(out, true, null);
      }
    }
    catch(NoSuchMethodException e)
    {
      throw new Exception(Messages.getString("SyncronizeCdfStructure.ERROR_001_INVALID_SYNCRONIZE_METHOD_EXCEPTION"));
    }
    catch(Exception e)
    {
      if(e.getCause() != null)
      {
        if (e.getCause() instanceof StructureException)
        {
          JsonUtils.buildJsonResult(out, false, e.getCause().getMessage());
        }
        else if(e instanceof InvocationTargetException)
        {
          throw (Exception)e.getCause();
        }
      }
      
      throw e;
    }
  }

  private void setFilePath(final IPentahoSession userSession, final HashMap<String, Object> parameters) throws Exception
  {

	  if (parameters.get("file") == null){
		  throw new Exception(Messages.getString("SyncronizeCdfStructure.ERROR_002_INVALID_FILE_PARAMETER_EXCEPTION"));
	  }
  }
}
