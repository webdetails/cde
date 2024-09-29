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


package pt.webdetails.cdf.dd.api;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import pt.webdetails.cdf.dd.CdeSettings;
import pt.webdetails.cdf.dd.CdeVersionChecker;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cpf.VersionChecker;
import pt.webdetails.cpf.VersionChecker.CheckVersionResponse;
import pt.webdetails.cpf.annotations.AccessLevel;
import pt.webdetails.cpf.annotations.Exposed;

@Path( "pentaho-cdf-dd/api/version" )
public class VersionApi {

  @GET
  @Path( "/check" )
  @Produces( TEXT_PLAIN )
  public Response checkVersion() throws JSONException {
    VersionChecker versionChecker = new CdeVersionChecker( CdeSettings.getSettings() );
    CheckVersionResponse result = versionChecker.checkVersion();

    return Response.ok( JsonUtils.getJsonResult( result != null, result ) ).build();
  }

  @GET
  @Path( "/get" )
  @Produces( TEXT_PLAIN )
  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public String getVersion() {
    VersionChecker versionChecker = new CdeVersionChecker( CdeSettings.getSettings() );
    return versionChecker.getVersion();
  }
}
