/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cdf.dd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.plugins.IPluginFilter;
import pt.webdetails.cpf.plugins.Plugin;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;

import java.util.List;

public class CdePlugins {
  private static Log logger = LogFactory.getLog( CdePlugins.class );

  public String getCdePlugins() {

    JSONArray pluginsArray = new JSONArray();

    PluginsAnalyzer pluginsAnalyzer =
        new PluginsAnalyzer( CdeEnvironment.getContentAccessFactory(), PentahoSystem.get( IPluginManager.class ) );
    pluginsAnalyzer.refresh();

    IPluginFilter pluginFilter = new IPluginFilter() {
      public boolean include( Plugin plugin ) {
        boolean include = false;
        if ( plugin.hasSettingsXML() ) {
          include =
            ( plugin.getXmlValue( "/settings/cde-compatible", "settings.xml" ).equals( "true" ) ) ? true : false;
        }
        return include;
      }
    };

    List<Plugin> cdePlugins = pluginsAnalyzer.getPlugins( pluginFilter );
    // TODO: plugin is json serializable...
    for ( Plugin plugin : cdePlugins ) {
      try {
        JSONObject pluginObject = new JSONObject();
        pluginObject.put( "title", plugin.getId() );
        pluginObject.put( "name", plugin.getName() );
        pluginObject.put( "description", plugin.getXmlValue( "/settings/description", "settings.xml" ) );
        pluginObject.put( "url", plugin.getXmlValue( "/settings/url", "settings.xml" ) );
        pluginObject.put( "jsPath", plugin.getXmlValue( "/settings/jsPath", "settings.xml" ) );
        pluginObject.put( "pluginId", plugin.getId() );

        pluginsArray.put( pluginObject );
      } catch ( Exception e ) {
        logger.error( e );
      }
    }

    logger.debug( "Feeding client with CDE-Compatible plugin list" );

    return pluginsArray.toString();
  }

}
