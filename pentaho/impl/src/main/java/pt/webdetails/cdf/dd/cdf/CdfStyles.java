/*!
 * Copyright 2002 - 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd.cdf;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import pt.webdetails.cdf.dd.DashboardDesignerException;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.GenericBasicFileFilter;

import java.util.ArrayList;
import java.util.List;

import pt.webdetails.cpf.plugins.PluginsAnalyzer;
import pt.webdetails.cpf.plugins.Plugin;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IReadAccess;

// TODO: move to core once pluginsAnalizer gets no pentaho-dependencies
public class CdfStyles {
  public static final String DEFAULTSTYLE = "Clean";
  private static final String SYSTEM_RESOURCE_STYLES_DIR = "resources/styles/";
  private static final String RESOURCE_STYLES_DIR_SOLUTION = "styles/";
  private static Log logger = LogFactory.getLog( CdfStyles.class );

  public CdfStyles() {
  }

  public Object liststyles() throws DashboardDesignerException {

    JSONArray result = new JSONArray();

    List<Style> styles = new ArrayList<Style>();
    Style style = null;
    style = new Style( CdeEnvironment.getPluginSystemReader(), SYSTEM_RESOURCE_STYLES_DIR, null );
    styles.add( style );
    style = new Style( CdeEnvironment.getPluginRepositoryReader(), RESOURCE_STYLES_DIR_SOLUTION, null );
    styles.add( style );

    PluginsAnalyzer pluginsAnalyzer =
      new PluginsAnalyzer( CdeEnvironment.getContentAccessFactory(), PentahoSystem.get( IPluginManager.class ) );
    pluginsAnalyzer.refresh();

    List<PluginsAnalyzer.PluginWithEntity> entities = pluginsAnalyzer.getRegisteredEntities( "/cde-styles" );

    for ( PluginsAnalyzer.PluginWithEntity entity : entities ) {

      String pluginStylesDir = entity.getRegisteredEntity().valueOf( "path" );
      String finalPath = pluginStylesDir + "/";
      String pluginId = entity.getPlugin().getId();
      style = null;

      IReadAccess access = CdeEnvironment.getOtherPluginSystemReader( pluginId );

      //Clean final path if it starts with sytem/pluginId/
      //Allows specification for style paths to be in the form (assuming styles is the folder):
      // * system/<plugin>/styles
      // * /system/<plugin>/styles - no need to clear start info
      // * styles
      if ( finalPath.startsWith( "system/" + pluginId + "/" ) ) {
        finalPath = finalPath.substring( ( "system/" + pluginId + "/" ).length() );
      }

      if ( access.fileExists( finalPath ) && access.fetchFile( finalPath ).isDirectory() ) {
        style = new Style( access, finalPath, pluginId );
        styles.add( style );
      }
    }

    if ( styles == null || styles.isEmpty() ) {
      logger.error( "No styles directory found in resources" );
      styles = new ArrayList<Style>();
    }

    for ( Style s : styles ) {
      List<IBasicFile> styleFiles = s.getStyleFiles();

      for ( IBasicFile file : styleFiles ) {
        String name = file.getName();
        result.put( name.substring( 0, name.lastIndexOf( '.' ) ) + s.getSufixPluginName() );
      }
    }

    return result;
  }

  public String getResourceLocation( String style ) {
    String stylePath = null;

    if ( StringUtils.isEmpty( style ) ) {
      style = DEFAULTSTYLE;
    }

    String styleFilename;
    String[] split = style.split( " - " );
    if ( split.length > 1 ) {
      String pluginId = split[ 1 ].replace( "(", "" ).replace( ")", "" );

      styleFilename = split[ 0 ] + ".html";

      PluginsAnalyzer pluginsAnalizer = new PluginsAnalyzer();
      pluginsAnalizer.refresh();

      List<Plugin> plugins = pluginsAnalizer.getInstalledPlugins();

      for ( Plugin plugin : plugins ) {
        if ( plugin.getId().equalsIgnoreCase( pluginId ) ) {
          stylePath = "/" + plugin.getRegisteredEntities( "/cde-styles" ).valueOf( "path" ) + "/" + styleFilename;
          break;
        }
      }

    } else {
      styleFilename = style + ".html";

      String customStylePath = RESOURCE_STYLES_DIR_SOLUTION + styleFilename;

      if ( CdeEnvironment.getPluginRepositoryReader().fileExists( customStylePath ) ) {
        stylePath = customStylePath;

      } else if ( CdeEnvironment.getPluginSystemReader( SYSTEM_RESOURCE_STYLES_DIR ).fileExists( styleFilename ) ) {
        stylePath = SYSTEM_RESOURCE_STYLES_DIR + styleFilename;
      }
    }

    return stylePath;
  }

  private class Style {
    String pluginId = null;
    IReadAccess access;
    String directory;
    List<IBasicFile> styleFiles = null;

    public Style( IReadAccess access, String directory, String pluginName ) {

      this.access = access;
      this.pluginId = pluginName;
      this.directory = directory;
      styleSelfBuild();
    }

    private void styleSelfBuild() {
      styleFiles = new ArrayList<IBasicFile>();
      final GenericBasicFileFilter htmlFilter = new GenericBasicFileFilter( null, ".html" );

      List<IBasicFile> htmlList = access.listFiles( directory, htmlFilter, IReadAccess.DEPTH_ALL, false, true );

      if ( htmlList != null ) {
          styleFiles.addAll( htmlList );
      }
    }

    public String getPluginId() {
      return pluginId;
    }

    public String getSufixPluginName() {
      String sufix = getPluginId();

      if ( sufix == null ) {
        sufix = "";
      } else {
        sufix = " - (" + getPluginId() + ")";
      }

      return sufix;
    }

    public List<IBasicFile> getStyleFiles() {
      return styleFiles;
    }
  }
}
