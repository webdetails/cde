/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import pt.webdetails.cdf.dd.structure.DashboardStructureException;
import pt.webdetails.cdf.dd.structure.DashboardStructure;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpf.repository.api.FileAccess;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SyncronizeCdfStructure {

	private static final Log logger = LogFactory.getLog(SyncronizeCdfStructure.class);
	
  private static SyncronizeCdfStructure syncronizeCdfStructure = null;
  public static String SYSTEM_PLUGIN_EMPTY_STRUCTURE_FILE_PATH = "resources/empty-structure.json";
  public static String SYSTEM_PLUGIN_EMPTY_WCDF_FILE_PATH = "resources/empty.wcdf";
  
  private static final String PARAM_PATH = "path";
  private static final String PARAM_FILE = "file";
  private static final String PARAM_OPERATION = "operation";

  static SyncronizeCdfStructure getInstance()
  {
    if (syncronizeCdfStructure == null)
    {
      syncronizeCdfStructure = new SyncronizeCdfStructure();
    }
    return syncronizeCdfStructure;
  }

  public void syncronize(final ICommonParameterProvider paramProvider, HttpServletResponse response) throws Exception {
    // Call sync method
    try {
    	
      final String path = ((String) paramProvider.getParameter(PARAM_FILE)).replaceAll("cdfde", "wcdf");
      
      // Check security. Caveat: if no path is supplied, then we're in the new parameter
      if (paramProvider.hasParameter(PARAM_PATH) && !CdeEnvironment.getUserContentAccess().hasAccess(path, FileAccess.EXECUTE)) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			logger.warn("Access denied for the syncronize method: " + path);
			return;
	  }
    	
      final String operation = paramProvider.getStringParameter(PARAM_OPERATION, "").toLowerCase();
      
      // file path must exist
	  if (paramProvider == null || paramProvider.getParameter(PARAM_FILE) == null){
	      throw new Exception(Messages.getString("SyncronizeCdfStructure.ERROR_002_INVALID_FILE_PARAMETER_EXCEPTION"));
	  }
    	
      final DashboardStructure dashboardStucture = new DashboardStructure();
      
      final Class<?>[] params = new Class[1];
      params[0] = HashMap.class;
      final Method method = dashboardStucture.getClass().getMethod(operation, params);
      final Object result = method.invoke(dashboardStucture, paramProvider.getParameters());
      
      JsonUtils.buildJsonResult(response.getOutputStream() , true, result);
    
    } catch(NoSuchMethodException e) {
      throw new Exception(Messages.getString("SyncronizeCdfStructure.ERROR_001_INVALID_SYNCRONIZE_METHOD_EXCEPTION"));
    } catch(Exception e) {
      if(e.getCause() != null) {
        if (e.getCause() instanceof DashboardStructureException) {
          JsonUtils.buildJsonResult(response.getOutputStream(), false, e.getCause().getMessage());
        } else if(e instanceof InvocationTargetException) {
          throw (Exception)e.getCause();
        }
      }
      throw e;
    }
  }
}
