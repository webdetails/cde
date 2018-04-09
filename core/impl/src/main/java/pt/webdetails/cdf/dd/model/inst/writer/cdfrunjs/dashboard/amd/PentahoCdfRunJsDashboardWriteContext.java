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

  public PentahoCdfRunJsDashboardWriteContext( CdfRunJsDashboardWriteContext factory,
                                               String indent ) {
    super( factory, indent );
  }

  @Override
  public String replaceTokens( String content ) {
    final long timestamp = this.getWriteDate().getTime();

    final String path = this.getDashboard().getSourcePath().replaceAll( "(.+/).*", "$1" );
    final String pluginId = getPluginId( path );

    final String absoluteResourceRoot = getRoot() + RESOURCE_API_GET;
    final String relativeResourceRoot = absoluteResourceRoot + path;

    return content
      // replace the dashboard path token
      .replaceAll( DASHBOARD_PATH_TAG, getDashboardPath( path ) )

      // build the image links
      .replaceAll( ABS_IMG_TAG, getResourceReplacement( "$1", absoluteResourceRoot, timestamp ) )
      .replaceAll( REL_IMG_TAG, getResourceReplacement( "$1", relativeResourceRoot, timestamp ) )

      // build the directory links
      .replaceAll( ABS_DIR_RES_TAG, getResourceReplacement( "$2", "", null ) )
      .replaceAll( REL_DIR_RES_TAG, getResourceReplacement( "$2", path, null ) )

      // build the resource links
      .replaceAll( ABS_RES_TAG, getResourceReplacement( "$2", "", null ) )
      .replaceAll( REL_RES_TAG, getResourceReplacement( "$2", path, null ) )

      //build the system resource links
      .replaceAll( ABS_SYS_RES_TAG, getSystemResourceReplacement( "$1", absoluteResourceRoot, pluginId ) )
      .replaceAll( REL_SYS_RES_TAG, getSystemResourceReplacement( "/$1", absoluteResourceRoot, pluginId ) );
  }
}
