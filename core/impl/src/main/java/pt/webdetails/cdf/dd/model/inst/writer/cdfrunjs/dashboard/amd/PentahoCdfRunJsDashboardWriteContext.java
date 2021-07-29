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

import static pt.webdetails.cdf.dd.CdeConstants.Writer.SLASH;

public class PentahoCdfRunJsDashboardWriteContext extends CdfRunJsDashboardWriteContext {
  private static final String SIMPLE_TOKEN = "\\$\\{(\\w+)\\}";
  private static final String RESOURCE_TOKEN = "\\$\\{(\\w+):((/?)(.+?)(/?))\\}";

  // ------------

  static final String DASH_PATH_TAG = "dashboardPath";

  static final String SYSTEM_TAG = "system";
  static final String IMAGE_TAG = "img";

  static final String RES_TAG = "res";
  static final String SOLUTION_TAG = "solution";

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
    final Pattern SimplePattern = Pattern.compile( SIMPLE_TOKEN );
    Matcher simpleMatch = SimplePattern.matcher( content );
    while ( simpleMatch.find() ) {
      content = replaceToken( content, simpleMatch, getSimpleTokenReplacement( simpleMatch ) );
      simpleMatch = SimplePattern.matcher( content );
    }

    Pattern pattern = Pattern.compile( RESOURCE_TOKEN );
    Matcher resourceMatch = pattern.matcher( content );


    while ( resourceMatch.find() ) {
      content = replaceToken( content, resourceMatch, getResourceTokenReplacement( resourceMatch ) );
      resourceMatch = pattern.matcher( content );
    }

    return content;
  }

  // region Simple Token
  private String getSimpleTokenReplacement( Matcher token ) {
    if ( isDashboardPathTag( token ) ) {
      return getDashboardPathReplacement();
    }

    // Only "dashboardPath" has a replacement for now
    return null;
  }

  private String getDashboardPathReplacement() {
    String dashboardPath = getDashboardSourcePath().replaceAll( "(^/.*/$)", "$1" );

    return replaceWhiteSpaces( dashboardPath );
  }

  private boolean isDashboardPathTag( Matcher token ) {
    return DASH_PATH_TAG.equals( token.group( 1 ) );
  }
  // endregion

  // region Resource Token
  private String getResourceTokenReplacement( Matcher resource ) {
    // build system resource links
    if ( isSystemTag( resource ) ) {
      return getResourceReplacement( resource, getSystemRoot() );
    }

    // build image links, with a timestamp for caching purposes
    if ( isImageTag( resource ) ) {
      return getResourceReplacement( resource, getPentahoResourceEndpoint() ) + "?v=" + getWriteDate().getTime();
    }

    // build resource links
    if ( isResourceTag( resource ) ) {
      return getResourceReplacement( resource, "" );
    }

    return null;
  }

  private String getResourceReplacement( Matcher tagMatcher, String absoluteRoot ) {
    StringBuilder replacedContent = new StringBuilder( absoluteRoot );

    if ( !isSystemTag( tagMatcher ) ) {
      replacedContent.append( isRelativeResource( tagMatcher ) ? getDashboardSourcePath() : "" );
    }

    replacedContent.append( getResourcePath( tagMatcher ) );

    return replaceWhiteSpaces( replacedContent.toString() );
  }

  private String getResourcePath( Matcher resource ) {
    String path = resource.group( 2 );

    if ( isSystemTag( resource ) ) {
      path = path.replaceFirst( "^/", "" );
    }

    return path;
  }

  private boolean isImageTag( Matcher resource ) {
    return IMAGE_TAG.equals( resource.group( 1 ) );
  }

  private boolean isSystemTag( Matcher resource ) {
    return SYSTEM_TAG.equals( resource.group( 1 ) );
  }

  private boolean isResourceTag( Matcher resource ) {
    final String tag = resource.group( 1 );

    return SOLUTION_TAG.equals( tag ) || RES_TAG.equals( tag );
  }

  private boolean isRelativeResource( Matcher resource ) {
    return !SLASH.equals( resource.group( 3 ) );
  }
  // endregion

  private String replaceToken( String content, Matcher match, String replacement ) {
    if ( replacement != null ) {
      content = content.substring( 0, match.start() ) + replacement + content.substring( match.end() );
    }

    return content;
  }

  private String getSystemRoot() {
    String pluginId = getSystemPluginId();
    if ( StringUtils.isEmpty( pluginId ) ) {
      pluginId = "";
    }

    return getPentahoResourceEndpoint() + SLASH + getSystemDir() + SLASH + pluginId + SLASH;
  }
}
