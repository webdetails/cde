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
package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import java.util.Date;
import org.apache.commons.lang.StringUtils;

import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.ICdeEnvironment;
import pt.webdetails.cdf.dd.model.core.writer.DefaultThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.Dashboard;

public abstract class CdfRunJsDashboardWriteContext extends DefaultThingWriteContext {
  private static final String COMPONENT_PREFIX = "render_";

  private static final String SHORT_H_TAG = "\\$\\{h:(.+?)\\}";
  private static final String SHORT_C_TAG = "\\$\\{c:(.+?)\\}";
  private static final String SHORT_P_TAG = "\\$\\{p:(.+?)\\}";

  private static final String LONG_H_TAG = "\\$\\{htmlObject:(.+?)\\}";
  private static final String LONG_C_TAG = "\\$\\{component:(.+?)\\}";
  private static final String LONG_P_TAG = "\\$\\{parameter:(.+?)\\}";

  // ------------

  // Endpoints
  public static final String RESOURCE_API_GET = "api/resources";

  private boolean _isFirstInList = true;
  private final Date _writeDate;
  private final String _indent;
  private final Dashboard _dash;
  private final boolean _bypassCacheRead;

  private final CdfRunJsDashboardWriteOptions _options;

  public CdfRunJsDashboardWriteContext( IThingWriterFactory factory, String indent, boolean bypassCacheRead,
                                        Dashboard dash, // Current Dashboard/Widget
                                        CdfRunJsDashboardWriteOptions options ) {
    super( factory, true );

    if ( dash == null ) {
      throw new IllegalArgumentException( "dash" );
    }

    if ( options == null ) {
      throw new IllegalArgumentException( "options" );
    }

    this._indent = StringUtils.defaultIfEmpty( indent, "" );
    this._bypassCacheRead = bypassCacheRead;
    this._dash = dash;
    this._options = options;
    this._writeDate = new Date();
  }

  protected CdfRunJsDashboardWriteContext( CdfRunJsDashboardWriteContext other, String indent ) {
    super( other.getFactory(), other.getBreakOnError() );

    this._indent = StringUtils.defaultIfEmpty( indent, "" );
    this._bypassCacheRead = other._bypassCacheRead;
    this._dash = other._dash;
    this._options = other._options;
    this._writeDate = other._writeDate;
  }

  public CdfRunJsDashboardWriteContext withIndent( String indent ) {
    return getCdeEnvironment()
      .getCdfRunJsDashboardWriteContext( getFactory(), indent, isBypassCacheRead(), getDashboard(), getOptions() );
  }

  // region Getters
  public String getIndent() {
    return this._indent;
  }

  public boolean isBypassCacheRead() {
    return this._bypassCacheRead;
  }

  public Date getWriteDate() {
    return this._writeDate;
  }

  public Dashboard getDashboard() {
    return this._dash;
  }

  public boolean isFirstInList() {
    return this._isFirstInList;
  }

  public void setIsFirstInList( boolean isFirst ) {
    this._isFirstInList = isFirst;
  }

  public CdfRunJsDashboardWriteOptions getOptions() {
    return this._options;
  }

  public String getId( Component<?> comp ) {
    return comp.buildId( this.getOptions().getAliasPrefix() );
  }
  // endregion

  // --------------

  public String replaceTokensAndAlias( String content ) {
    return this.replaceAlias( this.replaceTokens( content ) );
  }

  public abstract String replaceTokens( String content );

  private String replaceAlias( String content ) {
    if ( content == null ) {
      return "";
    }

    String alias = this.getOptions().getAliasPrefix();

    String aliasAndName = ( StringUtils.isNotEmpty( alias ) ? ( alias + "_" ) : "" ) + "$1";

    return content
      .replaceAll( SHORT_C_TAG, COMPONENT_PREFIX + aliasAndName )
      .replaceAll( LONG_C_TAG, COMPONENT_PREFIX + aliasAndName )
      .replaceAll( SHORT_H_TAG, aliasAndName )
      .replaceAll( SHORT_P_TAG, aliasAndName )
      .replaceAll( LONG_H_TAG, aliasAndName )
      .replaceAll( LONG_P_TAG, aliasAndName );
  }

  public String replaceHtmlAlias( String content ) {
    if ( content == null ) {
      return "";
    }

    String alias = this.getOptions().getAliasPrefix();

    String aliasAndName = ( StringUtils.isNotEmpty( alias ) ? ( alias + "_" ) : "" ) + "$1";

    return content
      .replaceAll( SHORT_C_TAG, COMPONENT_PREFIX + "$1" )
      .replaceAll( LONG_C_TAG, COMPONENT_PREFIX + "$1" )
      .replaceAll( SHORT_H_TAG, aliasAndName )
      .replaceAll( SHORT_P_TAG, "$1" )
      .replaceAll( LONG_H_TAG, aliasAndName )
      .replaceAll( LONG_P_TAG, "$1" );
  }

  protected String getSystemPluginId() {
    String path =  getDashboardSourcePath();

    if ( path.startsWith( "/" ) ) {
      path = path.replaceFirst( "/", "" );
    }

    if ( path.startsWith( getSystemDir() ) ) {
      return path.split( "/" )[ 1 ];
    } else {
      return "";
    }
  }

  // region private/protected aux methods
  protected String replaceWhiteSpaces( String value ) {
    return value.replaceAll( " ", "%20" );
  }

  protected String getDashboardSourcePath() {
    String path = this.getDashboard().getSourcePath().replaceAll( "(.+/).*", "$1" );

    return replaceWhiteSpaces( path );
  }

  protected String getPentahoResourceEndpoint() {
    String endpoint = getRoot() + RESOURCE_API_GET;

    return replaceWhiteSpaces( endpoint );
  }

  protected String getRoot() {
    final CdfRunJsDashboardWriteOptions options = this.getOptions();

    final String schemeRoot = options.isAbsolute() && StringUtils.isNotEmpty( options.getAbsRoot() )
      ? options.getSchemedRoot() : "";

    return schemeRoot + getCdeEnvironment().getApplicationBaseContentUrl();
  }

  private ICdeEnvironment getCdeEnvironment() {
    return CdeEngine.getEnv();
  }

  protected String getSystemDir() {
    return getCdeEnvironment().getSystemDir();
  }
  // endregion
}
