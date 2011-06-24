/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.webdetails.cdf.dd.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 *
 * @author pedro
 */
public class Utils {

  private static Log logger = LogFactory.getLog(Utils.class);
  private static String baseUrl = null;

  public static String getBaseUrl(){

    if(baseUrl == null){
      try
      {
        // Note - this method is deprecated and returns different values in 3.6
        // and 3.7. Change this in future versions -- but not yet
// getFullyQualifiedServerUeRL only available from 3.7
//      URI uri = new URI(PentahoSystem.getApplicationContext().getFullyQualifiedServerURL());
        URI uri = new URI(PentahoSystem.getApplicationContext().getBaseUrl());
        baseUrl = uri.getPath();
        if(!baseUrl.endsWith("/")){
          baseUrl+="/";
        }
      }
      catch (URISyntaxException ex)
      {
        logger.fatal("Error building BaseURL from " + PentahoSystem.getApplicationContext().getBaseUrl(),ex);
      }

    }

    return baseUrl;

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
  
  public static String getSolutionPath(){
    return StringUtils.replace(PentahoSystem.getApplicationContext().getSolutionPath("") , "\\", "/");
  }
  public static String getSolutionPath(String path){
    return joinPath(getSolutionPath(), path);
  }
  
  public static String joinPath(String...paths){
    return StringUtils.defaultString(StringUtils.join(paths, "/")).replaceAll("/+", "/");
  }

  public static boolean pathStartsWith(String fileName, String pathStart){
    if(pathStart == null) return true;
    else if(fileName == null) return false; 
    
    return FilenameUtils.getPath(fileName).startsWith(pathStart);
  }
  
}
