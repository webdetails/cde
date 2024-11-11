/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cdf.dd.api;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.owasp.encoder.Encode;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.util.logging.SimpleLogger;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.CdeConstants.DashboardSupportedTypes;
import pt.webdetails.cdf.dd.CdeConstants.MethodParams;
import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.DashboardManager;
import pt.webdetails.cdf.dd.ICdeEnvironment;
import pt.webdetails.cdf.dd.InterPluginBroker;
import pt.webdetails.cdf.dd.Messages;
import pt.webdetails.cdf.dd.MetaModelManager;
import pt.webdetails.cdf.dd.editor.DashboardEditor;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.CorsUtil;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.audit.CpfAuditHelper;
import pt.webdetails.cpf.localization.MessageBundlesHelper;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.CharsetHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.Map;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static pt.webdetails.cpf.utils.MimeTypes.JAVASCRIPT;

@Path( "pentaho-cdf-dd/api/renderer" )
public class RenderApi {

  private static final Log logger = LogFactory.getLog( RenderApi.class );
  protected ICdeEnvironment privateEnviroment;
  private DashboardManager dashboardManager;

  @GET
  @Path( "/getComponentDefinitions" )
  @Produces( JAVASCRIPT )
  public String getComponentDefinitions(
    @QueryParam( MethodParams.SUPPORTS ) @DefaultValue( DashboardSupportedTypes.LEGACY ) String supports,
    @Context HttpServletResponse response ) {

    // Get and output the definitions
    if ( !StringUtils.isEmpty( supports ) && supports.equals( DashboardSupportedTypes.AMD ) ) {
      return MetaModelManager.getInstance().getAmdJsDefinition();
    } else {
      return MetaModelManager.getInstance().getJsDefinition();
    }
  }

  @GET
  @Path( "/getContent" )
  @Produces( JAVASCRIPT )
  public String getContent( @QueryParam( MethodParams.SOLUTION ) @DefaultValue( "" ) String solution,
                            @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
                            @QueryParam( MethodParams.FILE ) @DefaultValue( "" ) String file,
                            @QueryParam( MethodParams.INFERSCHEME ) @DefaultValue( "false" ) boolean inferScheme,
                            @QueryParam( MethodParams.ROOT ) @DefaultValue( "" ) String root,
                            @QueryParam( MethodParams.ABSOLUTE ) @DefaultValue( "false" ) boolean absolute,
                            @QueryParam( MethodParams.BYPASSCACHE ) @DefaultValue( "false" ) boolean bypassCache,
                            @QueryParam( MethodParams.DEBUG ) @DefaultValue( "false" ) boolean debug,
                            @QueryParam( MethodParams.SCHEME ) @DefaultValue( "" ) String scheme,
                            @Context HttpServletRequest request,
                            @Context HttpServletResponse response ) throws ThingWriteException {

    solution = decodeAndEscape( solution );
    path = decodeAndEscape( path );
    file = decodeAndEscape( file );
    scheme = decodeAndEscape( scheme );
    root = processRoot( root );

    String schemeToUse = "";
    if ( !inferScheme ) {
      schemeToUse = StringUtils.isEmpty( scheme ) ? request.getScheme() : scheme;
    }

    String filePath = getWcdfRelativePath( solution, path, file );

    CdfRunJsDashboardWriteResult dashboardWrite = this.loadDashboard(
      filePath, schemeToUse, root, absolute, bypassCache, debug, null );

    return dashboardWrite.getContent();
  }

  @GET
  @Path( "/getHeaders" )
  @Produces( TEXT_PLAIN )
  public String getHeaders( @QueryParam( MethodParams.SOLUTION ) @DefaultValue( "" ) String solution,
                            @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
                            @QueryParam( MethodParams.FILE ) @DefaultValue( "" ) String file,
                            @QueryParam( MethodParams.INFERSCHEME ) @DefaultValue( "false" ) boolean inferScheme,
                            @QueryParam( MethodParams.ROOT ) @DefaultValue( "" ) String root,
                            @QueryParam( MethodParams.ABSOLUTE ) @DefaultValue( "true" ) boolean absolute,
                            @QueryParam( MethodParams.BYPASSCACHE ) @DefaultValue( "false" ) boolean bypassCache,
                            @QueryParam( MethodParams.DEBUG ) @DefaultValue( "false" ) boolean debug,
                            @QueryParam( MethodParams.SCHEME ) @DefaultValue( "" ) String scheme,
                            @Context HttpServletRequest request,
                            @Context HttpServletResponse response ) throws ThingWriteException {

    solution = decodeAndEscape( solution );
    path = decodeAndEscape( path );
    file = decodeAndEscape( file );
    scheme = decodeAndEscape( scheme );
    root = processRoot( root );

    String schemeToUse = "";
    if ( !inferScheme ) {
      schemeToUse = StringUtils.isEmpty( scheme ) ? request.getScheme() : scheme;
    }

    String filePath = getWcdfRelativePath( solution, path, file );

    CdfRunJsDashboardWriteResult dashboardWrite = this.loadDashboard(
      filePath, schemeToUse, root, absolute, bypassCache, debug, null );

    return dashboardWrite.getHeader();
  }

  @GET
  @Path( "/render" )
  @Produces( TEXT_HTML )
  public String render( @QueryParam( MethodParams.SOLUTION ) @DefaultValue( "" ) String solution,
                        @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
                        @QueryParam( MethodParams.FILE ) @DefaultValue( "" ) String file,
                        @QueryParam( MethodParams.INFERSCHEME ) @DefaultValue( "false" ) boolean inferScheme,
                        @QueryParam( MethodParams.ROOT ) @DefaultValue( "" ) String root,
                        @QueryParam( MethodParams.ABSOLUTE ) @DefaultValue( "true" ) boolean absolute,
                        @QueryParam( MethodParams.BYPASSCACHE ) @DefaultValue( "false" ) boolean bypassCache,
                        @QueryParam( MethodParams.DEBUG ) @DefaultValue( "false" ) boolean debug,
                        @QueryParam( MethodParams.SCHEME ) @DefaultValue( "" ) String scheme,
                        @QueryParam( MethodParams.VIEW ) @DefaultValue( "" ) String view,
                        @QueryParam( MethodParams.STYLE ) @DefaultValue( "" ) String style,
                        @Context HttpServletRequest request ) {

    solution = decodeAndEscape( solution );
    path = decodeAndEscape( path );
    file = decodeAndEscape( file );
    scheme = decodeAndEscape( scheme );
    view = decodeAndEscape( view );
    style = decodeAndEscape( style );
    root = processRoot( root );

    String schemeToUse = "";
    if ( !inferScheme ) {
      schemeToUse = StringUtils.isEmpty( scheme ) ? request.getScheme() : scheme;
    }

    String filePath = getWcdfRelativePath( solution, path, file );
    if ( StringUtils.isEmpty( filePath ) ) {
      return "No path provided.";
    }

    IReadAccess readAccess = Utils.getSystemOrUserReadAccess( filePath );
    if ( readAccess == null ) {
      return Messages.getString( "XmlStructure.ERROR_011_READ_WRITE_ACCESS_EXCEPTION" );
    }

    long start = System.currentTimeMillis();
    long end;
    ILogger iLogger = getAuditLogger();
    IParameterProvider requestParams = getParameterProvider( request.getParameterMap() );

    UUID uuid = CpfAuditHelper.startAudit(
      getPluginName(), filePath, getObjectName(), this.getPentahoSession(), iLogger, requestParams );

    try {
      logger.info( "[Timing] CDE Starting Dashboard Rendering" );
      CdfRunJsDashboardWriteResult dashboard =
        loadDashboard( filePath, schemeToUse, root, absolute, bypassCache, debug, style );

      DashboardWcdfDescriptor dashboardWcdf = DashboardWcdfDescriptor.load( filePath );

      String context = "";
      if ( dashboardWcdf != null && !dashboardWcdf.isRequire() ) {
        context = getCdfContext( filePath, "", view, requestParams );
      }

      String result = dashboard.render( context, getCdfRequireConfig( filePath, requestParams ) );

      //i18n token replacement
      if ( !StringUtils.isEmpty( result ) && dashboardWcdf != null && !dashboardWcdf.isRequire() ) {
        String msgDir = FilenameUtils.getPath( FilenameUtils.separatorsToUnix( filePath ) );
        msgDir = msgDir.startsWith( Util.SEPARATOR ) ? msgDir : Util.SEPARATOR + msgDir;

        result = new MessageBundlesHelper(
          msgDir,
          Utils.getAppropriateReadAccess( msgDir ),
          CdeEnvironment.getPluginSystemWriter(),
          getEnv().getLocale(),
          getEnv().getExtApi().getPluginStaticBaseUrl() )
          .replaceParameters( result, null );
      }

      logger.info( "[Timing] CDE Finished Dashboard Rendering: " + Utils.ellapsedSeconds( start ) + "s" );

      end = System.currentTimeMillis();
      CpfAuditHelper.endAudit(
        getPluginName(), filePath, getObjectName(), this.getPentahoSession(), iLogger, start, uuid, end );

      return result;
    } catch ( Exception ex ) { //TODO: better error handling?
      String msg = "Could not load dashboard: " + ex.getMessage();
      logger.error( msg, ex );

      end = System.currentTimeMillis();
      CpfAuditHelper.endAudit( getPluginName(), filePath, getObjectName(),
        this.getPentahoSession(), iLogger, start, uuid, end );

      return msg;
    }
  }

  @GET
  @Path( "/getDashboard" )
  @Produces( TEXT_HTML )
  public String getDashboard( @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
                              @QueryParam( MethodParams.INFERSCHEME ) @DefaultValue( "false" ) boolean inferScheme,
                              @QueryParam( MethodParams.ROOT ) @DefaultValue( "" ) String root,
                              @QueryParam( MethodParams.ABSOLUTE ) @DefaultValue( "true" ) boolean absolute,
                              @QueryParam( MethodParams.BYPASSCACHE ) @DefaultValue( "false" ) boolean bypassCache,
                              @QueryParam( MethodParams.DEBUG ) @DefaultValue( "false" ) boolean debug,
                              @QueryParam( MethodParams.SCHEME ) @DefaultValue( "" ) String scheme,
                              @QueryParam( MethodParams.VIEW ) @DefaultValue( "" ) String view,
                              @QueryParam( MethodParams.STYLE ) @DefaultValue( "" ) String style,
                              @QueryParam( MethodParams.ALIAS ) @DefaultValue( "" ) String alias,
                              @Context HttpServletRequest request ) {


    path = decodeAndEscape( path );
    scheme = decodeAndEscape( scheme );
    style = decodeAndEscape( style );
    alias = decodeAndEscape( alias );
    root = processRoot( root );

    final String schemeToUse;
    if ( !inferScheme ) {
      schemeToUse = StringUtils.isEmpty( scheme ) ? request.getScheme() : scheme;
    } else {
      schemeToUse = "";
    }

    if ( StringUtils.isEmpty( path ) ) {
      logger.warn( "No path provided." );
      return "No path provided.";
    }

    IReadAccess readAccess = Utils.getSystemOrUserReadAccess( path );
    if ( readAccess == null ) {
      logger.warn( "Access Denied or File Not Found." );
      return "Access Denied or File Not Found.";
    }

    long start = System.currentTimeMillis();
    long end;
    ILogger iLogger = getAuditLogger();
    IParameterProvider requestParams = getParameterProvider( request.getParameterMap() );

    UUID uuid = CpfAuditHelper.startAudit(
      getPluginName(), path, getObjectName(), this.getPentahoSession(), iLogger, requestParams );

    try {
      logger.info( "[Timing] CDE Starting To Generate Dashboard AMD Module" );
      String config = getCdfRequireConfig( path, requestParams );
      CdfRunJsDashboardWriteResult dashboard = getDashboardModule(
        path, schemeToUse, root, absolute, bypassCache, debug, style, alias );

      //TODO: how to process i18n for a required dashboard
      //i18n token replacement

      logger.info( "[Timing] CDE Finished Generating Dashboard AMD Module: " + Utils.ellapsedSeconds( start ) + "s" );

      end = System.currentTimeMillis();
      CpfAuditHelper.endAudit( getPluginName(), path, getObjectName(),
        this.getPentahoSession(), iLogger, start, uuid, end );

      return dashboard.getContent( config );
    } catch ( Exception ex ) { //TODO: better error handling?
      String msg = "Could not load dashboard: " + ex.getMessage();
      logger.error( msg, ex );

      end = System.currentTimeMillis();
      CpfAuditHelper.endAudit( getPluginName(), path, getObjectName(),
        this.getPentahoSession(), iLogger, start, uuid, end );

      return msg;
    }
  }

  @GET
  @Path( "/getDashboardParameters" )
  @Produces( APPLICATION_JSON )
  public String getDashboardParameters(
    @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
    @QueryParam( MethodParams.BYPASSCACHE ) @DefaultValue( "false" ) boolean bypassCache,
    @QueryParam( MethodParams.ALLPARAMS ) @DefaultValue( "false" ) boolean all,
    @Context HttpServletRequest servletRequest,
    @Context HttpServletResponse servletResponse ) {


    path = decodeAndEscape( path );

    servletResponse.setContentType( APPLICATION_JSON );
    servletResponse.setCharacterEncoding( CharsetHelper.getEncoding() );
    setCorsHeaders( servletRequest, servletResponse );

    if ( StringUtils.isEmpty( path ) ) {
      logger.warn( "No path provided." );
      return "No path provided.";
    }

    if ( !hasSystemOrUserReadAccess( path ) ) {
      logger.warn( "Access Denied or File Not Found." );
      return "Access Denied or File Not Found.";
    }

    try {
      return getDashboardManager().getDashboardParameters( path, bypassCache, all );
    } catch ( Exception ex ) { //TODO: better error handling?
      String msg = "Could not load dashboard parameters: " + ex.getMessage();
      logger.error( msg, ex );
      return msg;
    }
  }

  @GET
  @Path( "/getDashboardDatasources" )
  @Produces( APPLICATION_JSON )
  public String getDashboardDatasources(
    @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
    @QueryParam( MethodParams.BYPASSCACHE ) @DefaultValue( "false" ) boolean bypassCache,
    @Context HttpServletRequest servletRequest,
    @Context HttpServletResponse servletResponse ) throws JSONException {

    path = decodeAndEscape( path );

    servletResponse.setContentType( APPLICATION_JSON );
    servletResponse.setCharacterEncoding( CharsetHelper.getEncoding() );
    setCorsHeaders( servletRequest, servletResponse );

    if ( StringUtils.isEmpty( path ) ) {
      logger.warn( "No path provided." );
      return JsonUtils.getJsonResult( false, "No path provided" );
    }

    if ( !hasSystemOrUserReadAccess( path ) ) {
      logger.warn( "Access Denied or File Not Found." );
      return JsonUtils.getJsonResult( false, "Access Denied or File Not Found." );
    }

    try {
      return getDashboardManager().getDashboardDataSources( path, bypassCache );
    } catch ( Exception ex ) { //TODO: better error handling?
      String msg = "Could not load dashboard datasources: " + ex.getMessage();
      logger.error( msg, ex );

      return JsonUtils.getJsonResult( false, msg );
    }
  }

  @GET
  @Path( "/edit" )
  @Produces( TEXT_HTML )
  public String edit(
    @QueryParam( MethodParams.SOLUTION ) @DefaultValue( "" ) String solution,
    @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
    @QueryParam( MethodParams.FILE ) @DefaultValue( "" ) String file,
    @QueryParam( MethodParams.DEBUG ) @DefaultValue( "false" ) boolean debug,
    @QueryParam( "isDefault" ) @DefaultValue( "false" ) boolean isDefault,
    @Context HttpServletRequest request,
    @Context HttpServletResponse response ) throws Exception {

    solution = decodeAndEscape( solution );
    path = decodeAndEscape( path );
    file = decodeAndEscape( file );

    String wcdfPath = getWcdfRelativePath( solution, path, file );

    if ( !CdeEnvironment.canCreateContent() ) {
      return "This functionality is limited to users with permission 'Create Content'";
    } else if ( Utils.getSystemOrUserRWAccess( wcdfPath ) == null ) {
      return "Access Denied or file not found - " + wcdfPath; //TODO: keep html?
    }

    DashboardWcdfDescriptor descriptor = DashboardWcdfDescriptor.load( wcdfPath );

    return getEditor( wcdfPath, debug, request.getScheme(), isDefault, response, descriptor.isRequire() );
  }

  @GET
  @Path( "/new" )
  @Produces( TEXT_HTML )
  public String newDashboard( //TODO: change file to path; does new ever use this arg?
                              //      @QueryParam( MethodParams.SOLUTION ) @DefaultValue( "null" ) String solution,
                              @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
                              //      @QueryParam( MethodParams.FILE ) @DefaultValue( "null" ) String file,
                              @QueryParam( MethodParams.DEBUG ) @DefaultValue( "false" ) boolean debug,
                              @QueryParam( "isDefault" ) @DefaultValue( "false" ) boolean isDefault,
                              @Context HttpServletRequest request,
                              @Context HttpServletResponse response ) throws Exception {

    path = decodeAndEscape( path );

    if ( !CdeEnvironment.canCreateContent() ) {
      return "This functionality is limited to users with permission 'Create Content'";
    }

    return getEditor( path, debug, request.getScheme(), isDefault, response, true );
  }

  @GET
  @Path( "/listRenderers" )
  @Produces( APPLICATION_JSON )
  public String listRenderers() {
    return "{\"result\": [\""
      + DashboardWcdfDescriptor.DashboardRendererType.BLUEPRINT.getType()
      + "\",\""
      + DashboardWcdfDescriptor.DashboardRendererType.MOBILE.getType()
      + "\",\""
      + DashboardWcdfDescriptor.DashboardRendererType.BOOTSTRAP.getType()
      + "\"]}";
  }

  @GET
  @Path( "/refresh" )
  @Produces( TEXT_PLAIN )
  public String refresh( @Context HttpServletResponse servletResponse ) throws Exception {
    String msg = "Refreshed CDE Successfully";

    try {
      getDashboardManager().refreshAll();
    } catch ( Exception re ) {
      msg = "Method refresh failed while trying to execute.";

      logger.error( msg, re );
      servletResponse.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg );
    }

    return msg;
  }

  @GET
  @Path( "/cde-embed.js" )
  @Produces( JAVASCRIPT )
  public String getCdeEmbeddedContext( @Context HttpServletRequest servletRequest,
                                       @Context HttpServletResponse servletResponse ) throws Exception {
    return InterPluginBroker.getCdfEmbed( servletRequest.getProtocol(), servletRequest.getServerName(),
      servletRequest.getServerPort(), servletRequest.getSession().getMaxInactiveInterval(),
      servletRequest.getParameter( "locale" ), servletRequest.isSecure(),
      getParameterProvider( servletRequest.getParameterMap() ) );
  }

  private CdfRunJsDashboardWriteResult loadDashboard( String filePath, String scheme, String root, boolean absolute,
                                                      boolean bypassCache, boolean debug, String style )
    throws ThingWriteException {

    CdfRunJsDashboardWriteOptions options = new CdfRunJsDashboardWriteOptions(
      "", false, absolute, debug, root, scheme );

    return getDashboardManager().getDashboardCdfRunJs( filePath, options, bypassCache, style );
  }

  private CdfRunJsDashboardWriteResult getDashboardModule( String path, String scheme, String root,
                                                           boolean absolute, boolean bypassCache, boolean debug,
                                                           String style, String alias )
    throws ThingWriteException {

    final String dashboardAlias;
    if ( StringUtils.isEmpty( alias ) ) {
      dashboardAlias = FilenameUtils
        .removeExtension( FilenameUtils.getName( path ) ) + "_" + CdeConstants.DASHBOARD_ALIAS_TAG;
    } else {
      dashboardAlias = FilenameUtils.removeExtension( FilenameUtils.getName( path ) ) + "_" + alias;
    }

    CdfRunJsDashboardWriteOptions options =
      new CdfRunJsDashboardWriteOptions( dashboardAlias, true, absolute, debug, root, scheme );

    return getDashboardManager().getDashboardCdfRunJs( path, options, bypassCache, style );
  }

  public DashboardManager getDashboardManager() {
    return this.dashboardManager;
  }

  public void setDashboardManager( DashboardManager dashboardManager ) {
    this.dashboardManager = dashboardManager;
  }

  private String getWcdfRelativePath( String solution, String path, String file ) {
    //TODO: change to use path instead of file
    //    if ( !StringUtils.isEmpty( solution ) || !StringUtils.isEmpty( file ) ) {
    //      logger.warn( "Use of solution/path/file is deprecated. Use just the path argument" );
    return Util.joinPath( solution, path, file );
    //    }
    //    else return path;
    //    final String filePath = "/" + solution + "/" + path + "/" + file;
    //    return filePath.replaceAll( "//+", "/" );
  }

  private IPentahoSession getPentahoSession() {
    return PentahoSessionHolder.getSession();
  }

  private String getObjectName() {
    return RenderApi.class.getName();
  }

  private String getPluginName() {
    return CdeEnvironment.getPluginId();
  }

  private ILogger getAuditLogger() {
    return new SimpleLogger( RenderApi.class.getName() );
  }

  private IParameterProvider getParameterProvider( Map<String, String[]> params ) {
    return new SimpleParameterProvider( params );
  }

  private String getEditor( String path, boolean debug, String scheme, boolean isDefault,
                            HttpServletResponse response, boolean isRequire ) throws Exception {
    response.setContentType( TEXT_HTML );
    String result = DashboardEditor.getEditor( path, debug, scheme, isDefault, isRequire );

    //i18n token replacement
    if ( !StringUtils.isEmpty( result ) ) {

      /* cde editor's i18n is different; it continues on relying on pentaho-cdf-dd/lang/messages.properties */

      String msgDir = Util.SEPARATOR + "lang" + Util.SEPARATOR;
      result = new MessageBundlesHelper( msgDir, CdeEnvironment.getPluginSystemReader( null ),
        CdeEnvironment.getPluginSystemWriter(), getEnv().getLocale(),
        getEnv().getExtApi().getPluginStaticBaseUrl() ).replaceParameters( result, null );
    }

    return result;
  }

  private ICdeEnvironment getEnv() {
    if ( this.privateEnviroment != null ) {
      return this.privateEnviroment;
    }
    return CdeEngine.getEnv();
  }

  protected void setCorsHeaders( HttpServletRequest request, HttpServletResponse response ) {
    CorsUtil.getInstance().setCorsHeaders( request, response );
  }

  protected boolean hasSystemOrUserReadAccess( String path ) {
    return Utils.getSystemOrUserReadAccess( path ) != null;
  }

  protected String getCdfRequireConfig( String filePath, IParameterProvider requestParams ) throws Exception {
    return InterPluginBroker.getCdfRequireConfig( filePath, requestParams );
  }

  protected String getCdfContext( String filePath, String action, String view, IParameterProvider requestParams )
    throws Exception {
    return InterPluginBroker.getCdfContext( filePath, action, view, requestParams );
  }

  private static String decodeAndEscape( String path ) {
    final XSSHelper helper = XSSHelper.getInstance();

    return helper.escape( Utils.getURLDecoded( path ) );
  }

  private static String processRoot( String root ) {
    // Despite their "there's no valid use" justification for the deprecation,
    // in our case it seems safe to use. A protocol such as "http" is always prepended,
    // mitigating any attempts to execute code via a `javascript:` protocol.
    return StringUtils.isNotEmpty( root ) ? Encode.forUri( root ) : "";
  }
}
