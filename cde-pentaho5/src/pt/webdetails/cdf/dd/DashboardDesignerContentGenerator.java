/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cdf.dd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;

import pt.webdetails.cdf.dd.api.RenderApi;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.SimpleContentGenerator;


public class DashboardDesignerContentGenerator extends SimpleContentGenerator {
  public static final String PLUGIN_PATH = CdeEnvironment.getSystemDir() + "/" + CdeEnvironment.getPluginId() + "/";
  private static final Log logger = LogFactory.getLog( DashboardDesignerContentGenerator.class );

  private boolean edit = false;
  private boolean create = false;

  public DashboardDesignerContentGenerator() {
    super();
  }

  @Override
  public Log getLogger() {
    return logger;
  }

  @Override
  public void createContent() throws Exception {
    IParameterProvider requestParams = parameterProviders.get( MethodParams.REQUEST );

    String solution = requestParams.getStringParameter( MethodParams.SOLUTION, "" ), path =
        requestParams.getStringParameter( MethodParams.PATH, "" ), file = requestParams.getStringParameter( MethodParams.FILE, "" );
    String root = requestParams.getStringParameter( MethodParams.ROOT, "" );

    boolean inferScheme =
        requestParams.hasParameter( MethodParams.INFER_SCHEME ) && requestParams.getParameter( MethodParams.INFER_SCHEME ).equals( "false" );
    boolean absolute =
        requestParams.hasParameter( MethodParams.ABSOLUTE ) && requestParams.getParameter( MethodParams.ABSOLUTE ).equals( "true" );
    boolean bypassCacheRead =
        requestParams.hasParameter( MethodParams.BYPASS_CACHE ) && requestParams.getParameter( MethodParams.BYPASS_CACHE ).equals( "true" );
    boolean debug = requestParams.hasParameter( MethodParams.DEBUG ) && requestParams.getParameter( MethodParams.DEBUG ).equals( "true" );

    RenderApi renderer = new RenderApi();

    if( create ) {
    	renderer.newDashboard( solution, path, file, debug, getRequest(), getResponse() );
    
    } else if( edit ) {
    	renderer.edit( solution, path, file, debug, getRequest(), getResponse() );
    
    } else {
    	renderer.render( solution, path, file, inferScheme, root, absolute, bypassCacheRead, debug, getRequest(), getResponse() );
    }
  }

  @Override
  public String getPluginName() {
    return CdeEnvironment.getPluginId();
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

    public static final String DATA = "data";
  }

	public boolean isEdit() {
		return edit;
	}
	
	public void setEdit(boolean edit) {
		this.edit = edit;
	}
	
	public boolean isCreate() {
		return create;
	}
	
	public void setCreate(boolean create) {
		this.create = create;
	}
}
