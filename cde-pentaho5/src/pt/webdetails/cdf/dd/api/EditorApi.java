package pt.webdetails.cdf.dd.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.WILDCARD;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cpf.utils.CharsetHelper;

/**
 * Created with IntelliJ IDEA. User: diogomariano Date: 04/10/13
 */

@Path( "pentaho-cdf-dd/api/editor" )
public class EditorApi {
  private static final int NO_CACHE_DURATION = 0;
  private static final Log logger = LogFactory.getLog( EditorApi.class );
  private static final String EXTERNAL_EDITOR_PAGE = "resources/ext-editor.html";
  private static final String COMPONENT_EDITOR_PAGE = "resources/cdf-dd-component-editor.html";

  @GET
  @Path( "/file/get" )
  @Produces( "text/plain" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public String getFile( @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path, @Context HttpServletResponse response )
    throws IOException {
    IUserContentAccess access = getUserContentAccess();

    if ( access.fileExists( path ) ) {
      response.setHeader( "Cache-Control", "max-age=" + NO_CACHE_DURATION );
      return IOUtils.toString( access.getFileInputStream( path ) );
    } else {
      String msg = "File: " + path + "does not exist, or you do not have permissions to access it";
      logger.error( msg );
      return msg;
    }
  }

  @POST
  @Path( "/file/delete" )
  @Produces( "text/javascript" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public void deleteFile( @FormParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
                             @Context HttpServletResponse response) throws IOException {
    IUserContentAccess access = getUserContentAccess();
    if ( access.hasAccess( path, FileAccess.DELETE ) && access.deleteFile( path ) ) {
      logger.debug( "File: " + path + " removed" );
      JsonUtils.buildJsonResult( response.getOutputStream(), true, null );
    } else {
      logger.debug( "File: " + path + "not removed" );
      JsonUtils.buildJsonResult( response.getOutputStream(), false, null );
    }
  }

  @POST
  @Path( "/file/write" )
  @Produces( "text/plain" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED } )
  public String writeFile( @FormParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
      @FormParam( MethodParams.DATA ) @DefaultValue( "" ) String data,
      @Context HttpServletResponse response) throws IOException {
    IUserContentAccess access = getUserContentAccess();

    String msg = "";
    if ( access.hasAccess( path, FileAccess.WRITE ) ) {
      if ( access.saveFile( path, new ByteArrayInputStream( data.getBytes( CharsetHelper.getEncoding() ) ) ) ) {
        msg = "file '" + path + "' saved ok";
        logger.debug( msg );
      } else {
        msg = "error saving file " + path;
        logger.error( msg );
      }
    } else {
      msg = "no permissions to write file " + path;
      logger.error( msg );
    }

    return msg;
  }

  @PUT
  @Path( "/file/write" )
  @Produces( "text/plain" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED } )
  public String createFile( @FormParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
      @FormParam( MethodParams.DATA ) @DefaultValue( "" ) String data,
      @Context HttpServletResponse response) throws IOException {
    IUserContentAccess access = getUserContentAccess();

    String msg = "";
    if ( access.hasAccess( FilenameUtils.getFullPath( path ), FileAccess.WRITE ) ) {
      if ( access.saveFile( path, new ByteArrayInputStream( data.getBytes( CharsetHelper.getEncoding() ) ) ) ) {
        msg = "file '" + path + "' saved ok";
        logger.debug( msg );
      } else {
        msg = "error saving file " + path;
        logger.error( msg );
      }
    } else {
      msg = "no permissions to write file " + path;
      logger.error( msg );
    }
    return msg;
  }



  @GET
  @Path( "/file/canEdit" )
  @Produces( "text/plain" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public String canEdit( @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path ) {
    return String.valueOf( getUserContentAccess().hasAccess( path, FileAccess.WRITE ) );
  }

  @POST
  @Path( "/createFolder" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public String createFolder( @FormParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
                            @Context HttpServletResponse response) throws IOException {
    IUserContentAccess access = getUserContentAccess();

    String msg;
    if ( access.fileExists( path ) ) {
      msg = "already exists: " + path;
      logger.debug( msg );
    } else {
      if ( access.createFolder( path ) ) {
        msg = path + "created ok";
        logger.debug( msg );
      } else {
        msg = "error creating folder " + path;
        logger.debug( msg );
      }
    }
    return msg;
  }


  @GET
  @Path( "/getExternalEditor" )
  @Produces( "text/html" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public String externalEditor() throws IOException {
    IReadAccess access = CdeEnvironment.getPluginSystemReader();

    if ( access.fileExists( EXTERNAL_EDITOR_PAGE ) ) {
      return IOUtils.toString( access.getFileInputStream( EXTERNAL_EDITOR_PAGE ) );
    } else {
      String msg = "External editor not found: " +  EXTERNAL_EDITOR_PAGE;
      logger.error( msg );
      return msg;
    }

  }

  @GET
  @Path( "/getComponentEditor" )
  @Produces( "text/html" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public String componentEditor() throws IOException {
    IReadAccess access = CdeEnvironment.getPluginSystemReader();
    if ( access.fileExists( COMPONENT_EDITOR_PAGE ) ) {
      return IOUtils.toString( access.getFileInputStream( COMPONENT_EDITOR_PAGE ) );
    } else {
      String msg = "no external editor found: " + COMPONENT_EDITOR_PAGE;
      logger.error( msg );
      return msg;
    }
  }

  private class MethodParams {
    public static final String PATH = "path";
    public static final String DATA = "data";
  }
  protected IUserContentAccess getUserContentAccess(){
    return CdeEnvironment.getUserContentAccess();
  }
}
