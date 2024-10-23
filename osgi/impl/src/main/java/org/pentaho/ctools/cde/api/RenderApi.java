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


package org.pentaho.ctools.cde.api;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.CdeConstants.MethodParams;
import pt.webdetails.cdf.dd.DashboardManager;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;

@Path( "renderer" )
public class RenderApi {

  private static final Log logger = LogFactory.getLog( RenderApi.class );
  private DashboardManager dashboardManager;

  @GET
  @Path( "/getDashboard" )
  @Produces( APPLICATION_JSON )
  public String getDashboard( @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
                              @QueryParam( MethodParams.INFERSCHEME ) @DefaultValue( "false" ) boolean inferScheme,
                              @QueryParam( MethodParams.ROOT ) @DefaultValue( "" ) String root,
                              @QueryParam( MethodParams.ABSOLUTE ) @DefaultValue( "true" ) boolean absolute,
                              @QueryParam( MethodParams.BYPASSCACHE ) @DefaultValue( "false" ) boolean bypassCache,
                              @QueryParam( MethodParams.DEBUG ) @DefaultValue( "false" ) boolean debug,
                              @QueryParam( MethodParams.SCHEME ) @DefaultValue( "" ) String scheme,
                              @QueryParam( MethodParams.STYLE ) @DefaultValue( "" ) String style,
                              @QueryParam( MethodParams.ALIAS ) @DefaultValue( "" ) String alias,
                              @Context HttpServletRequest request ) {

    final String schemeToUse;
    if ( !inferScheme ) {
      schemeToUse = ( scheme == null || scheme.length() == 0 ) ? request.getScheme() : scheme;
    } else {
      schemeToUse = "";
    }

    if ( path == null || path.length() == 0 ) {
      logger.warn( "No path provided." );
      return "No path provided.";
    }

    try {
      CdfRunJsDashboardWriteResult dashboard = getDashboardModule(
        path, schemeToUse, root, absolute, bypassCache, debug, style, alias );

      // TODO: i18n for a required dashboard
      // TODO: get dashboard initial context and storage from CDF plugin

      return dashboard.getContent();
    } catch ( Exception ex ) {
      String msg = "Could not load dashboard: " + ex.getMessage();
      logger.error( msg, ex );
      return msg;
    }
  }

  @GET
  @Path( "/getDashboardParameters" )
  @Produces( APPLICATION_JSON )
  public String getDashboardParameters(
    @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
    @QueryParam( MethodParams.BYPASSCACHE ) @DefaultValue( "false" ) boolean bypassCache,
    @QueryParam( MethodParams.ALLPARAMS ) @DefaultValue( "false" ) boolean all ) {

    if ( path == null || path.length() == 0 ) {
      logger.warn( "No path provided." );
      return "No path provided.";
    }

    try {
      return getDashboardManager().getDashboardParameters( path, bypassCache, all );
    } catch ( Exception ex ) {
      String msg = "Could not load dashboard parameters: " + ex.getMessage();
      logger.error( msg, ex );
      return msg;
    }
  }

  private CdfRunJsDashboardWriteResult getDashboardModule( String path, String scheme, String root,
                                                           boolean absolute, boolean bypassCache, boolean debug,
                                                           String style, String alias )
    throws ThingWriteException {

    final String dashboardAlias = getDashboardAlias( path, alias );

    CdfRunJsDashboardWriteOptions options =
      new CdfRunJsDashboardWriteOptions( dashboardAlias, true, absolute, debug, root, scheme );

    return getDashboardManager().getDashboardCdfRunJs( path, options, bypassCache, style );

  }

  public DashboardManager getDashboardManager() {
    return this.dashboardManager;
  }

  public void setDashboardManager( DashboardManager dashboardManager ) {
    this.dashboardManager = dashboardManager;
  }

  /**
   * Returns a dashboard alias built using its filename and an alias. If no alias parameter is provided a default value
   * will be used.
   *
   * @param path The dashboard full path.
   * @param alias A unique alias.
   * @return The dashboard alias.
   */
  private String getDashboardAlias( final String path, final String alias ) {
    String dashboardAlias;
    int index = path.lastIndexOf( "/" );
    if ( index > -1 ) {
      dashboardAlias = path.substring( index + 1 );
    } else {
      dashboardAlias = path;
    }
    index = dashboardAlias.lastIndexOf( "." );
    if ( index > -1 ) {
      dashboardAlias = dashboardAlias.substring( 0, index );
    }
    if ( alias == null || alias.length() == 0 ) {
      dashboardAlias += "_" + CdeConstants.DASHBOARD_ALIAS_TAG;
    } else {
      dashboardAlias += "_" + alias;
    }
    return dashboardAlias;
  }
}
