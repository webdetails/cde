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
