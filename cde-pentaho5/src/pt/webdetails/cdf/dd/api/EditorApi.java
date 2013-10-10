package pt.webdetails.cdf.dd.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.util.CdeEnvironment;
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
  //private static final String PLUGIN_PATH = CdeEnvironment.getSystemDir() + "/" + CdeEnvironment.getPluginId() + "/";

  @GET
  @Path( "/file/get" )
  @Produces( "text/plain" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public String getFile( @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path, @Context HttpServletResponse response )
    throws IOException {
    IUserContentAccess access = CdeEnvironment.getUserContentAccess();

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
  @Produces( "text/plain" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public boolean deleteFile( @FormParam( MethodParams.PATH ) @DefaultValue( "" ) String path ) {
    IUserContentAccess access = CdeEnvironment.getUserContentAccess();
    if ( access.hasAccess( path, FileAccess.DELETE ) && access.deleteFile( path ) ) {
      logger.debug( "File: " + path + " removed" );
      return true;
    } else {
      logger.debug( "File: " + path + "not removed" );
      return false;
    }

  }

  @POST
  @Path( "/file/write" )
  @Produces( "text/plain" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public boolean writeFile( @FormParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
      @FormParam( MethodParams.DATA ) @DefaultValue( "" ) String data ) throws IOException {
    IUserContentAccess access = CdeEnvironment.getUserContentAccess();
    if ( access.hasAccess( path, FileAccess.WRITE ) ) {
      if ( access.saveFile( path, new ByteArrayInputStream( data.getBytes( CharsetHelper.getEncoding() ) ) ) ) {
        logger.debug( "File: " + path + " written" );
        return true;
      } else {
        logger.error( "writeFile: failed saving " + path );
        return false;
      }
    } else {
      logger.error( "writeFile: no permissions to write file " + path );
      return false;
    }
  }

  @GET
  @Path( "/file/canEdit" )
  @Produces( "text/plain" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public String canEdit( @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path ) {
    return String.valueOf( CdeEnvironment.getUserContentAccess().hasAccess( path, FileAccess.WRITE ) );
  }

  @POST
  @Path( "/createFolder" )
  @Consumes( { APPLICATION_JSON } )
  public boolean createFolder( @FormParam( MethodParams.PATH ) @DefaultValue( "" ) String path ) {
    IUserContentAccess access = CdeEnvironment.getUserContentAccess();

    if ( access.fileExists( path ) ) {
      logger.debug( "Folder: " + path + " already exists" );
      return false;
    } else {
      if ( access.createFolder( path ) ) {
        logger.debug( "Folder: " + path + " created" );
        return true;
      } else {
        logger.debug( "Folder: " + path + " not created" );
        return false;
      }
    }
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
  @Produces( "text/plain" )
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
}
