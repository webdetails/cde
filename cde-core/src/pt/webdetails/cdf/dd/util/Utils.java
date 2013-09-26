/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.DocumentException;
import org.xml.sax.EntityResolver;

import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.ICdeEnvironment;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;

/**
 *
 * @author pedro
 */
public class Utils {

  private static Log logger = LogFactory.getLog(Utils.class);

  private static final String NEWLINE = System.getProperty("line.separator");
  
  private static String baseUrl = null;

  public static String getBaseUrl() {  
    
	  if(baseUrl == null) {
		  
		  String appBaseUrl = "";
		  
		  try {	
			 
			  appBaseUrl = CdeEngine.getInstance().getEnvironment().getApplicationBaseUrl();
			  
			  // Note - this method is deprecated and returns different values in 3.6
			  // and 3.7. Change this in future versions -- but not yet
			  // getFullyQualifiedServerURL only available from 3.7
			  // URI uri = new URI(PentahoSystem.getApplicationContext().getFullyQualifiedServerURL());
			  URI uri = new URI(appBaseUrl);
	        
			  baseUrl = uri.getPath();
        
		      if(!baseUrl.endsWith("/")) {
		       	baseUrl += "/";
		      } 
		  } catch (URISyntaxException ex) {
			  logger.fatal("Error building BaseURL from " + appBaseUrl, ex);
		  }
	  }

	  return baseUrl;
  }
  
  public static String composeErrorMessage(String message, Exception cause)
  {
    String msg = "";
    if(StringUtils.isNotEmpty(message)) 
    {
      msg += message;
    }
    
    if(cause != null) 
    {
      if(msg.length() > 0) { msg += NEWLINE; }
      msg += cause.getMessage();
    }
    
    return msg;
  }
  
  public static void main(String[] args)
  {
    try
    {
      URI uri = new URI("http://127.0.0.1:8080/pentaho/");
      System.out.println(uri.getPath());
      uri = new URI("/pentaho/");
      System.out.println(uri.getPath());
      uri = new URI("http://127.0.0.1:8080/pentaho");
      System.out.println(uri.getPath());
      uri = new URI("/pentaho");
      System.out.println(uri.getPath());
    }
    catch (URISyntaxException ex)
    {
      Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public static String joinPath(String... paths) {
    // TODO: dcleao Shouldn't this use File.separator
    return StringUtils.defaultString(StringUtils.join(paths, "/")).replaceAll("/+", "/");
  }

  public static String toFirstLowerCase(String text)
  {
    if(text == null) { return null; }

    return text.length() > 1 ?
       text.substring(0, 1).toLowerCase() + text.substring(1) :
       text.toLowerCase();
  }

  public static String toFirstUpperCase(String text)
  {
    if(text == null) { return null; }

    return text.length() > 1 ?
       text.substring(0, 1).toUpperCase() + text.substring(1) :
       text.toUpperCase();
  }
  
  public static double ellapsedSeconds(Date dtStart) {
    return Math.round(100.0 * ((new Date().getTime() - dtStart.getTime()) / 1000.0)) / 100.0;
  }
  
  // Must start with a / and all \ are converted to / and has no duplicate /s.
  // When empty, returns empty
  public static String sanitizeSlashesInPath(String path)
  {
    if(StringUtils.isEmpty(path)) { return ""; }
    return ("/" + path)
           .replaceAll("\\\\+", "/")
           .replaceAll("/+",    "/");
  }

  public static class PathResolutionException extends RuntimeException {
      
	private static final long serialVersionUID = 1838386876452885975L;

	PathResolutionException(String msg){
          super(msg);
    }
  }
  
  public static String getNodeText(final String xpath, final Node node) {
    return getNodeText(xpath, node, null);
  }
  
  public static String getNodeText(final String xpath, final Node node, final String defaultValue) {
    if (node == null) {
      return defaultValue;
    }
    Node n = node.selectSingleNode(xpath);
    if (n == null) {
      return defaultValue;
    }
    return n.getText();
  }
  
  /**
   * Create a <code>Document</code> from the contents of a file.
   * 
   * @param path
   *          String containing the path to the file containing XML that will be
   *          used to create the Document.
   * @param resolver EntityResolver an instance of an EntityResolver that will resolve
   * any external URIs. See the docs on EntityResolver. null is an acceptable value.
   * @return <code>Document</code> initialized with the xml in
   *         <code>strXml</code>.
   * @throws DocumentException
   *           if the document isn't valid
   * @throws IOException 
   *           if the file doesn't exist
   */
  public static Document getDocFromFile(final IBasicFile file, final EntityResolver resolver) throws DocumentException, IOException {
    SAXReader reader = new SAXReader();
    if (resolver != null) {
      reader.setEntityResolver(resolver);
    }
    return reader.read(file.getContents());
  }
  
  public static Document getDocFromFile(final IReadAccess access, final String filePath, final EntityResolver resolver) throws DocumentException, IOException {    
	  return (access != null && filePath != null ? getDocFromFile(access.fetchFile(filePath), resolver) : null);
  }
  
  public static IReadAccess getAppropriateReadAccess(String resource){
	  return getAppropriateReadAccess(resource, null);
  }
  
  public static IReadAccess getAppropriateReadAccess(String resource, String basePath){
		
		if(StringUtils.isEmpty(resource)){
			return null;
		}
		
		ICdeEnvironment environment = CdeEngine.getInstance().getEnvironment();
		IContentAccessFactory factory = environment.getContentAccessFactory();
		
		String res = StringUtils.strip(resource.toLowerCase(), "/");
		
		if(res.startsWith(environment.getSystemDir() + "/")){
		
			res = StringUtils.strip(res, environment.getSystemDir() + "/");
			
			// system dir - this plugin
			if(res.startsWith(environment.getPluginId() + "/")){
				return factory.getPluginSystemReader(basePath);
				
			} else {
				// system dir - other plugin
				String pluginId = res.substring(0, resource.indexOf("/"));
				return factory.getOtherPluginSystemReader(pluginId, basePath);
			
			}
			
		} else if(res.startsWith(environment.getPluginRepositoryDir() + "/")) {
			
			// plugin repository dir
			return factory.getPluginRepositoryReader(basePath);
			
		} else {
			
			// one of two: already trimmed system resource (ex: 'resources/templates/1-empty-structure.cdfde')
			// or a user solution resource (ex: 'plugin-samples/pentaho-cdf-dd/styles/my-style.css')
			
			if(factory.getPluginSystemReader(basePath).fileExists(res)){
				return factory.getPluginSystemReader(basePath);
			} else {
				// user solution dir
				return factory.getUserContentAccess(basePath);
			}
		}
	}
  
  	public static IBasicFile getFileViaAppropriateReadAccess(String resource){
  		return getFileViaAppropriateReadAccess(resource, null);
  	}
  
  	public static IBasicFile getFileViaAppropriateReadAccess(String resource, String basePath){
  		if(StringUtils.isEmpty(resource)){
			return null;
		}
		
		ICdeEnvironment environment = CdeEngine.getInstance().getEnvironment();
		IContentAccessFactory factory = environment.getContentAccessFactory();
		
		String res = StringUtils.strip(resource.toLowerCase(), "/");
		
		if(res.startsWith(environment.getSystemDir() + "/")){
		
			res = StringUtils.strip(res, environment.getSystemDir() + "/");
			
			// system dir - this plugin
			if(res.startsWith(environment.getPluginId() + "/")){
				
				resource = resource.replaceFirst(environment.getSystemDir() + "/" + environment.getPluginId() + "/", "");
				
				return factory.getPluginSystemReader(basePath).fetchFile(resource);
				
			} else {
				// system dir - other plugin
				String pluginId = res.substring(0, resource.indexOf("/"));
				resource = resource.replaceFirst(environment.getSystemDir() + "/" +pluginId + "/", "");
				
				return factory.getOtherPluginSystemReader(pluginId, basePath).fetchFile(resource);
			}
			
		} else if(res.startsWith(environment.getPluginRepositoryDir() + "/")) {
			
			// plugin repository dir
			resource = resource.replaceFirst(environment.getPluginRepositoryDir() + "/", "");
			return factory.getPluginRepositoryReader(basePath).fetchFile(resource);
			
		} else {
			
			// one of two: already trimmed system resource (ex: 'resources/templates/1-empty-structure.cdfde')
			// or a user solution resource (ex: 'plugin-samples/pentaho-cdf-dd/styles/my-style.css')
			
			if (factory.getPluginSystemReader(basePath).fileExists(resource)){
				return factory.getPluginSystemReader(basePath).fetchFile(resource);
			
			} else if (factory.getUserContentAccess(basePath).fileExists(resource)){
				// user solution dir
				return factory.getUserContentAccess(basePath).fetchFile(resource);
			}
		}
		return null; //unable to determine appropriate way to fetch file
  	}
}
