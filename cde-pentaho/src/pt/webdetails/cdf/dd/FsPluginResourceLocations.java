/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cdf.dd.packager.input.OtherPluginStaticSystemOrigin;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.plugins.Plugin;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;
import pt.webdetails.cpf.plugins.PluginsAnalyzer.PluginPair;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

/**
 * @author dcleao
 */
public final class FsPluginResourceLocations
{
  private static final IReadAccess[] _resourceAbsDirs;
  private static final IReadAccess[] _customCompsSolRelDirs;
  
//  private List<PathOrigin> resourcesDirectories = new ArrayList<PathOrigin>();
  private List<PathOrigin> customComponentDirectories = new ArrayList<PathOrigin>(); 
  
  protected static final Log logger = LogFactory.getLog(FsPluginResourceLocations.class);

  static
  {
    
    ArrayList<IReadAccess> resourceAbsDirs = new ArrayList<IReadAccess>();
    ArrayList<IReadAccess> customCompsSolRelDirs = new ArrayList<IReadAccess>();
    
    initLocations(resourceAbsDirs, customCompsSolRelDirs);
    
    _resourceAbsDirs = resourceAbsDirs.toArray(new IReadAccess[resourceAbsDirs.size()]);
    _customCompsSolRelDirs = customCompsSolRelDirs.toArray(new IReadAccess[customCompsSolRelDirs.size()]);
  }
  
  public FsPluginResourceLocations()
  {
    initLocations();
  }
  
  public static IReadAccess[] getResourcesAbsDirs() //TODO: ever used?
  {
    return _resourceAbsDirs;
  }
  
  public static IReadAccess[] getCustomComponentsRelDirs()
  {
    return _customCompsSolRelDirs;
  }

  public List<PathOrigin> getCustomComponentLocations() {
    return customComponentDirectories;
  }
  private void initLocations() {
    // Add base locations
    //TODO: no, not like this
//    resourceAbsDirs.add(CdeEnvironment.getPluginSystemReader());
//    resourceAbsDirs.add(CdeEnvironment.getUserContentAccess());
    //FIXME need to add base?
    for (PathOrigin origin: CdeSettings.getCustomComponentLocations()){
        customComponentDirectories.add(origin);
    }

    // External component locations
    PluginsAnalyzer pluginsAnalyzer =
        new PluginsAnalyzer(CdeEnvironment.getContentAccessFactory(), PentahoSystem.get(IPluginManager.class));
    pluginsAnalyzer.refresh();

    // FIXME will fail often if not everytime
    for (PluginPair<List<Element>> entry : pluginsAnalyzer.getPluginsWithSection("/cde-components/path")) {
      for (Element pathNode : entry.getValue()) {
        String path = StringUtils.strip( pathNode.getStringValue() );
        String origin = pathNode.attributeValue("origin");//TODO:hcoded
        if (StringUtils.isEmpty(origin)) {
          //FIXME:
          customComponentDirectories.add(inferPathOriginFromLegacyLocation(entry.getPlugin(), path));
          logger.debug(String.format("Found CDE components location declared in %s [%s]", entry.getPlugin().getId(), path));
        }
      }

    }
  }

  private PathOrigin inferPathOriginFromLegacyLocation(Plugin plugin, String path) {
    //FIXME complete with pteixeira update after merge, assume system for now
    return new OtherPluginStaticSystemOrigin(plugin.getId(), RepositoryHelper.appendPath("../..", path));
  }

  private static void initLocations(ArrayList<IReadAccess> resourceAbsDirs, ArrayList<IReadAccess> customCompsSolRelDirs) {
    // Add base locations
    //TODO: do we really want system/pentaho-cdf-dd/* ?
    resourceAbsDirs.add(CdeEnvironment.getPluginSystemReader());
    resourceAbsDirs.add(CdeEnvironment.getUserContentAccess());

    for (IReadAccess componentsDir : CdeSettings.getComponentLocations()){
      customCompsSolRelDirs.add(componentsDir);
      resourceAbsDirs.add(componentsDir);
    }

    // External component locations
    PluginsAnalyzer pluginsAnalyzer =
        new PluginsAnalyzer(CdeEnvironment.getContentAccessFactory(), PentahoSystem.get(IPluginManager.class));
    pluginsAnalyzer.refresh();

    for(PluginsAnalyzer.PluginWithEntity entity : pluginsAnalyzer.getRegisteredEntities("/cde-components")) {
      String path = entity.getRegisteredEntity().valueOf("path");

      if (path != null) {

        path = path.startsWith("/") ? path.replaceFirst("/", "").toLowerCase().trim() : path.toLowerCase().trim();

        if (path.startsWith(DashboardDesignerContentGenerator.PLUGIN_PATH)) {

          path = path.replaceFirst(DashboardDesignerContentGenerator.PLUGIN_PATH, "");

          customCompsSolRelDirs.add(CdeEnvironment.getPluginSystemReader(path));
          resourceAbsDirs.add(CdeEnvironment.getPluginSystemReader(path));

    	  }else if(path.startsWith(CdeEnvironment.getSystemDir())){

    		  path = path.replaceFirst(CdeEnvironment.getSystemDir() + "/", ""); 
          path = path.replaceFirst(entity.getPlugin().getId().toLowerCase() + "/", "");

          customCompsSolRelDirs.add(CdeEnvironment.getOtherPluginSystemReader(entity.getPlugin().getId(), path));
          resourceAbsDirs.add(CdeEnvironment.getOtherPluginSystemReader(entity.getPlugin().getId(), path));

        } else if (path.startsWith(CdeEnvironment.getPluginRepositoryDir())) {

          path = path.replaceFirst(CdeEnvironment.getPluginRepositoryDir() + "/", "");

          customCompsSolRelDirs.add(CdeEnvironment.getPluginRepositoryReader(path));
          resourceAbsDirs.add(CdeEnvironment.getPluginRepositoryReader(path));

        } else {

          path = path.replaceFirst(entity.getPlugin().getId().toLowerCase() + "/", "");
          customCompsSolRelDirs.add(CdeEnvironment.getOtherPluginSystemReader(entity.getPlugin().getId(), path));
          resourceAbsDirs.add(CdeEnvironment.getOtherPluginSystemReader(entity.getPlugin().getId(), path));

        }

        logger.debug(String.format("Found CDE components location declared in %s [%s]", entity.getPlugin().getId(), entity.getRegisteredEntity().valueOf("path")));
      }
  	}
  }


}
