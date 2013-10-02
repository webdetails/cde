/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.cdf;

import net.sf.json.JSONArray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import pt.webdetails.cdf.dd.DashboardDesignerException;
import pt.webdetails.cdf.dd.Messages;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.GenericBasicFileFilter;
import pt.webdetails.cdf.dd.util.JsonUtils;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;
import pt.webdetails.cpf.plugins.Plugin;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IReadAccess;

@SuppressWarnings("unchecked")

//TODO: move to core once pluginsAnalizer gets no pentaho-dependencies
public class CdfStyles 
{
  private static Log logger = LogFactory.getLog(CdfStyles.class);

  private static CdfStyles instance;
  
  private static final String SYSTEM_RESOURCE_STYLES_DIR = "resources/styles/";
  private static final String RESOURCE_STYLES_DIR_SOLUTION = "styles/";
  
  public static final String DEFAULTSTYLE = "Clean";

  public CdfStyles() 
  {
  }

  public void handleCall(OutputStream out, IParameterProvider requestParams) throws Exception {

    //Read parameters
    Iterator<String> keys = requestParams.getParameterNames();
    HashMap<String, String> parameters = new HashMap<String, String>();
    while (keys.hasNext()) {
      String key = keys.next();
      parameters.put(key, requestParams.getStringParameter(key, null));
    }

    String operation = requestParams.getStringParameter("operation", "").toLowerCase();

    //Call sync method
    try {

      Class<?>[] params = new Class[1];
      params[0] = HashMap.class;
      Method mthd = this.getClass().getMethod(operation, params);
      Object result = mthd.invoke(this, parameters);
      if (result != null) {
        JsonUtils.buildJsonResult(out, true, result);
      }

      JsonUtils.buildJsonResult(out, true, null);

    }
    catch (NoSuchMethodException e) {
      throw new Exception(Messages.getString("CdfTemplates.ERROR_001_INVALID_SYNCRONIZE_METHOD_EXCEPTION"));
    }

  }

  private class Style{
    String pluginId = null;
    IReadAccess access;
    String directory;
    List<IBasicFile> styleFiles = null;
    
    public Style(IReadAccess access, String directory, String pluginName){
      
      this.access = access;
      this.pluginId = pluginName;
      this.directory = directory;
      styleSelfBuild();
    }
    
    private void styleSelfBuild(){
      styleFiles = new ArrayList<IBasicFile>();
      final GenericBasicFileFilter htmlFilter = new GenericBasicFileFilter(null, ".html");
      
      List<IBasicFile> htmlList = access.listFiles(directory, htmlFilter, IReadAccess.DEPTH_ALL);
      
      if(htmlList != null){
    	  if(styleFiles == null){
    		  styleFiles = htmlList;
    	  }else{
    		  styleFiles.addAll(htmlList);
    	  }
      }
    }

    public String getPluginId() {
      return pluginId;
    }
    
    public String getSufixPluginName(){
      String sufix = getPluginId();
      
      if(sufix == null){
        sufix = "";
      }else{
        sufix = " - ("+getPluginId()+")";
      }
      
      return sufix;
    }

    public List<IBasicFile> getStyleFiles() {
      return styleFiles;
    }
  }

  public Object liststyles(HashMap<String,String> parameters) throws DashboardDesignerException {

    JSONArray result = new JSONArray();

    List<Style> styles = new ArrayList<Style>();
    Style style = null; 
    style = new Style(CdeEnvironment.getPluginSystemReader(), SYSTEM_RESOURCE_STYLES_DIR, null);
    styles.add(style);
    style = new Style(CdeEnvironment.getPluginRepositoryReader(), RESOURCE_STYLES_DIR_SOLUTION, null);
    styles.add(style);
    
    PluginsAnalyzer pluginsAnalyzer = new PluginsAnalyzer(CdeEnvironment.getContentAccessFactory(), PentahoSystem.get(IPluginManager.class));
    pluginsAnalyzer.refresh();
    
    List<PluginsAnalyzer.PluginWithEntity> entities = pluginsAnalyzer.getRegisteredEntities("/cde-styles");
    
    for(PluginsAnalyzer.PluginWithEntity entity : entities){
      String pluginStylesDir = entity.getRegisteredEntity().valueOf("path");
      String finalPath = pluginStylesDir+"/";
      String pluginId = entity.getPlugin().getId().toUpperCase();
      style = null;
      
      IReadAccess access = CdeEnvironment.getOtherPluginSystemReader(pluginId);
      
      if(access.fileExists(finalPath) && access.fetchFile(finalPath).isDirectory()){
    	  style = new Style(access, finalPath, pluginId);
          styles.add(style);
      }
    }

    if (styles == null || styles.size() < 1) {
      logger.error("No styles directory found in resources");
      styles = new ArrayList<Style>();
    }

    for (Style s : styles) {
      List<IBasicFile> styleFiles = s.getStyleFiles();
      
      for(IBasicFile file : styleFiles){
        String name = file.getName();
        result.add(name.substring(0,name.lastIndexOf('.')) + s.getSufixPluginName());
      }
    }

    return result;
  }


  public static synchronized CdfStyles getInstance() {

    if (instance == null) {
      instance = new CdfStyles();
    }

    return instance;

  }

  public String getResourceLocation(String style) {
    String stylePath = null;
    
    String styleFilename;
    String[] split = style.split(" - ");
    if(split.length > 1) {
      String pluginId = split[1].replace("(", "").replace(")", "");
      
      styleFilename = split[0] + ".html";
      
      PluginsAnalyzer pluginsAnalizer = new PluginsAnalyzer();
      pluginsAnalizer.refresh();
      
      List<Plugin> plugins = pluginsAnalizer.getInstalledPlugins();
      
      for(Plugin plugin : plugins) {
        if(plugin.getId().equalsIgnoreCase(pluginId)) {
          stylePath = "/" + plugin.getRegisteredEntities("/cde-styles").valueOf("path")+ "/" + styleFilename;
          break;
        }
      }
    
    } else {
      styleFilename = style + ".html";
      
      String customStylePath = RESOURCE_STYLES_DIR_SOLUTION + styleFilename;
      
      if(CdeEnvironment.getPluginRepositoryReader().fileExists(customStylePath)) {
        stylePath =  customStylePath;
      
      } else if(CdeEnvironment.getPluginSystemReader(SYSTEM_RESOURCE_STYLES_DIR).fileExists(styleFilename)) {
        stylePath = SYSTEM_RESOURCE_STYLES_DIR + styleFilename;
      }
    }
    
    return stylePath;
  }
}