/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpf;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONException;
import org.pentaho.platform.api.engine.IParameterProvider;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
import org.springframework.security.GrantedAuthorityImpl;

import pt.webdetails.cpf.annotations.AccessLevel;
import pt.webdetails.cpf.annotations.Exposed;

/**
 *
 * @author pdpi
 */
public abstract class SimpleContentGenerator extends BaseContentGenerator {

    private static final long serialVersionUID = 1L;
    protected Log logger = LogFactory.getLog(this.getClass());
   
    protected static final String ENCODING = PluginSettings.ENCODING;
    protected static String getEncoding() { return ENCODING; }
    
    @Override
    public void createContent() {
      IParameterProvider pathParams = parameterProviders.get("path");
  
      try {
  
        final OutputStream out = getResponseOutputStream("text/html");
        final Class<?>[] params = getCGMethodParams();
        String path = pathParams.getStringParameter("path", null);
        String[] pathSections = StringUtils.split(path, "/");
  
        
        if(pathSections == null || pathSections.length == 0){
          String method = getDefaultPath(path);
          if(!StringUtils.isEmpty(method)){
            logger.warn("No method supplied, redirecting.");
            redirect(method);
          }else {
            logger.error("No method supplied.");
          }
        }
        else {
        
        final String methodName = StringUtils.lowerCase(pathSections[0]); 
          try {
            final Method method = this.getClass().getMethod(methodName, params);
            invokeMethod(out, methodName, method);
  
          } catch (NoSuchMethodException e) {
            logger.warn("couldn't locate method: " + methodName);
          } catch (InvocationTargetException e) {
            //get to the cause and rethrow properly
            Throwable target = e.getTargetException();
            if (!e.equals(target)) //just in case
            {//get to the real cause
              while (target != null && target instanceof InvocationTargetException)
              {
                target = ((InvocationTargetException) target).getTargetException();
              }
            }
            logger.error(InvocationTargetException.class.getName(), target);
  
          } catch (IllegalAccessException e) {
            logger.warn(e.toString());
  
          } catch (IllegalArgumentException e) {
            logger.error(e.toString());
          }
          
        }
      } catch (SecurityException e) {
        logger.warn(e.toString());
      } catch (IOException e) {
        logger.error(e.toString());
      }
    }
    
    protected Class<?>[] getCGMethodParams(){
     return new Class<?>[]{ OutputStream.class };
    }
    
    protected OutputStream getResponseOutputStream(final String mimeType) throws IOException {
      ServletResponse resp = getResponse();
      resp.setContentType(mimeType);
      return resp.getOutputStream();
    }

    protected ServletRequest getRequest(){
      return (ServletRequest) parameterProviders.get("path").getParameter("httprequest");
    }
    
    protected HttpServletResponse getResponse(){
      return (HttpServletResponse) parameterProviders.get("path").getParameter("httpresponse");
    }
    
    protected IParameterProvider getRequestParameters(){
      return parameterProviders.get("request");
    }
    protected IParameterProvider getPathParameters(){
      return parameterProviders.get("path");
    }
    
    protected String getDefaultPath(String path){
      return null;
    }
    
    protected boolean canAccessMethod(Method method){
      Exposed exposed = method.getAnnotation(Exposed.class);
      if (exposed != null) {
        
        AccessLevel accessLevel = exposed.accessLevel();
        if(accessLevel != null) {
          
          boolean accessible = false;
          switch (accessLevel) {
            case ADMIN:
              accessible = SecurityHelper.isPentahoAdministrator(PentahoSessionHolder.getSession());
              break;
            case ROLE:
              String role = exposed.role();
              if (!StringUtils.isEmpty(role)) {
                accessible = SecurityHelper.isGranted(PentahoSessionHolder.getSession(), new GrantedAuthorityImpl(role));
              }
              break;
            case PUBLIC:
              accessible = true;
              break;
            default:
              logger.error("Unsupported AccessLevel " + accessLevel);
          }
          
          return accessible;
        }
        
      }
      return false;
    }

    protected boolean invokeMethod(final OutputStream out, final String methodName, final Method method) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException {
      
      if(canAccessMethod(method)){
       method.invoke(this, out);
       return true;
      }
      logger.error("Method " + methodName + " not exposed or user does not have required permissions.");
      getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
      return false;
    }
    
    protected void redirect(String method){
      
      final HttpServletResponse response = (HttpServletResponse) parameterProviders.get("path").getParameter("httpresponse");
      
      if (response == null)
      {
        logger.error("response not found");
        return;
      }
      try {
        response.sendRedirect(method);
      } catch (IOException e) {
        logger.error("could not redirect", e);
      }
    }
    
    /**
     * Write to OutputStream using 
     * @param out
     * @param contents
     * @throws IOException
     */
    protected void writeOut(OutputStream out, String contents) throws IOException {
      StringBuffer buf = new StringBuffer(contents);
      IOUtils.write(buf, out, getEncoding());
    }
    
    protected void writeOut(OutputStream out, JsonSerializable contents) throws IOException, JSONException {
      StringBuffer buf = new StringBuffer(contents.toJSON().toString());
      IOUtils.write(buf, out, getEncoding());
    }

    @Override
    public Log getLogger() {
        return logger;
    }
}
