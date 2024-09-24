/*!
 * Copyright 2002 - 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
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
