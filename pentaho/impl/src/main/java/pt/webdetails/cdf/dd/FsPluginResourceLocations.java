/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cdf.dd;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import pt.webdetails.cpf.packager.origin.PathOrigin;
import pt.webdetails.cpf.packager.origin.OtherPluginStaticSystemOrigin;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.plugins.Plugin;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;
import pt.webdetails.cpf.plugins.PluginsAnalyzer.PluginPair;

public final class FsPluginResourceLocations {

  private List<PathOrigin> customComponentDirectories = new ArrayList<PathOrigin>();

  protected static final Log logger = LogFactory.getLog( FsPluginResourceLocations.class );

  public FsPluginResourceLocations() {
    initLocations();
  }


  public List<PathOrigin> getCustomComponentLocations() {
    return customComponentDirectories;
  }

  private void initLocations() {

    for ( PathOrigin origin : CdeSettings.getCustomComponentLocations() ) {
      customComponentDirectories.add( origin );
    }

    // External component locations
    PluginsAnalyzer pluginsAnalyzer =
      new PluginsAnalyzer( CdeEnvironment.getContentAccessFactory(), PentahoSystem.get( IPluginManager.class ) );
    pluginsAnalyzer.refresh();

    // FIXME will fail often if not everytime
    for ( PluginPair<List<Element>> entry : pluginsAnalyzer.getPluginsWithSection( "/cde-components/path" ) ) {
      for ( Element pathNode : entry.getValue() ) {
        String path = StringUtils.strip( pathNode.getStringValue() );
        String origin = pathNode.attributeValue( "origin" );//TODO:hcoded
        if ( StringUtils.isEmpty( origin ) ) {
          //FIXME:
          customComponentDirectories.add( inferPathOriginFromLegacyLocation( entry.getPlugin(), path ) );
          logger.debug(
            String.format( "Found CDE components location declared in %s [%s]", entry.getPlugin().getId(), path ) );
        }
      }

    }
  }

  private PathOrigin inferPathOriginFromLegacyLocation( Plugin plugin, String path ) {
    //FIXME need extra method! and don't always assume system
    if ( path.startsWith( "system" ) ) {
      path = StringUtils.removeStart( path, "system/" );
      path = path.substring( path.indexOf( "/" ) );
    }
    return new OtherPluginStaticSystemOrigin( plugin.getId(), path );
  }

}
