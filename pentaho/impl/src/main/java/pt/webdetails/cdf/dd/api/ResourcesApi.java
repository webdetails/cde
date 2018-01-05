/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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
import static javax.ws.rs.core.MediaType.WILDCARD;
import static pt.webdetails.cpf.utils.MimeTypes.CSS;
import static pt.webdetails.cpf.utils.MimeTypes.JAVASCRIPT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.web.http.api.resources.PluginResource;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.GenericBasicFileFilter;
import pt.webdetails.cdf.dd.CdeSettings;
import pt.webdetails.cdf.dd.reader.factory.IResourceLoader;
import pt.webdetails.cdf.dd.reader.factory.ResourceLoaderFactory;
import pt.webdetails.cdf.dd.util.GenericFileAndDirectoryFilter;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.MimeTypeHandler;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.util.RepositoryHelper;
import pt.webdetails.cpf.utils.PluginIOUtils;

@Path( "pentaho-cdf-dd/api/resources" )
public class ResourcesApi {
  private static final Log logger = LogFactory.getLog( ResourcesApi.class );

  private static final List<String> allowedExtensions;

  static {
    String formats = PentahoSystem.get( IPluginResourceLoader.class, null ).getPluginSetting( ResourcesApi.class,
            CdeConstants.PLUGIN_SETTINGS_DOWNLOADABLE_FORMATS );
    allowedExtensions =  Arrays.asList( StringUtils.split( formats, ',' ) );
  }

  @GET
  @Path( "/get" )
  @Produces( TEXT_PLAIN )
  public void getResource( @QueryParam( "resource" ) @DefaultValue( "" ) String resource,
                           @Context HttpServletResponse response ) throws IOException {

    resource = XSSHelper.getInstance().escape( resource );

    try {
      String extension = resource.replaceAll( ".*\\.(.*)", "$1" );
      if ( allowedExtensions.indexOf( extension ) < 0 ) {
        // We can't provide this type of file
        logger.error( "Extension '" + extension + "' not whitelisted" );
        throw new SecurityException( "Not allowed" );
      }

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
        mimeType = MimeTypeHandler.getMimeTypeFromExtension( file.getExtension() );
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

      PluginIOUtils.writeOutAndFlush( response.getOutputStream(), file.getContents() );
    } catch ( SecurityException e ) {
      response.sendError( HttpServletResponse.SC_FORBIDDEN );
    }
  }

  @GET
  @Path( "/getCss" )
  @Produces( CSS )
  public void getCssResource( @QueryParam( "path" ) @DefaultValue( "" ) String path,
                              @QueryParam( "resource" ) @DefaultValue( "" ) String resource,
                              @Context HttpServletResponse response )
    throws IOException {

    path = XSSHelper.getInstance().escape( path );
    resource = XSSHelper.getInstance().escape( resource );

    getResource( resource, response );
  }

  @GET
  @Path( "/getJs" )
  @Produces( JAVASCRIPT )
  public void getJsResource( @QueryParam( "path" ) @DefaultValue( "" ) String path,
                             @QueryParam( "resource" ) @DefaultValue( "" ) String resource,
                             @Context HttpServletResponse response )
    throws IOException {

    path = XSSHelper.getInstance().escape( path );
    resource = XSSHelper.getInstance().escape( resource );

    getResource( resource, response );
  }

  @GET
  @Path( "/getUntyped" )
  @Produces( TEXT_PLAIN )
  public void getUntypedResource( @QueryParam( "path" ) @DefaultValue( "" ) String path,
                                  @QueryParam( "resource" ) @DefaultValue( "" ) String resource,
                                  @Context HttpServletResponse response )
    throws IOException {

    path = XSSHelper.getInstance().escape( path );
    resource = XSSHelper.getInstance().escape( resource );

    response.setHeader( "content-disposition", "inline" );

    getResource( resource, response );
  }

  @GET
  @Path( "/getImg" )
  @Produces( TEXT_PLAIN )
  public void getImage( @QueryParam( "path" ) @DefaultValue( "" ) String path,
                        @QueryParam( "resource" ) @DefaultValue( "" ) String resource,
                        @Context HttpServletResponse response )
    throws IOException {

    path = XSSHelper.getInstance().escape( path );
    resource = XSSHelper.getInstance().escape( resource );

    getResource( resource, response );
  }

  @GET
  @Path( "/res" )
  @Produces( TEXT_PLAIN )
  public void res( @QueryParam( "path" ) @DefaultValue( "" ) String path,
                   @QueryParam( "resource" ) @DefaultValue( "" ) String resource,
                   @Context HttpServletResponse response )
    throws Exception {

    path = XSSHelper.getInstance().escape( path );
    resource = XSSHelper.getInstance().escape( resource );

    getResource( resource, response );
  }

  @POST
  @Path( "/explore" )
  @Produces( TEXT_PLAIN )
  public String exploreFolder( @FormParam( "dir" ) @DefaultValue( "/" ) String folder,
                               @FormParam( "outputType" ) String outputType,
                               @QueryParam( "dashboardPath" ) @DefaultValue( "" ) String dashboardPath,
                               @QueryParam( "fileExtensions" ) String fileExtensions,
                               @QueryParam( "access" ) String access,
                               @QueryParam( "showHiddenFiles" ) @DefaultValue( "false" ) boolean showHiddenFiles )
    throws IOException {

    folder = XSSHelper.getInstance().escape( folder );
    outputType = XSSHelper.getInstance().escape( outputType );
    dashboardPath = XSSHelper.getInstance().escape( dashboardPath );
    fileExtensions = XSSHelper.getInstance().escape( fileExtensions );
    access = XSSHelper.getInstance().escape( access );

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
        // For some reason, in 4.5 file-based rep started to report a leading dot in extensions
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
    boolean isSystem = false;
    if ( !dashboardPath.isEmpty() ) {
      String path = dashboardPath.toLowerCase().replaceFirst( "/", "" );
      if ( path.startsWith( CdeEnvironment.getSystemDir() + "/" ) ) {
        isSystem = true;
      }
    }

    IReadAccess access = loader.getReader();

    GenericFileAndDirectoryFilter fileAndDirFilter = new GenericFileAndDirectoryFilter( fileFilter );

    if ( isSystem ) {
      // folder filtering ( see settings.xml ) will only occur for non-admin users
      if ( !isAdministrator() ) {
        fileAndDirFilter.setDirectories( CdeSettings.getFilePickerHiddenFolderPaths( CdeSettings.FolderType.STATIC ) );
        fileAndDirFilter.setFilterType( GenericFileAndDirectoryFilter.FilterType.FILTER_OUT ); // act as a black-list
      }
      fileList = access.listFiles( dir, fileAndDirFilter, 1, true, false );
      fileList.remove( 0 ); //remove the first because the root is being added
    } else {
      // folder filtering ( see settings.xml ) will only occur for non-admin users
      if ( !isAdministrator() ) {
        fileAndDirFilter.setDirectories( CdeSettings.getFilePickerHiddenFolderPaths( CdeSettings.FolderType.REPO ) );
        fileAndDirFilter.setFilterType( GenericFileAndDirectoryFilter.FilterType.FILTER_OUT ); // act as a black-list
      }
      fileList = access.listFiles( dir, fileAndDirFilter, 1, true, showHiddenFiles );
    }

    if ( fileList != null && fileList.size() > 0 ) {
      return fileList.toArray( new IBasicFile[ fileList.size() ] );
    }

    return new IBasicFile[] { };
  }

  @GET
  @Path( "/system/{path: [^?]+ }" )
  @Produces( { WILDCARD } )
  public Response getSystemResource( @PathParam( "path" ) String path, @Context HttpServletResponse response )
    throws IOException {

    path = XSSHelper.getInstance().escape( path );

    String extension = path.replaceAll( ".*\\.(.*)", "$1" );
    if ( allowedExtensions.indexOf( extension ) < 0 ) {
      // We can't provide this type of file
      logger.error( "Extension '" + extension + "' not whitelisted" );
      throw new SecurityException( "Not allowed" );
    }


    String[] splitPath = path.split( "/" );
    String pluginId = splitPath[ 0 ];
    String resource = "";
    for ( int i = 1; i < splitPath.length; i++ ) {
      resource += "/" + splitPath[ i ];
    }

    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );

    if ( !StringUtils.isEmpty( path ) && pluginManager.isPublic( pluginId, resource ) ) {

      Response readFileResponse = new PluginResource( response ).readFile( pluginId, resource );

      if ( readFileResponse.getStatus() != Status.NOT_FOUND.getStatusCode() ) {
        return readFileResponse;
      }
    }

    return Response.status( Status.NOT_FOUND ).build();
  }

  @GET
  @Path( "/{resource: [^?]+ }" )
  @Produces( { WILDCARD } )
  public void resource( @PathParam( "resource" ) String resource, @Context HttpServletResponse response )
    throws Exception {

    resource = XSSHelper.getInstance().escape( resource );

    getResource( resource, response );
  }

  /**
   * checks is the current user is administrator
   *
   * @return true if the current user is administrator, false otherwise
   */
  protected boolean isAdministrator() {
    return SecurityHelper.getInstance().isPentahoAdministrator( PentahoSessionHolder.getSession() );
  }


}
