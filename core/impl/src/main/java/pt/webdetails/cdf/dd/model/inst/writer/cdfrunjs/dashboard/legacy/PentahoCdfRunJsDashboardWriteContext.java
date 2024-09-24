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
package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;

import static pt.webdetails.cdf.dd.CdeConstants.Writer.SLASH;

public class PentahoCdfRunJsDashboardWriteContext extends CdfRunJsDashboardWriteContext {
  private static final String DASHBOARD_PATH_TAG = "\\$\\{dashboardPath\\}";

  private static final String ABS_DIR_RES_TAG = "\\$\\{(?:res|solution):(/.+/)\\}";
  private static final String REL_DIR_RES_TAG = "\\$\\{(?:res|solution):(.+/)\\}";

  private static final String ABS_RES_TAG = "\\$\\{(?:res|solution):(/.+)\\}";
  private static final String REL_RES_TAG = "\\$\\{(?:res|solution):(.+)\\}";

  private static final String ABS_IMG_TAG = "\\$\\{img:(/.+)\\}";
  private static final String REL_IMG_TAG = "\\$\\{img:(.+)\\}";

  private static final String ABS_SYS_RES_TAG = "\\$\\{system:(/.+)\\}";
  private static final String REL_SYS_RES_TAG = "\\$\\{system:(.+)\\}";

  public PentahoCdfRunJsDashboardWriteContext( IThingWriterFactory factory, String indent,
                                               boolean bypassCacheRead, Dashboard dash,
                                               CdfRunJsDashboardWriteOptions options ) {
    super( factory, indent, bypassCacheRead, dash, options );
  }

  public PentahoCdfRunJsDashboardWriteContext( CdfRunJsDashboardWriteContext factory, String indent ) {
    super( factory, indent );
  }

  @Override
  public String replaceTokens( String content ) {
    final long timestamp = this.getWriteDate().getTime();

    return content
      // replace the dashboard path token
      .replaceAll( DASHBOARD_PATH_TAG, getDashPathReplacement() )

      // build the image links, with a timestamp for caching purposes
      .replaceAll( ABS_IMG_TAG, getAbsResourceReplacement( timestamp ) )
      .replaceAll( REL_IMG_TAG, getRelResourceReplacement( timestamp ) )

      // Directories don't need the caching timestamp
      .replaceAll( ABS_DIR_RES_TAG, getAbsResourceReplacement( null ) )
      .replaceAll( REL_DIR_RES_TAG, getRelResourceReplacement( null ) )

      // build the resource links, with a timestamp for caching purposes
      .replaceAll( ABS_RES_TAG, getAbsResourceReplacement( timestamp ) )
      .replaceAll( REL_RES_TAG, getRelResourceReplacement( timestamp ) )

      // build the system resource links, with a timestamp for caching purposes
      .replaceAll( ABS_SYS_RES_TAG, getSystemResourceReplacement( true ) )
      .replaceAll( REL_SYS_RES_TAG, getSystemResourceReplacement( false ) );
  }

  private String getDashPathReplacement() {
    String path = getDashboardSourcePath();

    return replaceWhiteSpaces( path.replaceAll( "(^/.*/$)", "$1" ) );
  }

  private String getAbsResourceReplacement( Long timestamp ) {
    final String resourceEndpoint = getPentahoResourceEndpoint();

    return getResourceReplacement( resourceEndpoint, timestamp );
  }

  private String getRelResourceReplacement( Long timestamp ) {
    final String resourceEndpoint = getPentahoResourceEndpoint();
    final String path = getDashboardSourcePath();

    return getResourceReplacement( resourceEndpoint + path, timestamp );
  }

  private String getSystemResourceReplacement( boolean isAbsolute ) {
    final String resourceEndpoint = getPentahoResourceEndpoint();

    final String token = isAbsolute ? "$1" : "/$1";
    final String pluginId = getSystemPluginId();

    return replaceWhiteSpaces( resourceEndpoint + SLASH + getSystemDir()
      + ( StringUtils.isEmpty( pluginId ) ? token : "/" + pluginId + token ) );
  }

  private String getResourceReplacement( String path, Long timestamp ) {
    return replaceWhiteSpaces( path + "$1" + ( timestamp != null ? "?v=" + timestamp : "" ) );
  }
}
