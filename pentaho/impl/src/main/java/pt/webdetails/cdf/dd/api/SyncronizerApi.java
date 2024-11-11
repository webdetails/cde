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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

import com.sun.jersey.multipart.FormDataParam;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.DashboardDesignerException;
import pt.webdetails.cdf.dd.Messages;
import pt.webdetails.cdf.dd.cdf.CdfStyles;
import pt.webdetails.cdf.dd.cdf.CdfTemplates;
import pt.webdetails.cdf.dd.structure.DashboardStructure;
import pt.webdetails.cdf.dd.structure.DashboardStructureException;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.CharsetHelper;

@Path( "pentaho-cdf-dd/api/syncronizer" )
public class SyncronizerApi { //TODO: synchronizer?

  private static final Log logger = LogFactory.getLog( SyncronizerApi.class );

  protected static final String OPERATION_LOAD = "load";
  protected static final String OPERATION_DELETE = "delete";
  protected static final String OPERATION_DELETE_PREVIEW = "deletepreview";
  protected static final String OPERATION_SAVE = "save";
  protected static final String OPERATION_SAVE_AS = "saveas";
  protected static final String OPERATION_NEW_FILE = "newfile";
  protected static final String OPERATION_SAVE_SETTINGS = "savesettings";

  private static final String GET_RESOURCE = "api/resources/get?resource=";
  /**
   * for historical reasons..
   */
  public static final String UNSAVED_FILE_PATH = "null/null/null";


  @POST
  @Path( "/syncronizeDashboard" )
  @Produces( APPLICATION_JSON )
  public String syncronize( @FormParam( MethodParams.FILE ) @DefaultValue( "" ) String file,
                            @FormParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
                            @FormParam( MethodParams.TITLE ) @DefaultValue( "" ) String title,
                            @FormParam( MethodParams.AUTHOR ) @DefaultValue( "" ) String author,
                            @FormParam( MethodParams.DESCRIPTION ) @DefaultValue( "" ) String description,
                            @FormParam( MethodParams.STYLE ) @DefaultValue( "" ) String style,
                            @FormParam( MethodParams.WIDGET_NAME ) @DefaultValue( "" ) String widgetName,
                            @FormParam( MethodParams.WIDGET ) boolean widget,
                            @FormParam( MethodParams.RENDERER_TYPE ) @DefaultValue( "" ) String rendererType,
                            @FormParam( MethodParams.WIDGET_PARAMETERS ) List<String> widgetParams,
                            @FormParam( MethodParams.DASHBOARD_STRUCTURE ) String cdfStructure,
                            @FormParam( MethodParams.OPERATION ) String operation,
                            @FormParam( MethodParams.REQUIRE ) boolean require,
                            @Context HttpServletRequest servletRequest,
                            @Context HttpServletResponse servletResponse ) throws Exception {

    final XSSHelper xssHelper = XSSHelper.getInstance();

    file = xssHelper.escape( file );
    title = xssHelper.escape( title );
    author = xssHelper.escape( author );
    description = xssHelper.escape( description );
    style = xssHelper.escape( style );
    widgetName = xssHelper.escape( widgetName );
    rendererType = xssHelper.escape( rendererType );
    cdfStructure = xssHelper.escape( cdfStructure );
    operation = xssHelper.escape( operation );

    if ( null != widgetParams ) {
      for ( int i = 0; i < widgetParams.size(); i++ ) {
        widgetParams.set( i, xssHelper.escape( widgetParams.get( i ) ) );
      }
    }

    servletResponse.setContentType( APPLICATION_JSON );
    servletResponse.setCharacterEncoding( CharsetHelper.getEncoding() );

    boolean isPreview = false;

    if ( !file.isEmpty() && !file.equals( UNSAVED_FILE_PATH ) ) {
      file = Utils.getURLDecoded( file, CharsetHelper.getEncoding() );

      isPreview = ( file.contains( "_tmp.cdfde" ) || file.contains( "_tmp.wcdf" ) );

      IReadAccess rwAccess = Utils.getSystemOrUserRWAccess( file );
      if ( rwAccess == null ) {
        String msg = "Access denied for the synchronize method syncronizeDashboard." + operation + " : " + file;
        logger.warn( msg );

        return JsonUtils.getJsonResult( false, msg );
      }
    }

    try {
      HashMap<String, Object> params = new HashMap<>();
      params.put( MethodParams.FILE, file );
      params.put( MethodParams.WIDGET, String.valueOf( widget ) );
      params.put( MethodParams.REQUIRE, String.valueOf( require ) );

      if ( !author.isEmpty() ) {
        params.put( MethodParams.AUTHOR, author );
      }

      if ( !style.isEmpty() ) {
        params.put( MethodParams.STYLE, style );
      }

      if ( !widgetName.isEmpty() ) {
        params.put( MethodParams.WIDGET_NAME, widgetName );
      }

      if ( !rendererType.isEmpty() ) {
        params.put( MethodParams.RENDERER_TYPE, rendererType );
      }

      if ( !title.isEmpty() ) {
        params.put( MethodParams.TITLE, title );
      }

      if ( !description.isEmpty() ) {
        params.put( MethodParams.DESCRIPTION, description );
      }

      String[] widgetParameters = widgetParams.toArray( new String[ 0 ] );
      if ( widgetParameters.length > 0 ) {
        params.put( MethodParams.WIDGET_PARAMETERS, widgetParameters );
      }

      final String wcdfdeFile = file.replace( ".wcdf", ".cdfde" );
      final DashboardStructure dashboardStructure = new DashboardStructure();

      Object result = null;
      if ( OPERATION_LOAD.equalsIgnoreCase( operation ) ) {
        return dashboardStructure.load( wcdfdeFile );
      }

      if ( OPERATION_DELETE.equalsIgnoreCase( operation ) ) {
        dashboardStructure.delete( params );

      } else if ( OPERATION_DELETE_PREVIEW.equalsIgnoreCase( operation ) ) {
        dashboardStructure.deletePreviewFiles( wcdfdeFile );

      } else if ( OPERATION_SAVE.equalsIgnoreCase( operation ) ) {
        result = dashboardStructure.save( file, cdfStructure );

      } else if ( OPERATION_SAVE_AS.equalsIgnoreCase( operation ) ) {
        if ( StringUtils.isEmpty( title ) ) {
          title = FilenameUtils.getBaseName( file );
        }

        result = dashboardStructure.saveAs( file, title, description, cdfStructure, isPreview );

      } else if ( OPERATION_NEW_FILE.equalsIgnoreCase( operation ) ) {
        dashboardStructure.newfile( params );

      } else if ( OPERATION_SAVE_SETTINGS.equalsIgnoreCase( operation ) ) {
        // check if user is attempting to save settings over a new (non yet saved) dashboard/widget/template
        if ( StringUtils.isEmpty( file ) || file.equals( UNSAVED_FILE_PATH ) ) {
          logger.warn( getMessage( "CdfTemplates.ERROR_003_SAVE_DASHBOARD_FIRST" ) );
          return JsonUtils.getJsonResult( false, getMessage( "CdfTemplates.ERROR_003_SAVE_DASHBOARD_FIRST" ) );
        }

        result = dashboardStructure.saveSettingsToWcdf( params );

      } else {
        logger.error( "Unknown operation: " + operation );
      }

      return JsonUtils.getJsonResult( true, result );

    } catch ( Exception e ) {
      if ( e.getCause() != null ) {
        handleDashboardStructureException( e, servletResponse.getOutputStream() );
      }

      throw e;
    }
  }

  @POST
  @Path( "/syncronizeTemplates" )
  @Produces( APPLICATION_JSON )
  public void syncTemplates(
      @FormParam( MethodParams.OPERATION ) String operation,
      @FormParam( MethodParams.FILE ) String file,
      @FormParam( MethodParams.DASHBOARD_STRUCTURE ) String cdfStructure,
      @FormParam( MethodParams.RENDERER_TYPE ) String rendererType,
      @Context HttpServletResponse servletResponse ) throws IOException, DashboardStructureException, JSONException {

    final XSSHelper xssHelper = XSSHelper.getInstance();

    file = xssHelper.escape( file );
    rendererType = xssHelper.escape( rendererType );
    cdfStructure = xssHelper.escape( cdfStructure );
    operation = xssHelper.escape( operation );

    servletResponse.setContentType( APPLICATION_JSON );
    servletResponse.setCharacterEncoding( CharsetHelper.getEncoding() );

    Object result = null;
    if ( OPERATION_LOAD.equalsIgnoreCase( operation ) ) {
      result = ( new CdfTemplates( GET_RESOURCE ) ).load( rendererType );

    } else if ( OPERATION_SAVE.equalsIgnoreCase( operation ) ) {
      ( new CdfTemplates( GET_RESOURCE ) ).save( file, cdfStructure, rendererType );
    }

    JsonUtils.buildJsonResult( servletResponse.getOutputStream(), true, result );
  }

  @GET
  @Path( "/syncronizeStyles" )
  @Produces( APPLICATION_JSON )
  public Response syncStyles() throws DashboardDesignerException, JSONException {
    String styles = listStyles();

    Map<String, String> mtParameters = new HashMap<>();
    mtParameters.put( "charset", CharsetHelper.getEncoding() );

    MediaType mt = new MediaType( APPLICATION_JSON_TYPE.getType(), APPLICATION_JSON_TYPE.getSubtype(), mtParameters );

    return Response.ok( styles, mt ).build();
  }

  private class MethodParams {
    private static final String FILE = "file";
    private static final String PATH = "path";
    private static final String TITLE = "title";
    private static final String AUTHOR = "author";
    private static final String DESCRIPTION = "description";
    private static final String STYLE = "style";
    private static final String OPERATION = "operation";
    private static final String RENDERER_TYPE = "rendererType";
    private static final String WIDGET = "widget";
    private static final String WIDGET_NAME = "widgetName";
    private static final String WIDGET_PARAMETERS = "widgetParameters";
    private static final String DASHBOARD_STRUCTURE = "cdfstructure";
    private static final String REQUIRE = "require";
  }

  @POST
  @Path( "/saveDashboard" )
  @Consumes( MULTIPART_FORM_DATA )
  @Produces( APPLICATION_JSON )
  public String saveDashboard( @FormDataParam( MethodParams.FILE ) @DefaultValue( "" ) String file,
                               @FormDataParam( MethodParams.TITLE ) @DefaultValue( "" ) String title,
                               @FormDataParam( MethodParams.DESCRIPTION ) @DefaultValue( "" ) String description,
                               @FormDataParam( MethodParams.DASHBOARD_STRUCTURE ) String cdfStructure,
                               @FormDataParam( MethodParams.OPERATION ) String operation,
                               @Context HttpServletResponse response ) throws Exception {

    response.setContentType( APPLICATION_JSON );
    response.setCharacterEncoding( CharsetHelper.getEncoding() );

    if( !isAllowSaveDashboard() ) {
      String msg = "You have disabled this feature on settings.xml. Please set allow-save-dashboard property to true.";
      logger.warn( msg );
      return JsonUtils.getJsonResult( false, msg );
    }

    final XSSHelper xssHelper = XSSHelper.getInstance();

    file = xssHelper.escape( file );
    title = xssHelper.escape( title );
    description = xssHelper.escape( description );
    cdfStructure = xssHelper.escape( cdfStructure );
    operation = xssHelper.escape( operation );

    boolean isPreview = false;

    if ( !file.isEmpty()
        && !( file.equals( UNSAVED_FILE_PATH ) || Utils.getURLDecoded( file ).equals( UNSAVED_FILE_PATH ) ) ) {

      file = Utils.getURLDecoded( file, CharsetHelper.getEncoding() );

      if ( StringUtils.isEmpty( title ) ) {
        title = FilenameUtils.getBaseName( file );
      }

      // check access to path folder
      String fileDir =
          file.contains( ".wcdf" ) || file.contains( ".cdfde" ) ? file.substring( 0, file.lastIndexOf( '/' ) ) : file;

      isPreview = ( file.contains( "_tmp.cdfde" ) || file.contains( "_tmp.wcdf" ) );

      IReadAccess rwAccess;
      if ( OPERATION_SAVE_AS.equalsIgnoreCase( operation ) && !isPreview ) {
        rwAccess = Utils.getSystemOrUserRWAccess( fileDir );
      } else {
        rwAccess = Utils.getSystemOrUserRWAccess( file );
      }

      if ( rwAccess == null ) {
        String msg = "Access denied for the syncronize method saveDashboard." + operation + " : " + file;
        logger.warn( msg );
        return JsonUtils.getJsonResult( false, msg );
      }
    }

    try {
      final DashboardStructure dashboardStructure = new DashboardStructure();
      Object result = null;


      if ( OPERATION_SAVE.equalsIgnoreCase( operation ) ) {
        result = dashboardStructure.save( file, cdfStructure );
      } else if ( OPERATION_SAVE_AS.equalsIgnoreCase( operation ) ) {
        result = dashboardStructure.saveAs( file, title, description, cdfStructure, isPreview );
      } else {
        logger.error( "Unknown operation: " + operation );
      }
      return JsonUtils.getJsonResult( true, result );
    } catch ( Exception e ) {
      if ( e.getCause() != null ) {
        handleDashboardStructureException( e, response.getOutputStream() );
      }

      throw e;
    }
  }

  // PPP-4798
  protected boolean isAllowSaveDashboard() {
    return Boolean.parseBoolean(PentahoSystem.get( IPluginResourceLoader.class, null )
            .getPluginSetting( SyncronizerApi.class, CdeConstants.PLUGIN_SETTINGS_ALLOW_SAVE_DASHBOARD, "true" ));
  }

  //useful to mock message bundle when unit testing SyncronizerApi
  protected String getMessage( String key ) {
    return Messages.getString( key );
  }

  protected String listStyles( )
      throws DashboardDesignerException, JSONException {

    final CdfStyles cdfStyles = new CdfStyles();
    return JsonUtils.getJsonResult( true, cdfStyles.liststyles() );
  }

  private void handleDashboardStructureException( Exception e, OutputStream out ) throws Exception {
    Throwable cause = e.getCause();
    if ( cause instanceof DashboardStructureException ) {
      JsonUtils.buildJsonResult( out, false, cause.getMessage() );

    } else if ( e instanceof InvocationTargetException ) {
      throw (Exception) cause;
    }
  }

}
