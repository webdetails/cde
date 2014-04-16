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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import org.pentaho.platform.web.http.api.resources.PluginResource;

import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.GenericBasicFileFilter;
import pt.webdetails.cdf.dd.reader.factory.IResourceLoader;
import pt.webdetails.cdf.dd.reader.factory.ResourceLoaderFactory;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.util.RepositoryHelper;
import pt.webdetails.cpf.utils.MimeTypes;



@Path( "pentaho-cdf-dd/api/resources" )
public class ResourcesApi {
  private static final Log logger = LogFactory.getLog( ResourcesApi.class );

  @GET
  @Path( "/get" )
  @Produces( "text/plain" )
  public void getResource( @QueryParam( "resource" ) @DefaultValue( "" ) String resource,
                           @Context HttpServletResponse response ) throws IOException {
    try {
      IBasicFile file = Utils.getFileViaAppropriateReadAccess( resource );

      if ( file == null ) {
        logger.error( "resource not found:" + resource );
        response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        return;
      }

      IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class, null );
      String maxAge = resLoader.getPluginSetting( this.getClass(), "max-age" );

      String mimeType;
      try {
        final MimeTypes.FileType fileType = MimeTypes.FileType.valueOf( file.getExtension().toUpperCase() );
        mimeType = MimeTypes.getMimeType( fileType );
      } catch ( java.lang.IllegalArgumentException ex ) {
        mimeType = "";
      } catch ( EnumConstantNotPresentException ex ) {
        mimeType = "";
      }

      response.setHeader( "Content-Type", mimeType );
      response.setHeader( "content-disposition", "inline; filename=\"" + file.getName() + "\"" );

      if ( maxAge != null ) {
        response.setHeader( "Cache-Control", "max-age=" + maxAge );
      }

      byte[] contents = IOUtils.toByteArray( file.getContents() );

      IOUtils.write( contents, response.getOutputStream() );
      response.getOutputStream().flush();
    } catch ( SecurityException e ) {
      response.sendError( HttpServletResponse.SC_FORBIDDEN );
    }
  }

  @GET
  @Path( "/getCss" )
  @Produces( "text/css" )
  public void getCssResource( @QueryParam( "path" ) @DefaultValue( "" ) String path,
                              @QueryParam( "resource" ) @DefaultValue( "" ) String resource,
                              @Context HttpServletResponse response )
    throws IOException {
    getResource( resource, response );
  }

  @GET
  @Path( "/getJs" )
  @Produces( "text/javascript" )
  public void getJsResource( @QueryParam( "path" ) @DefaultValue( "" ) String path,
                             @QueryParam( "resource" ) @DefaultValue( "" ) String resource,
                             @Context HttpServletResponse response )
    throws IOException {
    getResource( resource, response );
  }

  @GET
  @Path( "/getUntyped" )
  @Produces( "text/plain" )
  public void getUntypedResource( @QueryParam( "path" ) @DefaultValue( "" ) String path,
                                  @QueryParam( "resource" ) @DefaultValue( "" ) String resource,
                                  @Context HttpServletResponse response )
    throws IOException {
    response.setHeader( "content-disposition", "inline" );

    getResource( resource, response );
  }

  @GET
  @Path( "/getImg" )
  @Produces( "text/plain" )
  public void getImage( @QueryParam( "path" ) @DefaultValue( "" ) String path,
                        @QueryParam( "resource" ) @DefaultValue( "" ) String resource,
                        @Context HttpServletResponse response )
    throws IOException {
    getResource( resource, response );
  }

  @GET
  @Path( "/res" )
  @Produces( "text/plain" )
  public void res( @QueryParam( "path" ) @DefaultValue( "" ) String path,
                   @QueryParam( "resource" ) @DefaultValue( "" ) String resource,
                   @Context HttpServletResponse response )
    throws Exception {
    getResource( resource, response );
  }

  @POST
  @Path( "/explore" )
  @Produces( "text/plain" )
  public String exploreFolder( @FormParam( "dir" ) @DefaultValue( "/" ) String folder,
                               @FormParam( "outputType" ) String outputType,
                               @QueryParam( "dashboardPath" ) @DefaultValue( "" ) String dashboardPath,
                               @QueryParam( "fileExtensions" ) String fileExtensions,
                               @QueryParam( "access" ) String access,
                               @QueryParam( "showHiddenFiles" ) @DefaultValue( "false" ) boolean showHiddenFiles )
    throws IOException {

    if ( !StringUtils.isEmpty( outputType ) && outputType.equals( "json" ) ) {
      try {
        return RepositoryHelper
          .toJSON( folder, getFileList( folder, dashboardPath, fileExtensions, access, showHiddenFiles ) );
      } catch ( JSONException e ) {
        logger.error( "exploreFolder" + folder, e );
        return "Error getting files in folder " + folder;
      }
    } else {
      return RepositoryHelper
        .toJQueryFileTree( folder, getFileList( folder, dashboardPath, fileExtensions, access, showHiddenFiles ) );
    }
  }

  private IBasicFile[] getFileList( String dir, String dashboardPath, final String fileExtensions, String permission,
                                    boolean showHiddenFiles ) {

    ArrayList<String> extensionsList = new ArrayList<String>();
    String[] extensions = StringUtils.split( fileExtensions, "." );
    IResourceLoader loader = ( new ResourceLoaderFactory() ).getResourceLoader( dashboardPath );

    if ( extensions != null ) {
      for ( String extension : extensions ) {
        // For some reason, in 4.5 filebased rep started to report a leading dot in extensions
        // Adding both just to be sure we don't break stuff
        extensionsList.add( "." + extension );
        extensionsList.add( extension );
      }
    }

    FileAccess fileAccess = FileAccess.parse( permission );
    if ( fileAccess == null ) {
      fileAccess = FileAccess.READ;
    }

    GenericBasicFileFilter fileFilter =
      new GenericBasicFileFilter( null, extensionsList.toArray( new String[ extensionsList.size() ] ), true );

    //check if it is a system dashboard
    List<IBasicFile> fileList;
    //IReadAccess access = CdeEnvironment.getUserContentAccess();
    boolean isSystem = false;
    if ( !dashboardPath.isEmpty() ) {
      String path = dashboardPath.toLowerCase().replaceFirst( "/", "" );
      if ( path.startsWith( CdeEnvironment.getSystemDir() + "/" ) ) {
        //access = Utils.getAppropriateReadAccess( dashboardPath );
        isSystem = true;
      }
    }

    IReadAccess access = loader.getReader();

    if ( isSystem ) {
      fileList = access.listFiles( dir, fileFilter, 1, true, false );
      fileList.remove( 0 ); //remove the first because the root is being added
    } else {
      fileList = access.listFiles( dir, fileFilter, IReadAccess.DEPTH_ZERO, true, showHiddenFiles );
    }


    if ( fileList != null && fileList.size() > 0 ) {
      return fileList.toArray( new IBasicFile[ fileList.size() ] );
    }

    return new IBasicFile[] { };
  }

  @GET
  @Path( "/system/{path: [^?]+ }" )
  @Produces( { MediaType.WILDCARD } )
  public Response getSystemResource( @PathParam( "path" ) String path, @Context HttpServletResponse response )
    throws IOException {

    String pluginId = CdeEngine.getInstance().getEnvironment().getPluginId();

    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );

    if ( !StringUtils.isEmpty( path ) && pluginManager.isPublic( pluginId, path ) ) {

      Response readFileResponse = new PluginResource( response ).readFile( pluginId, path );

      if ( readFileResponse.getStatus() != Status.NOT_FOUND.getStatusCode() ) {
        return readFileResponse;
      }
    }

    return Response.status( Status.NOT_FOUND ).build();
  }

  @GET
  @Path( "/{resource: [^?]+ }" )
  @Produces( { MediaType.WILDCARD } )
  public void resource( @PathParam( "resource" ) String resource, @Context HttpServletResponse response )
    throws Exception {
    getResource( resource, response );
  }

}
