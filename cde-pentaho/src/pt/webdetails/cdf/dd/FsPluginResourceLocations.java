/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;
import pt.webdetails.cpf.repository.api.IReadAccess;

/**
 * @author dcleao
 */
public final class FsPluginResourceLocations
{
  private static final IReadAccess[] _resourceAbsDirs;
  private static final IReadAccess[] _customCompsSolRelDirs;
  
  protected static final Log _logger;

  static
  {
    _logger = LogFactory.getLog(FsPluginResourceLocations.class);
    
    ArrayList<IReadAccess> resourceAbsDirs = new ArrayList<IReadAccess>();
    ArrayList<IReadAccess> customCompsSolRelDirs = new ArrayList<IReadAccess>();
    
    initLocations(resourceAbsDirs, customCompsSolRelDirs);
    
    _resourceAbsDirs = resourceAbsDirs.toArray(new IReadAccess[resourceAbsDirs.size()]);
    _customCompsSolRelDirs = customCompsSolRelDirs.toArray(new IReadAccess[customCompsSolRelDirs.size()]);
  }
  
  // Static class
  private FsPluginResourceLocations()
  {
  }
  
  public static IReadAccess[] getResourcesAbsDirs()
  {
    return _resourceAbsDirs;
  }
  
  public static IReadAccess[] getCustomComponentsRelDirs()
  {
    return _customCompsSolRelDirs;
  }

  // ----------
  
  private static void initLocations(ArrayList<IReadAccess> resourceAbsDirs, ArrayList<IReadAccess> customCompsSolRelDirs) {
    // Add base locations
    resourceAbsDirs.add(CdeEnvironment.getPluginSystemReader());
    resourceAbsDirs.add(CdeEnvironment.getUserContentAccess());

    for (IReadAccess componentsDir : CdeSettings.getComponentLocations()){
    	customCompsSolRelDirs.add(componentsDir);
    	resourceAbsDirs.add(componentsDir);
    }

    // External component locations
    PluginsAnalyzer pluginsAnalyzer = new PluginsAnalyzer();
    pluginsAnalyzer.refresh();

    for(PluginsAnalyzer.PluginWithEntity entity : pluginsAnalyzer.getRegisteredEntities("/cde-components")) {
      String path = entity.getRegisteredEntity().valueOf("path");
      
      if(path != null){
          
    	  path = path.startsWith("/") ? path.replaceFirst("/", "").toLowerCase().trim() : path.toLowerCase().trim();
      
    	  if(path.startsWith(DashboardDesignerContentGenerator.PLUGIN_PATH)){
    		  
    		  path = path.replaceFirst(DashboardDesignerContentGenerator.PLUGIN_PATH, "");
    		
    		  customCompsSolRelDirs.add(CdeEnvironment.getPluginSystemReader(path));
    	      resourceAbsDirs.add(CdeEnvironment.getPluginSystemReader(path));
    	  
    	  }else if(path.startsWith(DashboardDesignerContentGenerator.SYSTEM_PATH)){
    		  
    		  path = path.replaceFirst(DashboardDesignerContentGenerator.SYSTEM_PATH + "/", ""); 
    		  path = path.replaceFirst(entity.getPlugin().getId().toLowerCase() + "/", "");
    		  
    		  customCompsSolRelDirs.add(CdeEnvironment.getOtherPluginSystemReader(entity.getPlugin().getId(), path));
    	      resourceAbsDirs.add(CdeEnvironment.getOtherPluginSystemReader(entity.getPlugin().getId(), path));
    		
    	  }else if(path.startsWith(DashboardDesignerContentGenerator.SOLUTION_DIR)) {
    		  
    		  path = path.replaceFirst(DashboardDesignerContentGenerator.SOLUTION_DIR + "/", ""); 
    		  
    		  customCompsSolRelDirs.add(CdeEnvironment.getPluginRepositoryReader(path));
    	      resourceAbsDirs.add(CdeEnvironment.getPluginRepositoryReader(path));  
    	  
    	  }else{
    		
    		  path = path.replaceFirst(entity.getPlugin().getId().toLowerCase() + "/", "");
    		  customCompsSolRelDirs.add(CdeEnvironment.getOtherPluginSystemReader(entity.getPlugin().getId(), path));
    	      resourceAbsDirs.add(CdeEnvironment.getOtherPluginSystemReader(entity.getPlugin().getId(), path));
    	  
    	  }
      
    	  _logger.debug(String.format("Found CDE components location declared in %s [%s]", entity.getPlugin().getId(), entity.getRegisteredEntity().valueOf("path")));
    }
  }
  }
}
