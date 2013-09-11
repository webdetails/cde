/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.cdf;

import net.sf.json.JSONArray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;

import pt.webdetails.cdf.dd.DashboardDesignerException;
import pt.webdetails.cdf.dd.Messages;
import pt.webdetails.cdf.dd.util.JsonUtils;

import java.io.FilenameFilter;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;
import pt.webdetails.cpf.plugins.Plugin;

@SuppressWarnings("unchecked")

//TODO: move to core once pluginsAnalizer gets no pentaho-dependencies
public class CdfStyles 
{
  private static Log logger = LogFactory.getLog(CdfStyles.class);

  private static CdfStyles instance;
  
  private static final String RESOURCE_STYLES_DIR = "resources/styles/";
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
    File directory = null;
    List<File> styleFiles = null;
    
    public Style(File directory, String pluginName){
      
      this.directory = directory;
      this.pluginId = pluginName;
      styleSelfBuild();
    }
    
    private void styleSelfBuild(){
      styleFiles = new ArrayList<File>();
      final FilenameFilter htmlFilter = new FilenameFilter() {
        public boolean accept(final File dir, final String name) {
          return name.endsWith(".html");
        }
      };
      
      if(getDirectory().isDirectory()){
        String files[] = directory.list(htmlFilter);
        
        for(String file : files){
          File f = new File(directory.getAbsolutePath() + "/" + file);
          styleFiles.add(f);
        }
        
      }
      
    }

    public File getDirectory()
    {
      return directory;
    }

    public String getPluginId()
    {
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

    public List<File> getStyleFiles()
    {
      return styleFiles;
    }
    
  }

  public Object liststyles(HashMap<String,String> parameters) throws DashboardDesignerException {

    JSONArray result = new JSONArray();

    List<Style> styles = new ArrayList<Style>();
    Style style = null; 
    style = new Style(new File(ResourceManager.PLUGIN_DIR + RESOURCE_STYLES_DIR), null);
    styles.add(style);
    style = new Style(new File(ResourceManager.SOLUTION_DIR + RESOURCE_STYLES_DIR_SOLUTION), null);
    styles.add(style);
    
    PluginsAnalyzer pluginsAnalyzer = new PluginsAnalyzer();
    pluginsAnalyzer.refresh();
    
    
    List<PluginsAnalyzer.PluginWithEntity> entities = pluginsAnalyzer.getRegisteredEntities("/cde-styles");
    
    for(PluginsAnalyzer.PluginWithEntity entity : entities){
      String solutionPath = PentahoSystem.getApplicationContext().getSolutionPath("");
      String pluginStylesDir = entity.getRegisteredEntity().valueOf("path");
      String finalPath = solutionPath+pluginStylesDir+"/";
      String pluginId = entity.getPlugin().getId().toUpperCase();
      style = null;
      
      File folder = new File(finalPath);
      
      if(folder.isDirectory()){
        style = new Style(folder, pluginId);
        styles.add(style);
        
      }
    }
    
    

    

    if (styles == null || styles.size() < 1) {
      logger.error("No styles directory found in resources");
      styles = new ArrayList<Style>();
    }
    

    for (Style s : styles) {
      List<File> styleFiles = s.getStyleFiles();
      
      for(File file : styleFiles){
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

  public String getResourceLocation(String style)
  {
    String stylePath = null;
    
    String styleFilename;
    String[] split = style.split(" - ");
    if(split.length > 1)
    {
      String pluginId = split[1].replace("(", "").replace(")", "");
      
      styleFilename = split[0] + ".html";
      
      PluginsAnalyzer pluginsAnalizer = new PluginsAnalyzer();
      pluginsAnalizer.refresh();
      
      List<Plugin> plugins = pluginsAnalizer.getInstalledPlugins();
      
      for(Plugin plugin : plugins)
      {
        if(plugin.getId().equalsIgnoreCase(pluginId))
        {
          stylePath = "/" + plugin.getRegisteredEntities("/cde-styles").valueOf("path")+ "/" + styleFilename;
          break;
        }
      }
    }
    else
    {
      styleFilename = style + ".html";
      
      String customStylePath = RESOURCE_STYLES_DIR_SOLUTION + styleFilename;
      File styleFile = new File(ResourceManager.SOLUTION_DIR + customStylePath);
      if(styleFile.exists())
      {
        stylePath =  customStylePath;
      }
      else if(new File(ResourceManager.PLUGIN_DIR + RESOURCE_STYLES_DIR+"/"+styleFilename).exists())
      {
        stylePath = RESOURCE_STYLES_DIR + styleFilename;
      }
    }
    
    return stylePath;
  }
}