/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cpf;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;


/**
 * Call to another pentaho plugin through its content generator.
 * Not thread safe.
 */
public class InterPluginCall implements Runnable, Callable<String> {

  public final static Plugin CDA = new Plugin("cda");
  public final static Plugin CDE = new Plugin("pentaho-cdf-dd");
  public final static Plugin CDC = new Plugin("cdc");
  public final static Plugin CDF = new Plugin("pentaho-cdf");
  
  private final static String DEFAULT_ENCODING = "UTF-8";
  
  public static class Plugin {
    
    private String name;
    private String title;
    
    public String getName() {
      return name;
    }

    public String getTitle() {
      return title;
    }
    
    public Plugin(String name, String title){
      this.name = name;
      this.title = title;
    }
    
    public Plugin(String id){
      this.name = id;
      this.title = id;
    }
    
  }
  
  private static final Log logger = LogFactory.getLog(InterPluginCall.class);

  private Plugin plugin;
  private String method;
  
  private Map<String, Object> requestParameters;
  private ServletResponse response;
  
  private OutputStream output;
  private IPentahoSession session;
  private IPluginManager pluginManager;
  
  private InterPluginCall(){
  }
  
  /**
   * Creates a new call.
   * @param plugin the plugin to call
   * @param method 
   */
  public InterPluginCall(Plugin plugin, String method){
    this();
    
    if(plugin == null) throw new IllegalArgumentException("Plugin must be specified");
    
    this.plugin = plugin;
    this.method = method;
    this.requestParameters = new HashMap<String, Object>();
  }
  
  public InterPluginCall(Plugin plugin, String method, Map<String, Object> params) {
    this(plugin, method);

    this.plugin = plugin;
    this.method = method;
    
    this.requestParameters.putAll(params);
  }
  
  /**
   * Put a request parameter 
   * @param name
   * @param value
   * @return this
   */
  public InterPluginCall putParameter(String name, Object value){
    requestParameters.put(name, value);
    return this;
  }
  
  public void run() {
    IOutputHandler outputHandler = new SimpleOutputHandler(getOutputStream(), false);
    IContentGenerator contentGenerator = getContentGenerator();
    try {
      contentGenerator.setSession(getSession());
      contentGenerator.setOutputHandler(outputHandler);
      contentGenerator.setParameterProviders(getParameterProviders());
      contentGenerator.createContent();
      
    } catch (Exception e) {
      logger.error("Failed to execute call to plugin: " + e.toString(), e);
    }
    
  }
  
  public String call() {
    setOutputStream(new ByteArrayOutputStream());
    run();
    try{
      return ((ByteArrayOutputStream) getOutputStream()).toString(getEncoding());
    }
    catch(UnsupportedEncodingException uee){
      logger.error("Charset " + getEncoding() + " not supported!!");
      return ((ByteArrayOutputStream) getOutputStream()).toString();
    }
  }

  public void runInPluginClassLoader(){
    getClassLoaderCaller().runInClassLoader(this);
  }

  public String callInPluginClassLoader() {
    try {
      return getClassLoaderCaller().callInClassLoader(this);
    } catch (Exception e) {
      logger.error(e);
      return null;
    }
  }

  public OutputStream getOutputStream(){
    if(output == null){
      output = new ByteArrayOutputStream();
    }
    return output;
  }

  public ServletResponse getResponse() {
    return response;
  }

  public void setResponse(ServletResponse response) {
    this.response = response;
  }
  
  public void setSession(IPentahoSession session) {
    this.session = session;
  }

  public void setOutputStream(OutputStream outputStream){
    this.output = outputStream;
  }
  
  public void setRequestParameters(Map<String, Object> parameters){
    this.requestParameters = parameters;
  }
  
  public void setRequestParameters(IParameterProvider requestParameterProvider){
    if(!requestParameters.isEmpty()) requestParameters.clear();
    
    for(@SuppressWarnings("unchecked")
    Iterator<String> params = requestParameterProvider.getParameterNames(); params.hasNext();){
      String parameterName = params.next();
      requestParameters.put(parameterName, requestParameterProvider.getParameter(parameterName));
    }
  }
  
  protected IPentahoSession getSession(){
    if(session == null){
      session = PentahoSessionHolder.getSession();
    }
    return session;
  }
  
  protected IParameterProvider getRequestParameterProvider(){
    return new SimpleParameterProvider(requestParameters);
  }
  
  protected ClassLoaderAwareCaller getClassLoaderCaller(){
    return new ClassLoaderAwareCaller(getPluginManager().getClassLoader(plugin.getTitle()));
  }

  protected IPluginManager getPluginManager() {
    if(pluginManager == null){
      pluginManager = PentahoSystem.get(IPluginManager.class, getSession());
    }
    return pluginManager;
  }

  protected IContentGenerator getContentGenerator(){
    try {
      return getPluginManager().getContentGenerator(plugin.getName(), getSession());
    } catch (Exception e) {
      logger.error("Failed to acquire " + plugin.getName() + " plugin: " + e.toString(), e);
      return null;
    }
  }
 

  private IParameterProvider getPathParameterProvider() {
    Map<String, Object> pathMap = new HashMap<String, Object>();
    pathMap.put("path", "/" + method);
    if (response != null) {
      pathMap.put("httpresponse", response);
    }
    IParameterProvider pathParams = new SimpleParameterProvider(pathMap);
    return pathParams;
  }
  
  private Map<String,IParameterProvider> getParameterProviders(){
    IParameterProvider requestParams = getRequestParameterProvider();
    IParameterProvider pathParams = getPathParameterProvider();
    Map<String, IParameterProvider> paramProvider = new HashMap<String, IParameterProvider>();
    paramProvider.put(IParameterProvider.SCOPE_REQUEST, requestParams);
    paramProvider.put("path", pathParams);
    return paramProvider;
  }

 private String getEncoding(){
   return DEFAULT_ENCODING;
 }
  

}
