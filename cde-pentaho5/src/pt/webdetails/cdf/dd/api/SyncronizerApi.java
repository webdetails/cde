package pt.webdetails.cdf.dd.api;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.DashboardDesignerException;
import pt.webdetails.cdf.dd.Messages;
import pt.webdetails.cdf.dd.cdf.CdfStyles;
import pt.webdetails.cdf.dd.cdf.CdfTemplates;
import pt.webdetails.cdf.dd.structure.DashboardStructure;
import pt.webdetails.cdf.dd.structure.DashboardStructureException;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.utils.MimeTypes;

/**
 * Created with IntelliJ IDEA. User: diogomariano Date: 07/10/13
 */
@Path( "pentaho-cdf-dd/api/syncronizer" )
public class SyncronizerApi {//TODO: synchronizer?

  private static final Log logger = LogFactory.getLog( SyncronizerApi.class );

  private static final String OPERATION_LOAD = "load";
  private static final String OPERATION_DELETE = "delete";
  private static final String OPERATION_SAVE = "save";
  private static final String OPERATION_SAVE_AS = "saveas";
  private static final String OPERATION_NEW_FILE = "newfile";
  private static final String OPERATION_SAVE_SETTINGS = "savesettings";
  /**
   * for historical reasons..
   */
  public static final String UNSAVED_FILE_PATH = "null/null/null";


  @POST
  @Path( "/syncronizeDashboard" )
  @Produces( MimeTypes.JSON )
  public String syncronize( @FormParam( MethodParams.FILE ) @DefaultValue( "" ) String file,
		  @FormParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
		  @FormParam( MethodParams.OPERATION ) String operation, @Context HttpServletRequest request,
      @Context HttpServletResponse response ) throws Exception {

    String filePath = file.replace( ".wcdf", ".cdfde" );

    if ( !filePath.isEmpty() && !filePath.equals( UNSAVED_FILE_PATH )
        && !CdeEnvironment.getUserContentAccess().hasAccess( filePath, FileAccess.EXECUTE ) ) {
      response.setStatus( HttpServletResponse.SC_FORBIDDEN );
      String msg = "Access denied for the syncronize method: " + path;
      logger.warn( msg );
      return JsonUtils.getJsonResult( false, msg ); 
    }

    try {
      final DashboardStructure dashboardStructure = new DashboardStructure();
      Object result = null;
      HashMap<String, Object> params = new HashMap<String, Object>( request.getParameterMap() );
      OutputStream out = response.getOutputStream();
      if ( OPERATION_LOAD.equalsIgnoreCase( operation ) ) {
        return dashboardStructure.load( file );
      } else if ( OPERATION_DELETE.equalsIgnoreCase( operation ) ) {
        dashboardStructure.delete( params );
      } else if ( OPERATION_SAVE.equalsIgnoreCase( operation ) ) {
        result = dashboardStructure.save( params );
      } else if ( OPERATION_SAVE_AS.equalsIgnoreCase( operation ) ) {
        dashboardStructure.saveas( params );
      } else if ( OPERATION_NEW_FILE.equalsIgnoreCase( operation ) ) {
        dashboardStructure.newfile( params );
      } else if ( OPERATION_SAVE_SETTINGS.equalsIgnoreCase( operation ) ) {
        dashboardStructure.savesettings( params );
      } else {
        logger.error( "Unknown operation: " + operation );
      }
      return JsonUtils.getJsonResult( true, result );
    } catch ( Exception e ) {
      if ( e.getCause() != null ) {
        if ( e.getCause() instanceof DashboardStructureException ) {
          JsonUtils.buildJsonResult( response.getOutputStream(), false, e.getCause().getMessage() );
        } else if ( e instanceof InvocationTargetException ) {
          throw (Exception) e.getCause();
        }
      }
      throw e;
    }
  }

  @GET
  @Path( "/syncronizeTemplates" )
  @Produces( MimeTypes.JSON )
  public void
    syncTemplates( @QueryParam( MethodParams.OPERATION ) String operation, @QueryParam( MethodParams.FILE ) String file,
    		@QueryParam( MethodParams.STRUCTURE ) String cdfStructure,
        @Context HttpServletResponse response ) throws IOException, DashboardStructureException {
    final CdfTemplates cdfTemplates = new CdfTemplates();

    if ( operation.equalsIgnoreCase( OPERATION_LOAD ) ) {
      Object result = cdfTemplates.load();
      JsonUtils.buildJsonResult( response.getOutputStream(), true, result );
    } else if ( operation.equalsIgnoreCase( OPERATION_SAVE ) ) {
      cdfTemplates.save( file, cdfStructure );
    }
  }

  @GET
  @Path( "/syncronizeStyles" )
  @Produces( MimeTypes.JSON )
  public void syncStyles( @Context HttpServletResponse response ) throws IOException, DashboardDesignerException {
    final CdfStyles cdfStyles = new CdfStyles();
    JsonUtils.buildJsonResult( response.getOutputStream(), true, cdfStyles.liststyles() );
  }

  private class MethodParams {
    private static final String FILE = "file";
    private static final String PATH = "path";
    private static final String OPERATION = "operation";
    private static final String STRUCTURE = "structure";
  }
}
