/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package org.pentaho.ctools.cde.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.pentaho.ctools.cde.impl.DashboardsImpl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path( "pentaho-cdf-dd/api/dashboards" )
public class DashboardsApi {

  private static final Log logger = LogFactory.getLog( DashboardsApi.class );
  private static final int INDENT = 2;

  @GET
  @Path ( "/list" )
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
}
