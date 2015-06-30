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

package pt.webdetails.cdf.dd;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONException;

import org.json.JSONObject;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityParameterProvider;

import pt.webdetails.cdf.dd.cdf.CdfStyles;
import pt.webdetails.cdf.dd.cdf.CdfTemplates;
import pt.webdetails.cdf.dd.datasources.CdaDataSourceReader;
import pt.webdetails.cdf.dd.editor.DashboardEditor;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;
import pt.webdetails.cdf.dd.structure.DashboardStructure;
import pt.webdetails.cdf.dd.structure.DashboardStructureException;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.GenericBasicFileFilter;
import pt.webdetails.cdf.dd.util.GenericFileAndDirectoryFilter;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.SimpleContentGenerator;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.VersionChecker;
import pt.webdetails.cpf.annotations.AccessLevel;
import pt.webdetails.cpf.annotations.Audited;
import pt.webdetails.cpf.annotations.Exposed;
import pt.webdetails.cpf.localization.MessageBundlesHelper;
import pt.webdetails.cpf.olap.OlapUtils;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cpf.repository.util.RepositoryHelper;
import pt.webdetails.cpf.utils.CharsetHelper;
import pt.webdetails.cpf.utils.MimeTypes;

public class DashboardDesignerContentGenerator extends SimpleContentGenerator {

  private static final Log logger = LogFactory.getLog( DashboardDesignerContentGenerator.class );

  private static final long serialVersionUID = 1L;

  private static final String MIME_TYPE = "text/html";

  private static final String CSS_TYPE = "text/css";

  private static final String JAVASCRIPT_TYPE = "text/javascript";

  /**
   * 1 week cache
   */
  private static final int RESOURCE_CACHE_DURATION = 3600 * 24 * 8; // 1 week
  // cache

  private static final int NO_CACHE_DURATION = 0;

  private static final String EXTERNAL_EDITOR_PAGE = "resources/ext-editor.html";

  private static final String COMPONENT_EDITOR_PAGE = "resources/cdf-dd-component-editor.html";

  private static final String OPERATION_LOAD = "load";
  private static final String OPERATION_DELETE = "delete";
  private static final String OPERATION_DELETE_PREVIEW = "deletepreview";
  private static final String OPERATION_SAVE = "save";
  private static final String OPERATION_SAVE_AS = "saveas";
  private static final String OPERATION_NEW_FILE = "newfile";
  private static final String OPERATION_SAVE_SETTINGS = "savesettings";

  private static final String REQUEST_PARAM_FILE = "file";
  private static final String REQUEST_PARAM_PATH = "path";
  private static final String REQUEST_PARAM_OPERATION = "operation";
  private static final String REQUEST_PARAM_CDFSTRUCTURE = "cdfstructure";
  private static final String REQUEST_PARAM_TITLE = "title";
  private static final String REQUEST_PARAM_DESCRIPTION = "description";

  private static final String GET_RESOURCE = "getResource?resource=";

  public static final String UNSAVED_FILE_PATH = "/null/null/null";


  /**
   * Parameters received by content generator
   */
  protected static class MethodParams {
    public static final String DEBUG = "debug";

    public static final String ROOT = "root";

    public static final String SOLUTION = "solution";

    public static final String PATH = "path";

    public static final String FILE = "file";

    public static final String RESOURCE = "resource";

    /**
     * JSON structure
     */

    public static final String DATA = "data";
  }

  /*
   * This block initializes exposed methods
   */
  private static Map<String, Method> exposedMethods = new HashMap<String, Method>();

  static {
    // to keep case-insensitive methods
    logger.info( "loading exposed methods" );
    exposedMethods = getExposedMethods(
      DashboardDesignerContentGenerator.class, true );
  }

  @Override
  protected Method getMethod( String methodName ) throws NoSuchMethodException {
    Method method = exposedMethods.get( StringUtils.lowerCase( methodName ) );
    if ( method == null ) {
      throw new NoSuchMethodException();
    }
    return method;
  }

  public DashboardDesignerContentGenerator() {
  }

  @Override
  public String getPluginName() {
    return CdeEnvironment.getPluginId();
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void syncronize( final OutputStream out ) throws Exception {
    String title = null;
    String description = null;
    String operation = null;
    String file = null;
    String path = null;
    String cdfStructure = null;
    if ( getRequest().getContentType().startsWith( "multipart/form-data" ) ) {
      FileItemFactory factory = new DiskFileItemFactory();
      ServletFileUpload upload = new ServletFileUpload( factory );
      List items = null;
      try {
        items = upload.parseRequest( getRequest() );
      } catch ( FileUploadBase.InvalidContentTypeException e ) {
        logger.debug( "Syncronize - content type not multipart/form-data" );
      }
      if ( items != null ) {
        for ( int i = 0; i < items.size(); i++ ) {
          FileItem fi = (FileItem) items.get( i );

          if ( "operation".equals( fi.getFieldName() ) ) {
            operation = fi.getString();
          }
          if ( "file".equals( fi.getFieldName() ) ) {
            path = Utils.getURLDecoded( fi.getString(), CharsetHelper.getEncoding() );
          }
          if ( "cdfstructure".equals( fi.getFieldName() ) ) {
            cdfStructure = fi.getString( "UTF-8" );
          }
          if ( "title".equals( fi.getFieldName() ) ) {
            title = fi.getString();
          }
          if ( "description".equals( fi.getFieldName() ) ) {
            description = fi.getString();
          }
        }
        if ( StringUtils.isEmpty( title ) ) {
          title = FilenameUtils.getBaseName( path );
        }
        if ( StringUtils.isEmpty( description ) ) {
          description = FilenameUtils.getBaseName( path );
        }
        boolean saveAsAccessGranted = false;
        if ( operation.equals( OPERATION_SAVE_AS ) ) {
          saveAsAccessGranted =
            CdeEnvironment.getUserContentAccess().hasAccess( FilenameUtils.getPath( path ), FileAccess.EXECUTE );
        }
        if ( !saveAsAccessGranted && ( path != null ) && ( !path.endsWith( "_tmp.cdfde" ) )
          && ( !CdeEnvironment.getUserContentAccess().hasAccess( path, FileAccess.EXECUTE ) ) ) {
          getResponse().setStatus( 403 );
          logger.warn( "Access denied for the syncronize method: " + path );
          return;
        }
      }
    } else {

      path = ( (String) getRequestParameters().getParameter( REQUEST_PARAM_FILE ) ).replaceAll( "cdfde", "wcdf" );

      // Check security. Caveat: if no path is supplied, then we're in the new parameter
      if ( getRequestParameters().hasParameter( REQUEST_PARAM_PATH ) && !CdeEnvironment.getUserContentAccess()
        .hasAccess( path, FileAccess.EXECUTE ) ) {
        getResponse().setStatus( HttpServletResponse.SC_FORBIDDEN );
        logger.warn( "Access denied for the syncronize method: " + path );
        return;
      }
      cdfStructure = (String) getRequestParameters().getStringParameter( REQUEST_PARAM_CDFSTRUCTURE, null );

      path = getRequestParameters().getStringParameter( REQUEST_PARAM_FILE, null );
      file = (String) getRequestParameters().getParameter( "file" );

      path = Utils.getURLDecoded( path, CharsetHelper.getEncoding() );
      file = Utils.getURLDecoded( file, CharsetHelper.getEncoding() );

      title = StringUtils.defaultIfEmpty( ( (String) getRequestParameters().getParameter( REQUEST_PARAM_TITLE ) ),
        FilenameUtils.getBaseName( path ) );
      description =
        StringUtils.defaultIfEmpty( (String) getRequestParameters().getParameter( REQUEST_PARAM_DESCRIPTION ), "" );
      operation = getRequestParameters().getStringParameter( REQUEST_PARAM_OPERATION, "" ).toLowerCase();

      // file path must exist
      if ( getRequestParameters() == null || getRequestParameters().getParameter( REQUEST_PARAM_FILE ) == null ) {
        throw new Exception(
          Messages.getString( "SyncronizeCdfStructure.ERROR_002_INVALID_FILE_PARAMETER_EXCEPTION" ) );
      }
    }

    try {

      final DashboardStructure dashboardStructure = new DashboardStructure();
      Object result = null;

      if ( OPERATION_LOAD.equalsIgnoreCase( operation ) ) {
        JsonUtils.buildJsonResult( getResponse().getOutputStream(), true, dashboardStructure.load( file ) );
        return;
      } else if ( OPERATION_DELETE.equalsIgnoreCase( operation ) ) {
        dashboardStructure.delete( toHashMap( getRequestParameters() ) );
      } else if( OPERATION_DELETE_PREVIEW.equalsIgnoreCase( operation ) ) {
        dashboardStructure.deletePreviewFiles( path );
      } else if ( OPERATION_SAVE.equalsIgnoreCase( operation ) ) {
        result = dashboardStructure.save( path, cdfStructure );
      } else if ( OPERATION_SAVE_AS.equalsIgnoreCase( operation ) ) {
        boolean isPreview = ( path.contains( "_tmp.cdfde" ) || path.contains( "_tmp.wcdf" ) );
        dashboardStructure.saveAs( path, title, description, cdfStructure, isPreview );

      } else if ( OPERATION_NEW_FILE.equalsIgnoreCase( operation ) ) {
        dashboardStructure.newfile( toHashMap( getRequestParameters() ) );

      } else if ( OPERATION_SAVE_SETTINGS.equalsIgnoreCase( operation ) ) {
        // check if user is attempting to save settings over a new (non yet saved) dashboard/widget/template
        if ( StringUtils.isEmpty( file ) || UNSAVED_FILE_PATH.equals( file ) ) {
          String msg = Messages.getString( "CdfTemplates.ERROR_003_SAVE_DASHBOARD_FIRST" );
          logger.warn( msg );
          JsonUtils.buildJsonResult( getResponse().getOutputStream(), false, msg );
          return;
        }
        dashboardStructure.savesettings( toHashMap( getRequestParameters() ) );

      } else {
        logger.error( "Unknown operation: " + operation );
      }

      JsonUtils.buildJsonResult( getResponse().getOutputStream(), true, result );

    } catch ( Exception e ) {
      if ( e.getCause() != null ) {
        if ( e.getCause() instanceof DashboardStructureException ) {
          JsonUtils.buildJsonResult( getResponse().getOutputStream(), false, e.getCause().getMessage() );
        } else if ( e instanceof InvocationTargetException ) {
          throw (Exception) e.getCause();
        }
      }
      throw e;
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  @Audited( action = "edit" )
  public void newDashboard( final OutputStream out ) throws Exception {
    this.edit( out );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JAVASCRIPT )
  public void getComponentDefinitions( OutputStream out ) throws Exception {
    // Get and output the definitions
    try {
      String definition = MetaModelManager.getInstance().getJsDefinition();
      out.write( definition.getBytes() );
    } catch ( Exception ex ) {
      String msg = "Could not get component definitions: " + ex.getMessage();
      logger.error( msg );
      throw ex;
    }
  }

  /**
   * Re-initializes the designer back-end.
   *
   * @author pdpi
   */
  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void refresh( final OutputStream out ) throws Exception {
    String msg = "Refreshed CDE Successfully";

    try {
      DashboardManager.getInstance().refreshAll();
    } catch ( Exception re ) {
      msg = "Method refresh failed while trying to execute.";

      logger.error( msg, re );
      writeOut( out, msg );
      return;
    }

    writeOut( out, msg );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void getContent( OutputStream out ) throws Exception {
    CdfRunJsDashboardWriteResult dashboardWrite = this.loadDashboard();
    writeOut( out, dashboardWrite.getContent() );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void getHeaders( final OutputStream out ) throws Exception {
    CdfRunJsDashboardWriteResult dashboardWrite = this.loadDashboard();
    writeOut( out, dashboardWrite.getHeader() );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  @Audited( action = "render" )
  public void render( final OutputStream out ) throws IOException {
    String relativePath = getWcdfRelativePath( getRequestParameters() );

    // Check security
    if ( !CdeEnvironment.getUserContentAccess().hasAccess( relativePath, FileAccess.EXECUTE ) ) {
      writeOut( out, "Access Denied or File Not Found." );
      return;
    }

    // Response
    try {
      setResponseHeaders( MIME_TYPE, 0, null );
    } catch ( Exception e ) {
      logger.warn( e.toString() );
    }

    try {
      long start = System.currentTimeMillis();
      logger.info( "[Timing] CDE Starting Dashboard Rendering" );

      String result = this.loadDashboard().render( getCdfContext() );

      //i18n token replacement
      if ( !StringUtils.isEmpty( result ) ) {
        IReadAccess readAccess = Utils.getSystemOrUserReadAccess( relativePath );
        String msgDir = FilenameUtils.getPath( FilenameUtils.separatorsToUnix( relativePath ) );
        msgDir = msgDir.startsWith( Util.SEPARATOR ) ? msgDir : Util.SEPARATOR + msgDir;
        result = new MessageBundlesHelper( msgDir, readAccess, CdeEnvironment.getPluginSystemWriter(),
                CdeEngine.getEnv().getLocale(), CdeEngine.getEnv().getExtApi().getPluginStaticBaseUrl() )
                .replaceParameters( result, null );
      }

      writeOut( out, result );
      logger.info( "[Timing] CDE Finished Dashboard Rendering: "
        + Utils.ellapsedSeconds( start ) + "s" );
    } catch ( FileNotFoundException ex ) { // could not open cdfe
      String msg = "File not found: " + ex.getLocalizedMessage();
      logger.error( msg, ex );
      writeOut( out, msg );
    } catch ( Exception ex ) {
      String msg = "Could not load dashboard: " + ex.getMessage();
      logger.error( msg, ex );
      writeOut( out, msg );
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC, outputType = MimeType.CSS )
  public void getCssResource( final OutputStream out ) throws Exception {
    setResponseHeaders( CSS_TYPE, RESOURCE_CACHE_DURATION, null );
    getResource( out );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JAVASCRIPT )
  public void getJsResource( final OutputStream out ) throws Exception {
    setResponseHeaders( JAVASCRIPT_TYPE, RESOURCE_CACHE_DURATION, null );
    final HttpServletResponse response = getResponse();
    // Set cache for 1 year, give or take.
    response.setHeader( "Cache-Control", "max-age=" + 60 * 60 * 24 * 365 );
    try {
      getResource( out );
    } catch ( SecurityException e ) {
      response.sendError( HttpServletResponse.SC_FORBIDDEN );
    } catch ( FileNotFoundException e ) {
      response.sendError( HttpServletResponse.SC_NOT_FOUND );
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void getUntypedResource( final OutputStream out ) throws IOException {
    final HttpServletResponse response = getResponse();
    response.setHeader( "Content-Type", "text/plain" );
    response.setHeader( "content-disposition", "inline" );

    getResource( out );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void getImg( final OutputStream out ) throws Exception {
    String pathString = getPathParameters().getStringParameter(
      MethodParams.PATH, "" );
    String resource;
    if ( pathString.split( "/" ).length > 2 ) {
      resource = pathString.replaceAll( "^/.*?/", "" );
    } else {
      resource = getRequestParameters().getStringParameter(
        MethodParams.PATH, "" );
    }

    resource = resource.startsWith( "/" ) ? resource : "/" + resource;

    String[] path = resource.split( "/" );
    String fileName = path[ path.length - 1 ];
    setResponseHeaders( MimeTypes.getMimeType( fileName ),
      RESOURCE_CACHE_DURATION, null );
    final HttpServletResponse response = getResponse();
    // Set cache for 1 year, give or take.
    response.setHeader( "Cache-Control", "max-age=" + 60 * 60 * 24 * 365 );
    try {
      getResource( out );
    } catch ( SecurityException e ) {
      response.sendError( HttpServletResponse.SC_FORBIDDEN );
    } catch ( FileNotFoundException e ) {
      response.sendError( HttpServletResponse.SC_NOT_FOUND );
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void res( final OutputStream out ) throws Exception {
    String pathString = getPathParameters().getStringParameter( MethodParams.PATH, "" );
    String resource;
    if ( pathString.split( "/" ).length > 2 ) {
      resource = pathString.replaceAll( "^/.*?/", "" );
    } else {
      resource = getRequestParameters().getStringParameter( MethodParams.PATH, "" );
    }

    final HttpServletResponse response = getResponse();

    if ( StringUtils.isEmpty( resource ) ) {
      response.sendError( HttpServletResponse.SC_NOT_FOUND );
      return;
    }

    resource = StringUtils.strip( resource, "/" );

    String[] path = resource.split( "/" );
    String[] fileName = path[ path.length - 1 ].split( "\\." );

    String mimeType;
    try {
      final MimeTypes.FileType fileType = MimeTypes.FileType.valueOf( fileName[ fileName.length - 1 ].toUpperCase() );
      mimeType = MimeTypes.getMimeType( fileType );
    } catch ( java.lang.IllegalArgumentException ex ) {
      mimeType = "";
    } catch ( EnumConstantNotPresentException ex ) {
      mimeType = "";
    }

    setResponseHeaders( mimeType, RESOURCE_CACHE_DURATION, null );

    // Set cache for 1 year, give or take.
    response.setHeader( "Cache-Control", "max-age=" + 60 * 60 * 24 * 365 );
    response.setHeader( "content-disposition", "inline; filename=\"" + path[ path.length - 1 ] + "\"" );
    InputStream resInput = null;
    try {
      IBasicFile file = Utils.getFileViaAppropriateReadAccess( resource );
      if ( file == null ) {
        logger.error( "resource not found:" + resource );
        response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        return;
      }
      resInput = file.getContents();
      IOUtils.copy( resInput, out );
      setCacheControl();
    } catch ( SecurityException e ) {
      response.sendError( HttpServletResponse.SC_FORBIDDEN );
    } catch ( IOException e ) {
      logger.error( "Erro reading resource " + resource, e );
    } finally {
      IOUtils.closeQuietly( resInput );
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void getResource( final OutputStream out ) throws IOException {

    String pathString = getPathParameters().getStringParameter( MethodParams.PATH, null );
    String resource;
    if ( pathString.split( "/" ).length > 2 ) {
      resource = pathString.replaceAll( "^/.*?/", "" );
    } else {
      resource = getRequestParameters().getStringParameter( MethodParams.RESOURCE, null );
    }

    final HttpServletResponse response = getResponse();

    if ( StringUtils.isEmpty( resource ) ) {
      response.sendError( HttpServletResponse.SC_NOT_FOUND );
      return;
    }

    resource = StringUtils.strip( resource, "/" );

    String[] path = resource.split( "/" );
    String[] fileName = path[ path.length - 1 ].split( "\\." );

    String mimeType;
    try {
      final MimeTypes.FileType fileType = MimeTypes.FileType.valueOf( fileName[ fileName.length - 1 ].toUpperCase() );
      mimeType = MimeTypes.getMimeType( fileType );
    } catch ( java.lang.IllegalArgumentException ex ) {
      mimeType = "";
    } catch ( EnumConstantNotPresentException ex ) {
      mimeType = "";
    }

    try {
      setResponseHeaders( mimeType, RESOURCE_CACHE_DURATION, null );

      // Set cache for 1 year, give or take.
      response.setHeader( "Cache-Control", "max-age=" + 60 * 60 * 24 * 365 );
      response.setHeader( "content-disposition", "inline; filename=\"" + path[ path.length - 1 ] + "\"" );

      IBasicFile file = Utils.getFileViaAppropriateReadAccess( resource );
      if ( file == null ) {
        logger.error( "resource not found:" + resource );
        response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        return;
      }

      IOUtils.copy( file.getContents(), out );
      setCacheControl();
    } catch ( SecurityException e ) {
      response.sendError( HttpServletResponse.SC_FORBIDDEN );
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  @Audited( action = "edit" )
  public void edit( final OutputStream out ) throws Exception {
    // 0 - Check security. Caveat: if no path is supplied, then we're in the new parameter
    IParameterProvider requestParams = getRequestParameters();
    IParameterProvider pathParams = getPathParameters();

    boolean debugMode = requestParams.hasParameter( MethodParams.DEBUG )
      && requestParams.getParameter( MethodParams.DEBUG ).toString().equals( "true" );

    String wcdfPath = getWcdfRelativePath( requestParams );

    if ( requestParams.hasParameter( CdeConstants.MethodParams.PATH )
      && !CdeEnvironment.getUserContentAccess().hasAccess( wcdfPath, FileAccess.WRITE ) ) {
      writeOut( out, "Access Denied" );
    }

    String result = DashboardEditor.getEditor( wcdfPath, debugMode, getScheme( pathParams ), false );

    //i18n token replacement
    if ( !StringUtils.isEmpty( result ) ) {
      /* cde editor's i18n is different; it continues on relying on pentaho-cdf-dd/lang/messages.properties */

      String msgDir = FilenameUtils.getPath( FilenameUtils.separatorsToUnix( wcdfPath ) );
      result = new MessageBundlesHelper( msgDir, CdeEnvironment.getPluginSystemReader( null ),
              CdeEnvironment.getPluginSystemWriter(), CdeEngine.getEnv().getLocale(),
              CdeEngine.getEnv().getExtApi().getPluginStaticBaseUrl() ).replaceParameters( result, null );
    }

    writeOut( out, result );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void syncTemplates( final OutputStream out ) throws Exception {
    IParameterProvider requestParams = getRequestParameters();
    String method = requestParams.getStringParameter( REQUEST_PARAM_OPERATION, null );

    final CdfTemplates cdfTemplates = new CdfTemplates( GET_RESOURCE );

    if ( method.equals( OPERATION_LOAD ) ) {
      Object result = cdfTemplates.load();
      JsonUtils.buildJsonResult( out, true, result );
    } else if ( method.equals( OPERATION_SAVE ) ) {
      String file = requestParams.getStringParameter( REQUEST_PARAM_FILE, null ),
        structure = requestParams.getStringParameter( CdeConstants.MethodParams.CDF_STRUCTURE, null );
      cdfTemplates.save( file, structure );
      JsonUtils.buildJsonResult( out, true, null );
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void syncStyles( final OutputStream out ) throws Exception {
    final CdfStyles cdfStyles = new CdfStyles();

    Object result = cdfStyles.liststyles();
    JsonUtils.buildJsonResult( out, true, result );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void listRenderers( final OutputStream out ) throws Exception {
    writeOut( out, "{\"result\": [\""
      + DashboardWcdfDescriptor.DashboardRendererType.MOBILE.getType()
      + "\",\""
      + DashboardWcdfDescriptor.DashboardRendererType.BLUEPRINT.getType()
      + "\",\""
      + DashboardWcdfDescriptor.DashboardRendererType.BOOTSTRAP.getType()

      + "\"]}" );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void olapUtils( final OutputStream out ) throws JSONException {
    OlapUtils olapUtils = new OlapUtils();
    Object result = null;

    try {
      String operation = getRequestParameters().getStringParameter( "operation", "-" );

      if ( operation.equals( "GetOlapCubes" ) ) {

        result = olapUtils.getOlapCubes();

      } else if ( operation.equals( "GetCubeStructure" ) ) {

        String catalog = getRequestParameters().getStringParameter( "catalog", null );
        String cube = getRequestParameters().getStringParameter( "cube", null );
        String jndi = getRequestParameters().getStringParameter( "jndi", null );

        result = olapUtils.getCubeStructure( catalog, cube, jndi );

      } else if ( operation.equals( "GetLevelMembersStructure" ) ) {

        String catalog = getRequestParameters().getStringParameter( "catalog", null );
        String cube = getRequestParameters().getStringParameter( "cube", null );
        String member = getRequestParameters().getStringParameter( "member", null );
        String direction = getRequestParameters().getStringParameter( "direction", null );

        result = olapUtils.getLevelMembersStructure( catalog, cube, member, direction );

      } else if ( operation.equals( "GetPaginatedLevelMembers" ) ) {

        String catalog = getRequestParameters().getStringParameter( "catalog", null );
        String cube = getRequestParameters().getStringParameter( "cube", null );
        String startMember = getRequestParameters().getStringParameter( "startMember", null );
        String level = getRequestParameters().getStringParameter( "level", null );
        String context = getRequestParameters().getStringParameter( "context", null );
        String searchTerm = getRequestParameters().getStringParameter( "searchTerm", null );
        long pageSize = getRequestParameters().getLongParameter( "pageSize", 0 );
        long pageStart = getRequestParameters().getLongParameter( "pageStart", 0 );

        result = olapUtils
          .getPaginatedLevelMembers( catalog, cube, level, startMember, context, searchTerm, pageSize, pageStart );

      }
      buildJsonResult( out, result != null, result );
    } catch ( Exception ex ) {
      logger.fatal( ex );
      buildJsonResult( out, false, "Exception found: " + ex.getClass().getName() + " - " + ex.getMessage() );
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void exploreFolder( final OutputStream out ) throws IOException, JSONException {

    IParameterProvider requestParams = getRequestParameters();
    final String folder = requestParams.getStringParameter( "dir", null );
    final String fileExtensions = requestParams.getStringParameter( "fileExtensions", null );
    final String permission = requestParams.getStringParameter( "access", null );
    final String outputType = requestParams.getStringParameter( "outputType", null );

    if ( outputType != null && outputType.equals( "json" ) ) {
      try {
        writeOut( out, RepositoryHelper.toJSON( folder, getFileList( folder, fileExtensions, permission ) ) );
      } catch ( JSONException e ) {
        logger.error( "exploreFolder" + folder, e );
        writeOut( out, "error getting files in folder " + folder );
      }
    } else {
      writeOut( out, RepositoryHelper.toJQueryFileTree( folder, getFileList( folder, fileExtensions, permission ) ) );
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void getCDEplugins( final OutputStream out ) throws IOException {
    CdePlugins plugins = new CdePlugins();
    writeOut( out, plugins.getCdePlugins() );
  }

  /**
   * List CDA datasources for given dashboard.
   */
  @Deprecated
  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void listCdaSources( final OutputStream out ) throws IOException {

    String dashboard = getRequestParameters().getStringParameter( "dashboard", null );
    dashboard = DashboardWcdfDescriptor.toStructurePath( dashboard );

    List<CdaDataSourceReader.CdaDataSource> dataSourcesList = getCdaSources( dashboard );
    CdaDataSourceReader.CdaDataSource[] dataSources =
      dataSourcesList.toArray( new CdaDataSourceReader.CdaDataSource[ dataSourcesList.size() ] );
    String result = "[" + StringUtils.join( dataSources, "," ) + "]";
    writeOut( out, result );
  }

  protected List<CdaDataSourceReader.CdaDataSource> getCdaSources( String dashboard ) {
    return CdaDataSourceReader.getCdaDataSources( dashboard );
  }


  // External Editor v
  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void getFile( final OutputStream out ) throws IOException {

    String path = getRequestParameters().getStringParameter( MethodParams.PATH, "" );
    IUserContentAccess access = CdeEnvironment.getUserContentAccess();

    if ( access.fileExists( path ) ) {
      setResponseHeaders( "text/plain", NO_CACHE_DURATION, null );
      writeOut( out, IOUtils.toString( access.getFileInputStream( path ) ) );
    } else {
      writeOut( out, "No file found in given path " + path );
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void createFolder( OutputStream out ) throws PentahoAccessControlException, IOException {

    String path = getRequestParameters().getStringParameter( MethodParams.PATH, null );
    IUserContentAccess access = CdeEnvironment.getUserContentAccess();

    if ( access.fileExists( path ) ) {
      writeOut( out, "already exists: " + path );
    } else {
      if ( access.createFolder( path, false ) ) {
        writeOut( out, path + "created ok" );
      } else {
        writeOut( out, "error creating folder " + path );
      }
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void deleteFile( OutputStream out ) throws PentahoAccessControlException, IOException {

    String path = getRequestParameters().getStringParameter( MethodParams.PATH, null );

    IUserContentAccess access = CdeEnvironment.getUserContentAccess();
    if ( access.hasAccess( path, FileAccess.DELETE ) && access.deleteFile( path ) ) {
      writeOut( out, "file  " + path + " removed ok" );
    } else {
      writeOut( out, "Error removing " + path );
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void writeFile( OutputStream out ) throws PentahoAccessControlException, IOException {

    IParameterProvider requestParams = getRequestParameters();
    String path = requestParams.getStringParameter( MethodParams.PATH, null );
    String contents = requestParams.getStringParameter( MethodParams.DATA, null );
    boolean createNew = requestParams.hasParameter( "createNew" );

    IUserContentAccess access = CdeEnvironment.getUserContentAccess();

    if ( access.hasAccess( path, FileAccess.WRITE )
      || ( createNew && access.hasAccess( FilenameUtils.getFullPath( path ), FileAccess.WRITE ) ) ) {

      if ( access.saveFile( path, new ByteArrayInputStream( contents.getBytes( ENCODING ) ) ) ) {
        // saved ok
        writeOut( out, "file '" + path + "' saved ok" );
      } else {
        // error
        logger.error( "writeFile: failed saving " + path );
        writeOut( out, "error saving file " + path );
      }

    } else {
      logger.error( "writeFile: no permissions to write file " + path );
      writeOut( out, "no permissions to write file " + path );
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void canEdit( OutputStream out ) throws IOException {

    String path = getRequestParameters().getStringParameter( MethodParams.PATH, null );

    writeOut( out, String.valueOf( CdeEnvironment.getUserContentAccess().hasAccess( path, FileAccess.WRITE ) ) );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void extEditor( final OutputStream out ) throws IOException {

    IReadAccess access = CdeEnvironment.getPluginSystemReader();

    if ( access.fileExists( EXTERNAL_EDITOR_PAGE ) ) {
      writeOut( out, IOUtils.toString( access.getFileInputStream( EXTERNAL_EDITOR_PAGE ) ) );
    } else {
      writeOut( out, "no external editor found in " + Utils.joinPath( getPluginDir(), EXTERNAL_EDITOR_PAGE ) );
    }
  }

  // External Editor ^
  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void componentEditor( final OutputStream out ) throws IOException {

    IReadAccess access = CdeEnvironment.getPluginSystemReader();

    if ( access.fileExists( COMPONENT_EDITOR_PAGE ) ) {
      writeOut( out, IOUtils.toString( access.getFileInputStream( COMPONENT_EDITOR_PAGE ) ) );
    } else {
      writeOut( out, "no external editor found in " + Utils.joinPath( getPluginDir(), COMPONENT_EDITOR_PAGE ) );
    }
  }

  private String getWcdfRelativePath( final IParameterProvider pathParams ) {
    final String path = "/"
      + pathParams.getStringParameter( MethodParams.SOLUTION, null )
      + "/" + pathParams.getStringParameter( MethodParams.PATH, null )
      + "/" + pathParams.getStringParameter( MethodParams.FILE, null );

    return path.replaceAll( "//+", "/" );
  }

  private void setCacheControl() {
    final IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class, null );
    final String maxAge = resLoader.getPluginSetting( this.getClass(), "max-age" );
    final HttpServletResponse response = getResponse();
    if ( maxAge != null && response != null ) {
      response.setHeader( "Cache-Control", "max-age=" + maxAge );
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void checkVersion( OutputStream out ) throws IOException,
    JSONException {
    VersionChecker versionChecker = new CdeVersionChecker(
      CdeSettings.getSettings() );
    writeOut( out, versionChecker.checkVersion() );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void getVersion( OutputStream out ) throws IOException, JSONException {
    VersionChecker versionChecker = new CdeVersionChecker(
      CdeSettings.getSettings() );
    writeOut( out, versionChecker.getVersion() );
  }

  private String getScheme( IParameterProvider pathParams ) {
    try {
      ServletRequest req = (ServletRequest) ( pathParams
        .getParameter( "httprequest" ) );
      return req.getScheme();
    } catch ( Exception e ) {
      return "http";
    }
  }

  private CdfRunJsDashboardWriteResult loadDashboard()
    throws ThingWriteException {
    IParameterProvider pathParams = parameterProviders.get( "path" );
    IParameterProvider requestParams = parameterProviders.get( "request" );

    String scheme = ( requestParams.hasParameter( "inferScheme" ) && requestParams
      .getParameter( "inferScheme" ).equals( "false" ) ) ? ""
      : getScheme( pathParams );

    String absRoot = requestParams.getStringParameter( "root", "" );

    boolean absolute = requestParams.hasParameter( "absolute" )
      && requestParams.getParameter( "absolute" ).equals( "true" );

    boolean bypassCacheRead = requestParams.hasParameter( "bypassCache" )
      && requestParams.getParameter( "bypassCache" ).equals( "true" );

    boolean debug = requestParams.hasParameter( "debug" )
      && requestParams.getParameter( "debug" ).equals( "true" );

    String wcdfFilePath = getWcdfRelativePath( requestParams );

    String style = requestParams.getStringParameter( "style", "" );

    CdfRunJsDashboardWriteOptions options = new CdfRunJsDashboardWriteOptions(
      absolute, debug, absRoot, scheme );

    return DashboardManager.getInstance().getDashboardCdfRunJs(
      wcdfFilePath, options, bypassCacheRead, style );
  }

  private String getCdfContext() {
    InterPluginCall cdfContext = new InterPluginCall( InterPluginCall.CDF,
      "Context" );
    cdfContext.setRequest( getRequest() );
    cdfContext.setRequestParameters( getRequestParameters() );
    return cdfContext.callInPluginClassLoader();
  }

  public IBasicFile[] getFileList( String dir, final String fileExtensions, String permission ) {

    ArrayList<String> extensionsList = new ArrayList<String>();
    String[] extensions = StringUtils.split( fileExtensions, "." );
    if ( extensions != null ) {
      for ( String extension : extensions ) {
        // For some reason, in 4.5 filebased rep started to report a leading dot in extensions
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

    GenericFileAndDirectoryFilter fileAndDirFilter = new GenericFileAndDirectoryFilter( fileFilter );

    // folder filtering ( see settings.xml ) will only occur for non-admin users
    if ( !isAdministrator() ) {
      fileAndDirFilter.setDirectories( CdeSettings.getFilePickerHiddenFolderPaths( CdeSettings.FolderType.REPO ) );
      fileAndDirFilter.setFilterType( GenericFileAndDirectoryFilter.FilterType.FILTER_OUT ); // act as a black-list
    }

    List<IBasicFile> fileList =
      CdeEnvironment.getContentAccessFactory().getUserContentAccess( "/" ).listFiles( dir, fileAndDirFilter, 1, true );

    if ( fileList != null && fileList.size() > 0 ) {
      return fileList.toArray( new IBasicFile[ fileList.size() ] );
    }

    return new IBasicFile[] { };
  }

  private static HashMap<String, Object> toHashMap( IParameterProvider parameterProvider ) {

    HashMap<String, Object> map = new HashMap<String, Object>();

    if ( parameterProvider == null ) {
      return map;
    }

    Iterator<String> names = parameterProvider.getParameterNames();

    while ( names.hasNext() ) {
      String name = names.next();
      Object value = parameterProvider.getParameter( name );
      map.put( name, value );
    }

    return map;
  }

  public static String getPluginDir() {
    return CdeEnvironment.getSystemDir() + "/" + CdeEnvironment.getPluginId() + "/";
  }

  // this should be in JsonUtils, when the custom json lib gets removed
  private void buildJsonResult( OutputStream out, Boolean sucess, Object result ) throws JSONException {
    JSONObject jsonResult = new JSONObject();

    jsonResult.put( "status", sucess.toString() );
    if ( result != null ) {
      jsonResult.put( "result", result );
    }
    PrintWriter pw = null;
    try {
      pw = new PrintWriter( out );
      pw.print( jsonResult.toString( 2 ) );
      pw.flush();
    } finally {
      IOUtils.closeQuietly( pw );
    }

  }

  /**
   * checks is the current user is administrator
   *
   * @return true if the current user is administrator, false otherwise
   */
  protected boolean isAdministrator() {
    SecurityParameterProvider securityParams = new SecurityParameterProvider( userSession );
    return Boolean.valueOf( (String) securityParams.getParameter( "principalAdministrator" ) );
  }

}
