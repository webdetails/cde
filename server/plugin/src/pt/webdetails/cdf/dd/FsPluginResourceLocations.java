
package pt.webdetails.cdf.dd;

import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;

/**
 * @author dcleao
 */
public final class FsPluginResourceLocations
{
  private static final String[] _resourceAbsDirs;
  private static final String[] _customCompsSolRelDirs;
  
  protected static final Log _logger;

  static
  {
    _logger = LogFactory.getLog(FsPluginResourceLocations.class);
    
    ArrayList<String> resourceAbsDirs = new ArrayList<String>();
    ArrayList<String> customCompsSolRelDirs = new ArrayList<String>();
    
    initLocations(resourceAbsDirs, customCompsSolRelDirs);
    
    _resourceAbsDirs = resourceAbsDirs.toArray(new String[resourceAbsDirs.size()]);
    _customCompsSolRelDirs = customCompsSolRelDirs.toArray(new String[customCompsSolRelDirs.size()]);
  }
  
  // Static class
  private FsPluginResourceLocations()
  {
  }
  
  public static String[] getResourcesAbsDirs()
  {
    return _resourceAbsDirs;
  }
  
  public static String[] getCustomComponentsRelDirs()
  {
    return _customCompsSolRelDirs;
  }

  // ----------
  
  private static void initLocations(ArrayList<String> resourceAbsDirs, ArrayList<String> customCompsSolRelDirs)
  {
    // Add base locations
    resourceAbsDirs.add(PentahoRepositoryAccess.getPentahoSolutionPath(DashboardDesignerContentGenerator.PLUGIN_PATH));
    resourceAbsDirs.add(PentahoRepositoryAccess.getPentahoSolutionPath(""));

    for (String componentsDir : CdeSettings.getComponentLocations())
    {
      customCompsSolRelDirs.add(componentsDir);
      resourceAbsDirs.add(PentahoRepositoryAccess.getPentahoSolutionPath(componentsDir));
    }

    // External component locations
    PluginsAnalyzer pluginsAnalyzer = new PluginsAnalyzer();
    pluginsAnalyzer.refresh();

    for(PluginsAnalyzer.PluginWithEntity entity : pluginsAnalyzer.getRegisteredEntities("/cde-components"))
    {
      String location = entity.getRegisteredEntity().valueOf("path");
      customCompsSolRelDirs.add(location);
      resourceAbsDirs.add(PentahoRepositoryAccess.getPentahoSolutionPath(location));
      
      _logger.debug(String.format("Found CDE components location declared in %s [%s]", entity.getPlugin().getId(), location));
    }
  }
}
