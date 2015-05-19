/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

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

import com.sun.jersey.multipart.FormDataParam;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.DashboardDesignerException;
import pt.webdetails.cdf.dd.Messages;
import pt.webdetails.cdf.dd.cdf.CdfStyles;
import pt.webdetails.cdf.dd.cdf.CdfTemplates;
import pt.webdetails.cdf.dd.structure.DashboardStructure;
import pt.webdetails.cdf.dd.structure.DashboardStructureException;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.CharsetHelper;
import pt.webdetails.cpf.utils.MimeTypes;

@Path( "pentaho-cdf-dd/api/syncronizer" )
public class SyncronizerApi { //TODO: synchronizer?

  private static final Log logger = LogFactory.getLog( SyncronizerApi.class );

  private static final String OPERATION_LOAD = "load";
  private static final String OPERATION_DELETE = "delete";
  private static final String OPERATION_DELETE_PREVIEW = "deletepreview";
  private static final String OPERATION_SAVE = "save";
  private static final String OPERATION_SAVE_AS = "saveas";
  private static final String OPERATION_NEW_FILE = "newfile";
  private static final String OPERATION_SAVE_SETTINGS = "savesettings";

  private static final String GET_RESOURCE = "api/resources/get?resource=";
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
                            @FormParam( MethodParams.DESCRIPTION ) @DefaultValue( "" ) String description,
                            @FormParam( MethodParams.STYLE ) @DefaultValue( "" ) String style,
                            @FormParam( MethodParams.WIDGET_NAME ) @DefaultValue( "" ) String widgetName,
                            @FormParam( MethodParams.WIDGET ) boolean widget,
                            @FormParam( MethodParams.RENDERER_TYPE ) @DefaultValue( "" ) String rendererType,
                            @FormParam( MethodParams.WIDGET_PARAMETERS ) List<String> widgetParams,
                            @FormParam( MethodParams.DASHBOARD_STRUCTURE ) String cdfStructure,
                            @FormParam( MethodParams.OPERATION ) String operation,
                            @FormParam( MethodParams.REQUIRE ) boolean require,
                            @Context HttpServletRequest request,
                            @Context HttpServletResponse response ) throws Exception {

    boolean isPreview = false;

    if ( !file.isEmpty() && !file.equals( UNSAVED_FILE_PATH ) ) {

      file = Utils.getURLDecoded( file, CharsetHelper.getEncoding() );

      // check access to path folder
      String fileDir =
        file.contains( ".wcdf" ) || file.contains( ".cdfde" ) ? file.substring( 0, file.lastIndexOf( "/" ) ) : file;

      isPreview = ( file.contains( "_tmp.cdfde" ) || file.contains( "_tmp.wcdf" ) );

      IReadAccess rwAccess = Utils.getSystemOrUserRWAccess( file );

      if ( rwAccess == null ) {
        String msg = "Access denied for the syncronize method syncronizeDashboard." + operation + " : " + file;
        logger.warn( msg );
        return JsonUtils.getJsonResult( false, msg );
      }
    }

    try {
      final DashboardStructure dashboardStructure = new DashboardStructure();
      Object result = null;
      HashMap<String, Object> params = new HashMap<String, Object>();
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

      String wcdfdeFile = file.replace( ".wcdf", ".cdfde" );

      if ( OPERATION_LOAD.equalsIgnoreCase( operation ) ) {
        return dashboardStructure.load( wcdfdeFile );
      } else if ( OPERATION_DELETE.equalsIgnoreCase( operation ) ) {
        dashboardStructure.delete( params );
      } else if( OPERATION_DELETE_PREVIEW.equalsIgnoreCase( operation ) ) {
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
        if( StringUtils.isEmpty( file ) || file.equals( UNSAVED_FILE_PATH ) ) {
          logger.warn( getMessage( "CdfTemplates.ERROR_003_SAVE_DASHBOARD_FIRST" ) );
          return JsonUtils.getJsonResult( false, getMessage( "CdfTemplates.ERROR_003_SAVE_DASHBOARD_FIRST" ) );
        }
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
    final CdfTemplates cdfTemplates = new CdfTemplates( GET_RESOURCE );
    Object result = null;

    if ( OPERATION_LOAD.equalsIgnoreCase( operation ) ) {
      result = cdfTemplates.load();

    } else if ( OPERATION_SAVE.equalsIgnoreCase( operation ) ) {
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
    private static final String RENDERER_TYPE = "rendererType";
    private static final String WIDGET = "widget";
    private static final String WIDGET_NAME = "widgetName";
    private static final String WIDGET_PARAMETERS = "widgetParameters";
    private static final String DASHBOARD_STRUCTURE = "cdfstructure";
    private static final String REQUIRE = "require";
  }

  @POST
  @Path( "/saveDashboard" )
  @Produces( MimeTypes.JSON )
  @Consumes( "multipart/form-data" )
  public String saveDashboard( @FormDataParam( MethodParams.FILE ) @DefaultValue( "" ) String file,
                               @FormDataParam( MethodParams.TITLE ) @DefaultValue( "" ) String title,
                               @FormDataParam( MethodParams.DESCRIPTION ) @DefaultValue( "" ) String description,
                               @FormDataParam( MethodParams.DASHBOARD_STRUCTURE ) String cdfStructure,
                               @FormDataParam( MethodParams.OPERATION ) String operation,
                               @Context HttpServletResponse response ) throws Exception {

    boolean isPreview = false;

    if ( !file.isEmpty() &&
        !( file.equals( UNSAVED_FILE_PATH ) || Utils.getURLDecoded( file ).equals( UNSAVED_FILE_PATH ) ) ){

      file = Utils.getURLDecoded( file, CharsetHelper.getEncoding() );

      if ( StringUtils.isEmpty( title ) ) {
        title = FilenameUtils.getBaseName( file );
      }

      // check access to path folder
      String fileDir =
        file.contains( ".wcdf" ) || file.contains( ".cdfde" ) ? file.substring( 0, file.lastIndexOf( "/" ) ) : file;

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
        if ( e.getCause() instanceof DashboardStructureException ) {
          JsonUtils.buildJsonResult( response.getOutputStream(), false, e.getCause().getMessage() );
        } else if ( e instanceof InvocationTargetException ) {
          throw (Exception) e.getCause();
        }
      }
      throw e;
    }
  }

  //useful to mock message bundle when unit testing SyncronizerApi
  protected String getMessage( String key ){
    return Messages.getString( key );
  }

}
