/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.amd;

import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;

public class PentahoCdfRunJsDashboardWriteContext extends CdfRunJsDashboardWriteContext {

  public PentahoCdfRunJsDashboardWriteContext( IThingWriterFactory factory,
                                               String indent, boolean bypassCacheRead, Dashboard dash,
                                               CdfRunJsDashboardWriteOptions options ) {
    super( factory, indent, bypassCacheRead, dash, options );
  }

  public PentahoCdfRunJsDashboardWriteContext( CdfRunJsDashboardWriteContext factory, String indent ) {
    super( factory, indent );
  }

  @Override
  public String replaceTokens( String content ) {
    final String path = this.getDashboard().getSourcePath().replaceAll( "(.+/).*", "$1" );
    final String pluginId = getPluginId( path );

    return content
      // replace the dashboard path token
      .replaceAll( DASHBOARD_PATH_TAG, getDashboardPath( path ) )

      // build the directory links
      .replaceAll( ABS_DIR_RES_TAG, getResourceReplacement( "" ) )
      .replaceAll( REL_DIR_RES_TAG, getResourceReplacement( path ) )

      // build the resource links
      .replaceAll( ABS_RES_TAG, getResourceReplacement( "" ) )
      .replaceAll( REL_RES_TAG, getResourceReplacement( path ) )

      // build foundry resources links
      .replaceAll( ABS_OSGI_RES_TAG, getOsgiResourceReplacement( "" ) )
      .replaceAll( REL_OSGI_RES_TAG, getOsgiResourceReplacement( path ) )

      // build the image links
      .replaceAll( ABS_IMG_TAG, getImageResourceReplacement( "" ) )
      .replaceAll( REL_IMG_TAG, getImageResourceReplacement( path ) )

      //build the system resource links
      .replaceAll( ABS_SYS_RES_TAG, getSystemResourceReplacement( pluginId ) )
      .replaceAll( REL_SYS_RES_TAG, getSystemResourceReplacement( pluginId, true ) );
  }

  @Override
  protected String getResourceReplacement( String path ) {
    return path + "$1";
  }

  private String getOsgiResourceReplacement( String path ) {
    return OSGI_RESOURCE_API_GET + path + "$1";
  }
}
