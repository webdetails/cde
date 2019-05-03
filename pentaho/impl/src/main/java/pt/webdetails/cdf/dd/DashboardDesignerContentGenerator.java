/*!
 * Copyright 2002 - 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cdf.dd.api.RenderApi;
import pt.webdetails.cdf.dd.api.ResourcesApi;
import pt.webdetails.cpf.SimpleContentGenerator;
import pt.webdetails.cpf.audit.CpfAuditHelper;
import pt.webdetails.cpf.utils.MimeTypes;
import pt.webdetails.cpf.utils.PluginIOUtils;

import java.util.UUID;

public class DashboardDesignerContentGenerator extends SimpleContentGenerator {
  private static final Log logger = LogFactory.getLog( DashboardDesignerContentGenerator.class );

  private boolean edit = false;
  private boolean create = false;
  private boolean resource = false;

  private RenderApi renderer;
  private ICdeEnvironment environment;

  public DashboardDesignerContentGenerator() {
    super();
  }

  public RenderApi getRenderer() {
    return this.renderer;
  }

  public void setRenderer( RenderApi renderer ) {
    this.renderer = renderer;
  }

  public ICdeEnvironment getEnvironment() {
    return environment;
  }

  public void setEnvironment( ICdeEnvironment environment ) {
    this.environment = environment;
  }

  @Override
  public Log getLogger() {
    return logger;
  }

  @Override
  public void createContent() throws Exception {
    IParameterProvider requestParams = parameterProviders.get( MethodParams.REQUEST );
    IParameterProvider pathParams = parameterProviders.get( MethodParams.PATH );

    String solution = getRequestParameterAsString( MethodParams.SOLUTION, "" ),
      path = getRequestParameterAsString( MethodParams.PATH, "" ),
      file = getRequestParameterAsString( MethodParams.FILE, "" ),
      root = getRequestParameterAsString( MethodParams.ROOT, "" ),
      scheme = getRequestParameterAsString( MethodParams.SCHEME, "" );

    String view = getRequestParameterAsString( MethodParams.VIEW, "" );

    String filePath = getPathParameterAsString( MethodParams.PATH, "" );

    String auditPath = filePath.length() > 0 ? filePath : "newDashboard";

    boolean inferScheme = requestParams.hasParameter( MethodParams.INFER_SCHEME )
      && getRequestParameterAsString( MethodParams.INFER_SCHEME, "" ).equals( "false" );
    boolean absolute = requestParams.hasParameter( MethodParams.ABSOLUTE )
      && getRequestParameterAsString( MethodParams.ABSOLUTE, "" ).equals( "true" );
    boolean bypassCacheRead = requestParams.hasParameter( MethodParams.BYPASS_CACHE )
      && getRequestParameterAsString( MethodParams.BYPASS_CACHE, "" ).equals( "true" );
    boolean debug = requestParams.hasParameter( MethodParams.DEBUG )
      && getRequestParameterAsString( MethodParams.DEBUG, "" ).equals( "true" );

    String style = getRequestParameterAsString( MethodParams.STYLE, "" );

    long start = System.currentTimeMillis();
    UUID uuid = CpfAuditHelper.startAudit( getPluginName(), auditPath, getObjectName(),
      this.userSession, this, requestParams );

    if ( create ) {

      PluginIOUtils.writeOutAndFlush( getResponse().getOutputStream(),
          getRenderer().newDashboard( filePath, debug, true, getRequest(), getResponse() ) );

    } else if ( edit ) {

      PluginIOUtils.writeOutAndFlush( getResponse().getOutputStream(),
        getRenderer().edit( "", "", filePath, debug, true, getRequest(), getResponse() ) );

    } else if ( resource ) {
      // TODO review later if there is a viable solution to making resources being
      // called via cde resources rest api (pentaho/plugin/pentaho-cdf-dd/api/resources?resource=)
      // this has to take into consideration:
      // 1 - token replacement (see cde-core#CdfRunJsDashboardWriteContext.replaceTokens())
      // 2 - resources being called from other resources (ex: resource plugin-samples/template.css calls resource
      // images/button-contact-png)

      new ResourcesApi().getResource( pathParams.getStringParameter( MethodParams.COMMAND, "" ), getResponse(), null );

    } else {

      getResponse().setContentType( MimeTypes.HTML );

      PluginIOUtils.writeOutAndFlush( getResponse().getOutputStream(),
        getRenderer().render( "", "", filePath, inferScheme, root, absolute, bypassCacheRead, debug, scheme,
          view, style, getRequest() ) );
    }

    long end = System.currentTimeMillis();
    CpfAuditHelper.endAudit( getPluginName(), auditPath, getObjectName(), this.userSession,
      this, start, uuid, end );
  }

  @Override
  public String getPluginName() {
    return getEnvironment().getPluginId();
  }

  public String getObjectName() {
    return this.getClass().getName();
  }

  private class MethodParams {
    public static final String DEBUG = "debug";
    public static final String BYPASS_CACHE = "bypassCache";
    public static final String ROOT = "root";
    public static final String INFER_SCHEME = "inferScheme";
    public static final String ABSOLUTE = "absolute";
    public static final String SOLUTION = "solution";
    public static final String PATH = "path";
    public static final String FILE = "file";
    public static final String REQUEST = "request";
    public static final String VIEW = "view";
    public static final String VIEWID = "viewId";
    public static final String COMMAND = "cmd";
    public static final String STYLE = "style";
    public static final String SCHEME = "scheme";


    public static final String DATA = "data";
  }

  public boolean isEdit() {
    return edit;
  }

  public void setEdit( boolean edit ) {
    this.edit = edit;
  }

  public boolean isCreate() {
    return create;
  }

  public void setCreate( boolean create ) {
    this.create = create;
  }

  public boolean isResource() {
    return resource;
  }

  public void setResource( boolean resource ) {
    this.resource = resource;
  }

  public String getPluginDir() {
    return getEnvironment().getSystemDir() + "/" +  getEnvironment().getPluginId() + "/";
  }
}
