/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.cdf;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator;
import pt.webdetails.cdf.dd.Messages;
import pt.webdetails.cdf.dd.structure.DashboardStructureException;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.GenericBasicFileFilter;
import pt.webdetails.cdf.dd.util.JsonUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cpf.utils.CharsetHelper;

@SuppressWarnings("unchecked")
public class CdfTemplates {

  private static String SYSTEM_CDF_DD_TEMPLATES = /*"system/" + DashboardDesignerContentGenerator.PLUGIN_NAME +*/ "/resources/templates";
  private static String REPOSITORY_CDF_DD_TEMPLATES_CUSTOM = DashboardDesignerContentGenerator.SOLUTION_DIR + "/templates";
  
  public static String SYSTEM_RESOURCE_TEMPLATE_DIR = Utils.getBaseUrl() + "content/pentaho-cdf-dd/getResource?resource=/resources/templates/";
  public static String UNKNOWN_IMAGE = SYSTEM_RESOURCE_TEMPLATE_DIR + "unknown.png";

  private static Log logger = LogFactory.getLog(CdfTemplates.class);

  public CdfTemplates(){}

  /**
   * Invokes operation through reflection
   * @param out
   * @param requestParams
   * @throws Exception
   */
  public void handleCall(final OutputStream out, final IParameterProvider requestParams) throws Exception {

    //Read parameters
    final Iterator<String> keys = requestParams.getParameterNames();
    final HashMap<String, String> parameters = new HashMap<String, String>();
    while (keys.hasNext()) {
      final String key = keys.next();
      parameters.put(key, requestParams.getStringParameter(key, null));
    }

    final String operation = requestParams.getStringParameter("operation", "").toLowerCase();

    //Call sync method
    try {

      final Class<?>[] params = new Class[1];
      params[0] = HashMap.class;
      final Method mthd = this.getClass().getMethod(operation, params);
      final Object result = mthd.invoke(this, parameters);
      if (result != null) {
        JsonUtils.buildJsonResult(out, true, result);
      }

      JsonUtils.buildJsonResult(out, true, null);

    }
    catch (NoSuchMethodException e) {
      throw new Exception(Messages.getString("CdfTemplates.ERROR_001_INVALID_SYNCRONIZE_METHOD_EXCEPTION"));
    }

  }

  public void save(final HashMap<String,String> parameters) throws DashboardStructureException, PentahoAccessControlException, IOException {

    final String fileName = (String) parameters.get("file");
    logger.info("Saving File:" + fileName);
    
    IUserContentAccess access = CdeEnvironment.getUserContentAccess();
    
    if(!access.fileExists(REPOSITORY_CDF_DD_TEMPLATES_CUSTOM)) {
    	access.createFolder(REPOSITORY_CDF_DD_TEMPLATES_CUSTOM);
    }
    
    String cdfStructure = (String) parameters.get(CdeConstants.MethodParams.CDF_STRUCTURE);
    byte[] fileData = cdfStructure.getBytes(CharsetHelper.getEncoding());
    
    if(!access.saveFile(Utils.joinPath(REPOSITORY_CDF_DD_TEMPLATES_CUSTOM,fileName), new ByteArrayInputStream(fileData))){
    	throw new DashboardStructureException(Messages.getString("DashboardStructure.ERROR_006_SAVE_FILE_ADD_FAIL_EXCEPTION"));
    }
  }

  public Object load(final HashMap<String,String> parameters) {

    Object result = new JSONArray();

    try {

    	GenericBasicFileFilter jsonFilter = new GenericBasicFileFilter(null, ".cdfde");
    	
    	List<IBasicFile> defaultTemplatesList = CdeEnvironment.getPluginSystemReader(SYSTEM_CDF_DD_TEMPLATES).listFiles(null, jsonFilter, IReadAccess.DEPTH_ALL);
    	
      if (defaultTemplatesList != null) {
        loadFiles(defaultTemplatesList.toArray(new IBasicFile[]{}), (JSONArray) result, "default");
      } else {
        result = Messages.getString("CdfTemplates.ERROR_002_LOADING_TEMPLATES_EXCEPTION");
      }
      
      List<IBasicFile> customTemplatesList = CdeEnvironment.getPluginRepositoryReader(REPOSITORY_CDF_DD_TEMPLATES_CUSTOM).listFiles(null, jsonFilter, IReadAccess.DEPTH_ALL);
      
      if (customTemplatesList != null) {
          loadFiles(customTemplatesList.toArray(new IBasicFile[]{}), (JSONArray) result, "custom");
      }
      
    } catch (Exception e) {
      result = Messages.getString("CdfTemplates.ERROR_002_LOADING_EXCEPTION");
    }

    return result;

  }

  private void loadFiles(final IBasicFile[] jsonFiles, final JSONArray result, final String type) throws Exception {

	  Arrays.sort(jsonFiles, 
		  new Comparator<IBasicFile>(){
	
			@Override
			public int compare(IBasicFile file1, IBasicFile file2) {
				if (file1 == null && file2 == null){
					return 0;
				}else{
					return file1.getFullPath().toLowerCase().compareTo(file2.getFullPath().toLowerCase());
				}
			}	
	  });

	IReadAccess access = CdeEnvironment.getPluginSystemReader(SYSTEM_RESOURCE_TEMPLATE_DIR);
	  
    for (int i = 0; i < jsonFiles.length; i++) {
      final JSONObject template = new JSONObject();
      
      String imgResourcePath = UNKNOWN_IMAGE;
      
      if(access.fileExists(jsonFiles[i].getName().replace(".cdfde", ".png"))){
    	  imgResourcePath = jsonFiles[i].getFullPath().replace(".cdfde", ".png");
      }
      
      template.put("img", imgResourcePath);
      template.put("type", type);
      
      template.put("structure", JsonUtils.readJsonFromInputStream(jsonFiles[i].getContents()));
      result.add(template);
    }
  }
}
