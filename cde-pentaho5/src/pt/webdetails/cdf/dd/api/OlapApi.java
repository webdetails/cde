/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cdf.dd.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cpf.olap.OlapUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.io.IOException;

import net.sf.json.JSONObject;
import org.json.JSONException;

@Path( "pentaho-cdf-dd/api/olap" )
public class OlapApi {
  private static final Log logger = LogFactory.getLog( SyncronizerApi.class );

  @GET
  @Path( "/getCubes" )
  @Produces( "text/javascript" )
  public void getCubes( @QueryParam( MethodParams.CATALOG ) String catalog, @Context HttpServletRequest request,
                        @Context HttpServletResponse response ) throws IOException, JSONException {
    OlapUtils olapUtils = new OlapUtils();
    JSONObject result = olapUtils.getOlapCubes();
    JsonUtils.buildJsonResult( response.getOutputStream(), result != null, result );
  }

  @GET
  @Path( "/getCubeStructure" )
  @Produces( "text/javascript" )
  public void getCubeStructure( @QueryParam( MethodParams.CATALOG ) String catalog,
                                @QueryParam( MethodParams.CUBE ) String cube,
                                @QueryParam( MethodParams.JNDI ) String jndi,
                                @Context HttpServletResponse response ) throws IOException, JSONException {
    OlapUtils olapUtils = new OlapUtils();
    JSONObject result = olapUtils.getCubeStructure( catalog, cube, jndi );
    JsonUtils.buildJsonResult( response.getOutputStream(), result != null, result );
  }

  @GET
  @Path( "/getLevelMembersStructure" )
  @Produces( "text/javascript" )
  public void getLevelMembersStructure( @QueryParam( MethodParams.CATALOG ) String catalog,
                                        @QueryParam( MethodParams.CUBE ) String cube,
                                        @QueryParam( MethodParams.MEMBER ) String member,
                                        @QueryParam( MethodParams.DIRECTION ) String direction,
                                        @Context HttpServletResponse response )
    throws IOException, JSONException {
    OlapUtils olapUtils = new OlapUtils();
    JSONObject result = olapUtils.getLevelMembersStructure( catalog, cube, member, direction );
    JsonUtils.buildJsonResult( response.getOutputStream(), result != null, result );
  }

  @GET
  @Path( "/getPaginatedLevelMembers" )
  @Produces( "text/javascript" )
  public void getPaginatedLevelMembers( @QueryParam( MethodParams.CATALOG ) String catalog,
                                        @QueryParam( MethodParams.CUBE ) String cube,
                                        @QueryParam( MethodParams.LEVEL ) String level,
                                        @QueryParam( MethodParams.START_MEMBER ) String startMember,
                                        @QueryParam( MethodParams.CONTEXT ) String context,
                                        @QueryParam( MethodParams.SEARCH_TERM ) String searchTerm,
                                        @QueryParam( MethodParams.PAGE_SIZE ) long pageSize,
                                        @QueryParam( MethodParams.PAGE_START ) long pageStart,

                                        @Context HttpServletResponse response ) throws IOException, JSONException {
    OlapUtils olapUtils = new OlapUtils();
    JSONObject result =
      olapUtils
        .getPaginatedLevelMembers( catalog, cube, level, startMember, context, searchTerm, pageSize, pageStart );
    JsonUtils.buildJsonResult( response.getOutputStream(), result != null, result );
  }

  private class MethodParams {
    public static final String CATALOG = "catalog";
    public static final String CUBE = "cube";
    public static final String LEVEL = "level";
    public static final String JNDI = "jndi";
    public static final String MEMBER = "member";
    public static final String START_MEMBER = "startMember";
    public static final String DIRECTION = "direction";
    public static final String CONTEXT = "context";
    public static final String SEARCH_TERM = "searchTerm";
    public static final String PAGE_SIZE = "pageSize";
    public static final String PAGE_START = "pageStart";
  }
}
