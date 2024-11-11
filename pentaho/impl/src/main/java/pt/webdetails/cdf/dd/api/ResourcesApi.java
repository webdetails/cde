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


package pt.webdetails.cdf.dd.api;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static pt.webdetails.cpf.utils.MimeTypes.CSS;
import static pt.webdetails.cpf.utils.MimeTypes.JAVASCRIPT;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
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
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

@Path( "pentaho-cdf-dd/api/resources" )
public class ResourcesApi {
  private static final Log logger = LogFactory.getLog( ResourcesApi.class );

  private static List<String> allowedExtensions = new ArrayList<>();

  static {
    IPluginResourceLoader resourceLoader = PentahoSystem.get( IPluginResourceLoader.class, null );
    //Added to check resourceLoader for null values
    if ( resourceLoader != null ) {
      String formats = resourceLoader
        .getPluginSetting( ResourcesApi.class, CdeConstants.PLUGIN_SETTINGS_DOWNLOADABLE_FORMATS );
      allowedExtensions =  Arrays.asList( StringUtils.split( formats, ',' ) );
    }

  }
  //Added to be able to set allowed extensions for testing
  public static void setAllowedExtensions( List<String> stringList ){
    allowedExtensions = stringList;
  }


  @GET
  @Path( "/get" )
  @Produces( TEXT_PLAIN )
  public Response getResource( @QueryParam( "resource" ) @DefaultValue( "" ) String resource,
                               @HeaderParam( "if-none-match" ) String ifNoneMatch ) throws IOException {

    resource = decodeAndEscape( resource );

    try {
      checkExtensions( resource );

      IBasicFile file = Utils.getFileViaAppropriateReadAccess( resource );

      if ( file == null ) {
        logger.error( "resource not found:" + resource );
        return Response.status( Status.INTERNAL_SERVER_ERROR ).build();
      }

      boolean modified = wasModified( ifNoneMatch, resource );

      if ( !modified ) {
        return buildNotModifiedResponse( resource );
      }

      String mimeType = getMimeType( file.getExtension() );

      StreamingOutput streamingOutput = output -> IOUtils.copy( file.getContents(), output );

      Response.ResponseBuilder responseBuilder = cacheControlHandler( file, streamingOutput, resource );

      responseBuilder.header( "Content-Type", mimeType );
      responseBuilder.header( "content-disposition", "inline; filename=\"" + file.getName() + "\"" );

      return responseBuilder.build();

    } catch ( SecurityException e ) {
      return Response.status( Status.FORBIDDEN ).build();
    }
  }

  @GET
  @Path( "/getCss" )
  @Produces( CSS )
  public Response getCssResource( @QueryParam( "path" ) @DefaultValue( "" ) String path,
                                  @QueryParam( "resource" ) @DefaultValue( "" ) String resource,
                                  @HeaderParam( "if-none-match" ) String ifNoneMatch )
    throws IOException {

    return getResource( resource, ifNoneMatch );
  }

  @GET
  @Path( "/getJs" )
  @Produces ( JAVASCRIPT )
  public Response getJsResource( @QueryParam( "path" ) @DefaultValue( "" ) String path,
                             @QueryParam( "resource" ) @DefaultValue( "" ) String resource )
    throws IOException {

    return getResource( resource, null );
  }

  @GET
  @Path( "/getUntyped" )
  @Produces( TEXT_PLAIN )
  public Response getUntypedResource( @QueryParam( "path" ) @DefaultValue( "" ) String path,
                                  @QueryParam( "resource" ) @DefaultValue( "" ) String resource )
    throws IOException {

    return getResource( resource, null );
  }

  @GET
  @Path( "/getImg" )
  @Produces( TEXT_PLAIN )
  public Response getImage( @QueryParam( "path" ) @DefaultValue( "" ) String path,
                        @QueryParam( "resource" ) @DefaultValue( "" ) String resource )
    throws IOException {

    return getResource( resource, null );
  }

  @GET
  @Path( "/res" )
  @Produces( TEXT_PLAIN )
  public Response res( @QueryParam( "path" ) @DefaultValue( "" ) String path,
                   @QueryParam( "resource" ) @DefaultValue( "" ) String resource )
    throws IOException {

    return getResource( resource, null );
  }

  @POST
  @Path( "/explore" )
  @Produces( TEXT_PLAIN )
  public String exploreFolder( @FormParam( "dir" ) @DefaultValue( "/" ) String folder,
                               @FormParam( "outputType" ) String outputType,
                               @QueryParam( "dashboardPath" ) @DefaultValue( "" ) String dashboardPath,
                               @QueryParam( "fileExtensions" ) String fileExtensions,
                               @QueryParam( "access" ) String access,
                               @QueryParam( "showHiddenFiles" ) @DefaultValue( "false" ) boolean showHiddenFiles ) {

    folder = decodeAndEscape( folder );
    outputType = decodeAndEscape( outputType );
    dashboardPath = decodeAndEscape( dashboardPath );
    fileExtensions = decodeAndEscape( fileExtensions );

    if ( !StringUtils.isEmpty( outputType ) && outputType.equals( "json" ) ) {
      try {
        IBasicFile[] files = getFileList( folder, dashboardPath, fileExtensions, showHiddenFiles );
        return RepositoryHelper.toJSON( folder, files );
      } catch ( JSONException e ) {
        logger.error( "exploreFolder" + folder, e );
        return "Error getting files in folder " + folder;
      }
    } else {
      IBasicFile[] files = getFileList( folder, dashboardPath, fileExtensions, showHiddenFiles );
      return RepositoryHelper.toJQueryFileTree( folder, files );
    }
  }

  private IBasicFile[] getFileList( String dir, String dashboardPath, final String fileExtensions,
                                    boolean showHiddenFiles ) {

    ArrayList<String> extensionsList = new ArrayList<>();
    String[] extensions = StringUtils.split( fileExtensions, "." );
    if ( extensions != null ) {
      for ( String extension : extensions ) {
        // For some reason, in 4.5 file-based rep started to report a leading dot in extensions
        // Adding both just to be sure we don't break stuff
        extensionsList.add( "." + extension );
        extensionsList.add( extension );
      }
    }

    GenericBasicFileFilter fileFilter = new GenericBasicFileFilter(
      null, extensionsList.toArray( new String[ 0 ] ), true );

    //check if it is a system dashboard
    List<IBasicFile> fileList;
    boolean isSystem = false;
    if ( !dashboardPath.isEmpty() ) {
      String path = dashboardPath.toLowerCase().replaceFirst( "/", "" );
      if ( path.startsWith( CdeEnvironment.getSystemDir() + "/" ) ) {
        isSystem = true;
      }
    }

    IResourceLoader loader = getResourceLoader( dashboardPath );
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

    if ( fileList != null && !fileList.isEmpty() ) {
      return fileList.toArray( new IBasicFile[ 0 ] );
    }

    return new IBasicFile[] { };
  }

  @GET
  @Path( "/system/{path: [^?]+ }" )
  @Produces( {  } )
  public Response getSystemResource( @PathParam( "path" ) String path, @Context HttpServletResponse response )
    throws IOException {

    path = decodeAndEscape( path );

    checkExtensions( path );

    String[] splitPath = path.split( "/" );
    String pluginId = splitPath[ 0 ];

    StringBuilder resource = new StringBuilder();
    for ( int i = 1; i < splitPath.length; i++ ) {
      resource.append( "/" ).append( splitPath[i] );
    }

    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );

    if ( !StringUtils.isEmpty( path ) && pluginManager.isPublic( pluginId, resource.toString() ) ) {
      Response readFileResponse = new PluginResource( response ).readFile( pluginId, resource.toString() );

      if ( readFileResponse.getStatus() != Status.NOT_FOUND.getStatusCode() ) {
        return readFileResponse;
      }
    }

    return Response.status( Status.NOT_FOUND ).build();
  }

  @GET
  @Path( "/{resource: [^?]+ }" )
  @Produces( {  } )
  public Response resource( @PathParam( "resource" ) String resource,
                            @HeaderParam( "if-none-match" ) String ifNoneMatch )
    throws IOException {

    IBasicFile resourceFile = Utils.getFileViaAppropriateReadAccess( resource );
    ifNoneMatch = resourceFile != null && "css".equals( resourceFile.getExtension() ) ? ifNoneMatch : null;

    return getResource( resource, ifNoneMatch );
  }

  /**
   * checks is the current user is administrator
   *
   * @return true if the current user is administrator, false otherwise
   */
  protected boolean isAdministrator() {
    return SecurityHelper.getInstance().isPentahoAdministrator( PentahoSessionHolder.getSession() );
  }

  private IResourceLoader getResourceLoader( String path ) {
    return new ResourceLoaderFactory().getResourceLoader( path );
  }

  @VisibleForTesting
  String decodeAndEscape( String path ) {
    final XSSHelper helper = XSSHelper.getInstance();

    return helper.escape( Utils.getURLDecoded( path ) );
  }

  /**
   * Checks it the resource extensions are allowed. If not logs the information and throws an exception.
   *
   * @param decodedPath - Path already decoded and escaped.
   */
  private void checkExtensions( String decodedPath ) {
    String extension = decodedPath.replaceAll( ".*\\.(.*)", "$1" );
    if ( allowedExtensions.indexOf( extension ) < 0 ) {
      // We can't provide this type of file
      logger.error( "Extension '" + extension + "' not whitelisted" );
      throw new SecurityException( "Not allowed" );
    }
  }

  /**
   * Returns the mime type from the received file extension.
   *
   * @param fileExtension - File extension.
   * @return - The mime type.
   */
  private String getMimeType( String fileExtension ) {
    try {
      return MimeTypeHandler.getMimeTypeFromExtension( fileExtension );
    } catch ( IllegalArgumentException | EnumConstantNotPresentException ex ) {
      return "";
    }
  }

  private Response.ResponseBuilder cacheControlHandler( IBasicFile file, StreamingOutput streamingOutput, String resource ) {
    Response.ResponseBuilder responseBuilder = Response.ok( streamingOutput );

    if ( file.getExtension().equals( "css" ) ) {
      long lastModifiedTime = getLastModifiedTime( resource );
      //forces the first time to check the resource freshness adding also Etag tag
      responseBuilder.header( "Cache-Control", "max-age=0" );
      responseBuilder.header( "Etag", lastModifiedTime );
    } else {
      //Added to check PentahoSystem object for null values
      IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class, null );
      if (resLoader != null) {
        String maxAge = resLoader.getPluginSetting( this.getClass(), "max-age" );
        if ( maxAge != null ) {
          responseBuilder.header( "Cache-Control", "max-age=" + maxAge );
        }
      }
    }

    return responseBuilder;
  }

  private Response buildNotModifiedResponse( String resource ) {
    final long lastModifiedTime = getLastModifiedTime( resource );

    Response.ResponseBuilder responseBuilder = Response.notModified();
    responseBuilder.header( "Etag", lastModifiedTime );

    return responseBuilder.build();
  }

  @VisibleForTesting
  long getLastModifiedTime( String resource ) {
    IReadAccess ra = getResourceLoader( "" ).getReader();

    return ra.getLastModified( resource );
  }

  private boolean wasModified( String ifNoneMatch, String resource ) {
    String lastModifiedTime = Long.toString( getLastModifiedTime( resource ) );

    if ( ifNoneMatch == null ) {
      return true;
    }

    if ( lastModifiedTime.equals( ifNoneMatch ) ) {
      return false;
    }

    return true;
  }

}
