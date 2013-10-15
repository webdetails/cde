package pt.webdetails.cdf.dd.api;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.DashboardDesignerException;
import pt.webdetails.cdf.dd.cdf.CdfStyles;
import pt.webdetails.cdf.dd.cdf.CdfTemplates;
import pt.webdetails.cdf.dd.structure.DashboardStructure;
import pt.webdetails.cdf.dd.structure.DashboardStructureException;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cdf.dd.util.Utils;
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
		  					@FormParam( MethodParams.TITLE ) @DefaultValue( "" ) String title,
                @FormParam( MethodParams.AUTHOR ) @DefaultValue( "" ) String author,
		  					@FormParam( MethodParams.DESCRIPTION ) @DefaultValue( "" )  String description,
                @FormParam( MethodParams.STYLE ) @DefaultValue( "" ) String style,
                @FormParam( MethodParams.WIDGET_NAME ) @DefaultValue( "" ) String widgetName,
                @FormParam( MethodParams.WIDGET ) boolean widget,
                @FormParam( MethodParams.RENDER_TYPE ) @DefaultValue( "" ) String renderType,
                @FormParam( MethodParams.WIDGET_PARAMETERS ) List<String> widgetParams,
		  					@FormParam( MethodParams.DASHBOARD_STRUCTURE )  String cdfStructure,
		  					@FormParam( MethodParams.OPERATION ) String operation,

		  					@Context HttpServletRequest request,
		  					@Context HttpServletResponse response ) throws Exception {
    
    if ( !file.isEmpty() && !file.equals( UNSAVED_FILE_PATH )){
    	
    	if( widget ) {
    		//widgets are stored in a plugin specific folder (currently it is /public/cde/widgets/)
    		file = Utils.joinPath(CdeEnvironment.getPluginRepositoryDir(), CdeConstants.SolutionFolders.WIDGETS, file);
    	}
    	
    	// check access to path folder
    	String fileDir = file.contains(".wcdf") || file.contains(".cdfde") ? file.substring(0, file.lastIndexOf("/")) : file;
    	
    	if( !CdeEnvironment.getUserContentAccess().hasAccess( fileDir, FileAccess.EXECUTE )){
     	  response.setStatus( HttpServletResponse.SC_FORBIDDEN );
 	      String msg = "Access denied for the syncronize method syncronizeDashboard." + operation + " : "+ file;
 	      logger.warn( msg );
 	      return JsonUtils.getJsonResult( false, msg );
    	}
    }
    
    try {
      final DashboardStructure dashboardStructure = new DashboardStructure();
      Object result = null;
      HashMap<String, Object> params = new HashMap<String, Object>( request.getParameterMap() );
      params.put( MethodParams.FILE, file );
      params.put( MethodParams.WIDGET, String.valueOf(widget) );
      if( !author.isEmpty() ) params.put( MethodParams.AUTHOR, author );
      if( !style.isEmpty() ) params.put( MethodParams.STYLE, style );
      if( !widgetName.isEmpty() ) params.put( MethodParams.WIDGET_NAME, widgetName );
      if( !renderType.isEmpty() ) params.put( MethodParams.RENDER_TYPE, renderType );
      String[] widgetParameters = widgetParams.toArray( new String[0] );
      if( widgetParameters.length > 0 ) params.put( MethodParams.WIDGET_PARAMETERS, widgetParameters );

      String wcdfdeFile = file.replace( ".wcdf", ".cdfde" );
      
      if ( OPERATION_LOAD.equalsIgnoreCase( operation ) ) {
        return dashboardStructure.load( wcdfdeFile );
      } else if ( OPERATION_DELETE.equalsIgnoreCase( operation ) ) {
        dashboardStructure.delete( params );
      } else if ( OPERATION_SAVE.equalsIgnoreCase( operation ) ) {
        result = dashboardStructure.save(file, cdfStructure );
      } else if ( OPERATION_SAVE_AS.equalsIgnoreCase( operation ) ) {
        result = dashboardStructure.saveAs( file, title, description, cdfStructure );
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

  @POST
  @Path( "/syncronizeTemplates" )
  @Produces( MimeTypes.JSON )
  public void syncTemplates( @FormParam( MethodParams.OPERATION ) String operation, 
		  					 @FormParam( MethodParams.FILE ) String file,
		  					 @FormParam( MethodParams.DASHBOARD_STRUCTURE ) String cdfStructure,
		  					 @Context HttpServletResponse response ) throws IOException, DashboardStructureException {
    final CdfTemplates cdfTemplates = new CdfTemplates();
    Object result = null;
    
    if ( OPERATION_LOAD.equalsIgnoreCase(operation) ) {
      result = cdfTemplates.load();
    
    } else if ( OPERATION_SAVE.equalsIgnoreCase(operation) ) {
      cdfTemplates.save( file, cdfStructure );
    }
    
    JsonUtils.buildJsonResult( response.getOutputStream(), true, result );
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
    private static final String TITLE = "title";
    private static final String AUTHOR = "author";
    private static final String DESCRIPTION = "description";
    private static final String STYLE = "style";
    private static final String OPERATION = "operation";
    private static final String RENDER_TYPE = "renderType";
    private static final String WIDGET = "widget";
    private static final String WIDGET_NAME = "widgetName";
    private static final String WIDGET_PARAMETERS = "widgetParameters";
    private static final String DASHBOARD_STRUCTURE = "cdfstructure";
  }
}
