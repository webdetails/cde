/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import pt.webdetails.cdf.dd.structure.DashboardStructureException;
import pt.webdetails.cdf.dd.structure.DashboardStructure;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cpf.http.ICommonParameterProvider;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

public class SyncronizeCdfStructure
{

  private static SyncronizeCdfStructure syncronizeCdfStructure = null;
  public static String PLUGIN_EMPTY_STRUCTURE_FILE_PATH = "resources/empty-structure.json";
  public static String PLUGIN_EMPTY_WCDF_FILE_PATH = "resources/empty.wcdf";

  static SyncronizeCdfStructure getInstance()
  {
    if (syncronizeCdfStructure == null)
    {
      syncronizeCdfStructure = new SyncronizeCdfStructure();
    }
    return syncronizeCdfStructure;
  }

  public void syncronize(final OutputStream out, final ICommonParameterProvider requestParams) throws Exception
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

    // file path must exist
    if (parameters == null || parameters.get("file") == null){
        throw new Exception(Messages.getString("SyncronizeCdfStructure.ERROR_002_INVALID_FILE_PARAMETER_EXCEPTION"));
    }

    // Call sync method
    try
    {
      final DashboardStructure dashboardStucture = new DashboardStructure();
      
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
        if (e.getCause() instanceof DashboardStructureException)
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
}
