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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.ctools.cde.impl.DashboardsImpl;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path( "pentaho-cdf-dd/api/dashboards" )
public class DashboardsApi {

  private static final Log logger = LogFactory.getLog( DashboardsApi.class );
  private static final int INDENT = 2;

  public static final String PATH_SEPARATOR = "/";
  public static final String ENCODED_PATH_SEPARATOR = ":";

  @GET
  @Produces ( APPLICATION_JSON )
  public String getDashboardList( @QueryParam( "showHiddenFiles" ) @DefaultValue( "false" ) boolean showHiddenFiles ) {
    try {
      JSONArray dashboardArray = new DashboardsImpl().getDashboardList( -1, showHiddenFiles );
      if ( dashboardArray != null ) {
        return dashboardArray.toString( INDENT );
      }
      return new JSONArray().toString( INDENT );
    } catch ( JSONException jEx ) {
      logger.fatal( jEx );
    }
    return null;
  }

  @GET
  @Path ( "/{pathId : .+}" )
  @Produces ( APPLICATION_JSON )
  public String getDashboardById( @PathParam( "pathId" ) String pathId ) {
    try {
      String path = pathId.replace( ENCODED_PATH_SEPARATOR, PATH_SEPARATOR );
      JSONObject dashboard = new DashboardsImpl().getDashboardFromPath( path );
      if ( dashboard != null ) {
        return dashboard.toString( INDENT );
      }
      return new JSONObject().toString( INDENT );
    } catch ( JSONException jEx ) {
      logger.fatal( jEx );
    }
    return null;
  }
}
