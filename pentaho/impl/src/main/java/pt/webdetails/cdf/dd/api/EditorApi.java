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

package pt.webdetails.cdf.dd.api;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static pt.webdetails.cpf.utils.MimeTypes.JAVASCRIPT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import pt.webdetails.cdf.dd.reader.factory.IResourceLoader;
import pt.webdetails.cdf.dd.reader.factory.ResourceLoaderFactory;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IACAccess;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.CharsetHelper;

@Path( "pentaho-cdf-dd/api/editor" )
public class EditorApi {
  private static final int NO_CACHE_DURATION = 0;
  private static final Log logger = LogFactory.getLog( EditorApi.class );
  private static final String EXTERNAL_EDITOR_PAGE = "resources/ext-editor.html";
  private static final String COMPONENT_EDITOR_PAGE = "resources/cdf-dd-component-editor.html";

  @GET
  @Path( "/file/get" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  @Produces( TEXT_PLAIN )
  public String getFile( @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
                         @Context HttpServletResponse response )
    throws IOException {

    path = decodeAndEscape( path );

    final IResourceLoader loader = getResourceLoader( path );
    final IACAccess contentAccess = loader.getAccessControl();
    final IReadAccess reader = loader.getReader();

    final boolean canGetFile = reader.fileExists( path ) && contentAccess.hasAccess( path, FileAccess.READ );
    if ( !canGetFile ) {
      String errorMessage = "File: " + path + " does not exist, or you do not have permissions to access it";

      logger.error( errorMessage );
      return errorMessage;
    }

    response.setHeader( "Cache-Control", "max-age=" + NO_CACHE_DURATION );
    return IOUtils.toString( reader.getFileInputStream( path ) );
  }

  @POST
  @Path( "/file/delete" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  @Produces( JAVASCRIPT )
  public void deleteFile( @FormParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
                          @Context HttpServletResponse response ) throws IOException, JSONException {

    path = decodeAndEscape( path );

    final IResourceLoader loader = getResourceLoader( path );
    final IACAccess access = loader.getAccessControl();
    final IRWAccess writer = loader.getWriter();

    final boolean fileDeleted = access.hasAccess( path, FileAccess.DELETE ) && writer.deleteFile( path );

    final String message = "File: " + path + ( fileDeleted ? "" : "not" ) + " removed";

    logger.debug( message );
    JsonUtils.buildJsonResult( response.getOutputStream(), fileDeleted, null );
  }

  @POST
  @Path( "/file/write" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED } )
  @Produces( TEXT_PLAIN )
  public String writeFile( @FormParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
                           @FormParam( MethodParams.DATA ) @DefaultValue( "" ) String data,
                           @Context HttpServletResponse response ) throws IOException {

    path = decodeAndEscape( path );

    final IResourceLoader loader = getResourceLoader( path );

    return writeFile( path, loader, data );
  }



  @PUT
  @Path( "/file/write" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON, APPLICATION_FORM_URLENCODED } )
  @Produces( TEXT_PLAIN )
  public String createFile( @FormParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
                            @FormParam( MethodParams.DATA ) @DefaultValue( "" ) String data,
                            @Context HttpServletResponse response ) throws IOException {

    path = decodeAndEscape( path );

    final IResourceLoader loader = getResourceLoader( path );

    return writeFile( path, FilenameUtils.getFullPath( path ), loader, data );
  }


  @GET
  @Path( "/file/canEdit" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  @Produces( TEXT_PLAIN )
  public String canEdit( @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path ) {

    path = decodeAndEscape( path );

    final IResourceLoader loader = getResourceLoader( path );
    final IACAccess contentAccess = loader.getAccessControl();

    return String.valueOf( contentAccess.hasAccess( path, FileAccess.WRITE ) );
  }

  @POST
  @Path( "/createFolder" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public String createFolder( @FormParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
                              @Context HttpServletResponse response ) {

    path = decodeAndEscape( path );

    final IResourceLoader loader = getResourceLoader( path );
    final IACAccess access = loader.getAccessControl();

    String message;
    if ( access.hasAccess( path, FileAccess.WRITE ) ) {
      final IReadAccess reader = loader.getReader();

      if ( reader.fileExists( path ) ) {
        message = "already exists: " + path;

        logger.debug( message );
      } else {
        final IRWAccess writer = loader.getWriter();

        if ( writer.createFolder( path ) ) {
          message = path + "created ok";
          logger.debug( message );
        } else {
          message = "error creating folder " + path;
          logger.debug( message );
        }
      }

    } else {
      message = "no permissions to create folder " + path;
      logger.error( message );
    }


    return message;
  }


  @GET
  @Path( "/getExternalEditor" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  @Produces( TEXT_HTML )
  public String externalEditor() throws IOException {
    final IReadAccess access = CdeEnvironment.getPluginSystemReader();

    final boolean externalEditorExists = access.fileExists( EXTERNAL_EDITOR_PAGE );
    if ( !externalEditorExists ) {
      final String errorMessage = "External editor not found: " + EXTERNAL_EDITOR_PAGE;

      logger.error( errorMessage );
      return errorMessage;
    }

    return IOUtils.toString( access.getFileInputStream( EXTERNAL_EDITOR_PAGE ) );
  }

  @GET
  @Path( "/getComponentEditor" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  @Produces( TEXT_HTML )
  public String componentEditor() throws IOException {
    final IReadAccess access = CdeEnvironment.getPluginSystemReader();

    final boolean componentEditorExists = access.fileExists( COMPONENT_EDITOR_PAGE );
    if ( !componentEditorExists ) {
      final String errorMessage = "no external editor found: " + COMPONENT_EDITOR_PAGE;

      logger.error( errorMessage );
      return errorMessage;
    }

    return IOUtils.toString( access.getFileInputStream( COMPONENT_EDITOR_PAGE ) );
  }

  private class MethodParams {
    public static final String PATH = "path";
    public static final String DATA = "data";
  }

  // region private/package aux methods
  protected IResourceLoader getResourceLoader( String path ) {
    return new ResourceLoaderFactory().getResourceLoader( path );
  }

  private String writeFile( String path, IResourceLoader loader, String data ) throws IOException {
    return writeFile( path, path, loader, data );
  }

  private String writeFile( String path, String fullPath, IResourceLoader loader, String data ) throws IOException {
    final IACAccess access = loader.getAccessControl();

    String message;
    if ( access.hasAccess( fullPath, FileAccess.WRITE ) ) {
      final IRWAccess writer = loader.getWriter();

      InputStream content = new ByteArrayInputStream( data.getBytes( CharsetHelper.getEncoding() ) );
      if ( writer.saveFile( path, content ) ) {
        message = "file '" + path + "' saved ok";
        logger.debug( message );
      } else {
        message = "error saving file " + path;
        logger.error( message );
      }

    } else {
      message = "no permissions to write file " + path;
      logger.error( message );
    }

    return message;
  }

  private String decodeAndEscape( String path ) {
    final XSSHelper helper = XSSHelper.getInstance();

    return helper.escape( Utils.getURLDecoded( path ) );
  }
  // endregion

}
