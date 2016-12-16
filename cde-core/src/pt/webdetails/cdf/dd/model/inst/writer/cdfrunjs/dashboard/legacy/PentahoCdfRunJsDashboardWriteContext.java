/*!
 * Copyright 2002 - 2016 Webdetails, a Pentaho company. All rights reserved.
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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy;

import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;

public class PentahoCdfRunJsDashboardWriteContext extends CdfRunJsDashboardWriteContext {
  private static final String RESOURCE_API_GET = "api/resources";
  private static final String SYS_RESOURCE_API_GET = "/pentaho/api/repos";

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
    final long timestamp = this._writeDate.getTime();

    final String root = getRoot();

    final String path = this._dash.getSourcePath().replaceAll( "(.+/).*", "$1" );

    return content
      // replace the dashboard path token
      .replaceAll( DASHBOARD_PATH_TAG, path.replaceAll( "(^/.*/$)", "$1" ) )
      // build the image links, with a timestamp for caching purposes
      .replaceAll( ABS_IMG_TAG, root + RESOURCE_API_GET + "$1" + "?v=" + timestamp )
      .replaceAll( REL_IMG_TAG, root + RESOURCE_API_GET + path + "$1" + "?v=" + timestamp )
      // Directories don't need the caching timestamp
      .replaceAll( ABS_DIR_RES_TAG, root + RESOURCE_API_GET + "$2" )
      .replaceAll( REL_DIR_RES_TAG, root + RESOURCE_API_GET + path + "$2" )
      // build the resource links, with a timestamp for caching purposes
      .replaceAll( ABS_RES_TAG, root + RESOURCE_API_GET + "$2" + "?v=" + timestamp )
      .replaceAll( REL_RES_TAG, root + RESOURCE_API_GET + path + "$2" + "?v=" + timestamp )
      // build the system resource links, with a timestamp for caching purposes
      .replaceAll( ABS_SYS_RES_TAG, root + RESOURCE_API_GET + "/" + getSystemDir() + "/"
        + getPluginId( path ) + "$1" + "?v=" + timestamp )
      .replaceAll( REL_SYS_RES_TAG, root + RESOURCE_API_GET + path + "$1" + "?v=" + timestamp );
  }
}
