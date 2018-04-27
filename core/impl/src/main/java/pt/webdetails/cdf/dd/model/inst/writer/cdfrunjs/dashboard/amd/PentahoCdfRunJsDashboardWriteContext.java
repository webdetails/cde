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

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PentahoCdfRunJsDashboardWriteContext extends CdfRunJsDashboardWriteContext {
  private static final String RESOURCE_TAG = "\\$\\{(\\w+):((/?)(.+?)(/?))\\}";

  // ------------

  private static final String CXF_RESOURCE_API_GET = "/cxf/cde/resources";

  private static final String SYSTEM_RESOURCE = "system";
  private static final String OSGI_RESOURCE = "osgi";
  private static final String IMAGE_RESOURCE = "img";

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
    // replace the dashboard path token
    if ( content.matches( DASHBOARD_PATH_TAG ) ) {
      return replaceDashboardPathTag();
    }

    if ( content.matches( RESOURCE_TAG ) ) {
      return replaceTagAmd( content );
    }

    return content;
  }

  private String replaceTagAmd( String content ) {
    // build system resource links
    if ( isSystemResource( content ) ) {
      return replaceTag( content, getSystemRoot() );
    }

    // build image links, with a timestamp for caching purposes
    if ( isImageResource( content ) ) {
      return replaceTag( content, getPentahoResourceEndpoint() ) + "?v=" + getWriteDate().getTime();
    }

    // build osgi resource links
    if ( isOsgiResource( content ) ) {
      return replaceTag( content, getOsgiResourceEndpoint() );
    }

    // build directory links
    // build resource links
    // build foundry resources links
    // build system resource links
    return replaceTag( content, "" );
  }

  // region replaceTag
  private String replaceDashboardPathTag() {
    String dashboardPath = getDashboardSourcePath().replaceAll( "(^/.*/$)", "$1" );

    return replaceWhiteSpaces( dashboardPath );
  }

  private String replaceTag( String content, String absoluteRoot ) {
    final Matcher tagMatcher = Pattern.compile( RESOURCE_TAG ).matcher( content );

    StringBuilder replacedContent = new StringBuilder( absoluteRoot );

    final boolean isSystemResource = isSystemResource( content );
    if ( !isSystemResource ) {
      replacedContent.append( !isAbsoluteResource( tagMatcher ) ? getDashboardSourcePath() : "" );
    }

    replacedContent.append( getResourcePath( tagMatcher, isSystemResource ) );

    return replaceWhiteSpaces( replacedContent.toString() );
  }
  // endregion

  // region Resource Regx
  private String getResourcePath( Matcher resource, boolean isSystem ) {
    String path = resource.replaceAll( "$2" );

    if ( isSystem ) {
      path = path.replaceFirst( "^/", "" );
    }

    return path;
  }

  private boolean isImageResource( String content ) {
    final Matcher resource = Pattern.compile( RESOURCE_TAG ).matcher( content );

    return IMAGE_RESOURCE.equals( resource.replaceAll( "$1" ) );
  }

  private boolean isOsgiResource( String content ) {
    final Matcher resource = Pattern.compile( RESOURCE_TAG ).matcher( content );

    return OSGI_RESOURCE.equals( resource.replaceAll( "$1" ) );
  }

  private boolean isSystemResource( String content ) {
    final Matcher resource = Pattern.compile( RESOURCE_TAG ).matcher( content );

    return SYSTEM_RESOURCE.equals( resource.replaceAll( "$1" ) );
  }

  private boolean isAbsoluteResource( Matcher resource ) {
    return SLASH.equals( resource.replaceAll( "$3" ) );
  }
  // endregion

  private String getSystemRoot() {
    String pluginId = getPluginId( getDashboardSourcePath() );
    if ( StringUtils.isEmpty( pluginId ) ) {
      pluginId = "";
    }

    return getPentahoResourceEndpoint() + SLASH + getSystemDir() + SLASH + pluginId + SLASH;
  }

  private String getOsgiResourceEndpoint() {
    return CXF_RESOURCE_API_GET;
  }
}
