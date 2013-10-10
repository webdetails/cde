package pt.webdetails.cdf.dd.api;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

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
import pt.webdetails.cpf.repository.api.FileAccess;

/**
 * Created with IntelliJ IDEA. User: diogomariano Date: 07/10/13
 */
@Path( "pentaho-cdf-dd/api/syncronizer" )
public class SyncronizerApi {

  private static final Log logger = LogFactory.getLog( SyncronizerApi.class );

  private static final String OPERATION_LOAD = "load";
  private static final String OPERATION_DELETE = "delete";
  private static final String OPERATION_SAVE = "save";
  private static final String OPERATION_SAVE_AS = "saveas";
  private static final String OPERATION_NEW_FILE = "newfile";
  private static final String OPERATION_SAVE_SETTINGS = "savesettings";


  @POST
  @Path( "/syncronizeDashboard" )
  public void syncronize( @FormParam( MethodParams.FILE ) @DefaultValue( "" ) String file,
		  @FormParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
		  @FormParam( MethodParams.OPERATION ) String operation, @Context HttpServletRequest request,
      @Context HttpServletResponse response ) throws Exception {

    String filePath = file.replaceAll( "cdfde", "wcdf" );

    if ( !filePath.isEmpty() && !CdeEnvironment.getUserContentAccess().hasAccess( filePath, FileAccess.EXECUTE ) ) {
      response.setStatus( HttpServletResponse.SC_FORBIDDEN );
      logger.warn( "Access denied for the syncronize method: " + path );
      return;
    }

    if ( filePath.isEmpty() ) {
      throw new Exception( Messages.getString( "SyncronizeCdfStructure.ERROR_002_INVALID_FILE_PARAMETER_EXCEPTION" ) );
    }

    try {
      final DashboardStructure dashboardStructure = new DashboardStructure();
      Object result = null;
      HashMap<String, Object> params = new HashMap<String, Object>( request.getParameterMap() );

      if ( OPERATION_LOAD.equalsIgnoreCase( operation ) ) {
        result = dashboardStructure.load( params );
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

      JsonUtils.buildJsonResult( response.getOutputStream(), true, result );
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

  @POST
  @Path( "/syncronizeTemplates" )
  public void
    syncTemplates( @FormParam( MethodParams.OPERATION ) String operation, @FormParam( MethodParams.FILE ) String file,
    		@FormParam( MethodParams.STRUCTURE ) String cdfStructure,
        @Context HttpServletResponse response ) throws IOException, DashboardStructureException {
    final CdfTemplates cdfTemplates = new CdfTemplates();

    if ( operation.equalsIgnoreCase( OPERATION_LOAD ) ) {
      Object result = cdfTemplates.load();
      JsonUtils.buildJsonResult( response.getOutputStream(), true, result );
    } else if ( operation.equalsIgnoreCase( OPERATION_SAVE ) ) {
      cdfTemplates.save( file, cdfStructure );
    }
  }

  @POST
  @Path( "/syncronizeStyles" )
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
