/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.util;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 *
 * @author pedro
 */
@SuppressWarnings("deprecation")
public class Utils {

  private static Log logger = LogFactory.getLog(Utils.class);

  private static final String NEWLINE = System.getProperty("line.separator");
  
  private static String baseUrl = null;

  public static String getBaseUrl() {

    if (baseUrl == null) {
      try {
        // Note - this method is deprecated and returns different values in 3.6
        // and 3.7. Change this in future versions -- but not yet
        // getFullyQualifiedServerURL only available from 3.7
        //      URI uri = new URI(PentahoSystem.getApplicationContext().getFullyQualifiedServerURL());
        URI uri = new URI(PentahoSystem.getApplicationContext().getBaseUrl());
        baseUrl = uri.getPath();
        if (!baseUrl.endsWith("/")) {
          baseUrl += "/";
        }
      } catch (URISyntaxException ex) {
        logger.fatal("Error building BaseURL from " + PentahoSystem.getApplicationContext().getBaseUrl(), ex);
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
    } catch (URISyntaxException ex) {
      Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public static String getSolutionPath() {
    return StringUtils.replace(PentahoSystem.getApplicationContext().getSolutionPath(""), "\\", "/");
  }

  public static String getSolutionPath(String path) {
    return joinPath(getSolutionPath(), path);
  }
  
  public static String joinPath(String...paths){
    // TODO: dcleao Shouldn't this use File.separator
    return StringUtils.defaultString(StringUtils.join(paths, "/")).replaceAll("/+", "/");
  }

  public static boolean pathStartsWith(String fileName, String pathStart) {
    if (pathStart == null)
      return true;
    else if (fileName == null)
      return false;

    return FilenameUtils.getPath(fileName).startsWith(pathStart);
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

  public static List<File> listAllFiles(File dir, FilenameFilter filter)
  {
    ArrayList<File> results = new ArrayList<File>();
    collectAllFiles(results, dir, filter);
    return results;
  }

  private static void collectAllFiles(List<File> results, File dir, FilenameFilter filter)
  {
    File[] files = dir.listFiles();
    if (files != null)
    {
      for (File file : files)
      {
        if (file.isDirectory())
        {
          collectAllFiles(results, file, filter);
        }
        else if (filter == null || filter.accept(dir, file.getName()))
        {
          results.add(file);
        }
      }
    }
  }
  
  public static double ellapsedSeconds(Date dtStart)
  {
    return Math.round(10.0 * ((new Date().getTime() - dtStart.getTime()) / 1000.0)) / 10.0;
  }
  
  // Must start with a / and all \ are converted to / and has no duplicate /s.
  // When empty, returns empty
  public static String normalizeSolutionRelativePath(String path)
  {
    if(StringUtils.isEmpty(path)) { return ""; }
    return ("/" + path)
           .replaceAll("\\\\+", "/")
           .replaceAll("/+",    "/");
  }
  
  public static String getRelativePath(String targetPath, String basePath) 
  {
    return getRelativePath(targetPath, basePath, File.separator);
  }
  
  /**
   * Get the relative path from one file to another, specifying the directory separator. 
   * If one of the provided resources does not exist, it is assumed to be a file unless it ends with '/' or
   * '\'.
   * 
   * @param targetPath targetPath is calculated to this file
   * @param basePath basePath is calculated from this file
   * @param pathSeparator directory separator. The platform default is not assumed so that we can test Unix behaviour when running on Windows (for example)
   * @return
   * 
   * (adapted from http://stackoverflow.com/questions/204784/how-to-construct-a-relative-path-in-java-from-two-absolute-paths-or-urls "The Only 'Working' Solution (June 2010)" by Don)
   */
  public static String getRelativePath(String targetPath, String basePath, String pathSeparator) {

      // Normalize the paths
      String normalizedTargetPath = FilenameUtils.normalizeNoEndSeparator(targetPath);
      String normalizedBasePath   = FilenameUtils.normalizeNoEndSeparator(basePath);

      // Undo the changes to the separators made by normalization
      if (pathSeparator.equals("/")) {
          normalizedTargetPath = FilenameUtils.separatorsToUnix(normalizedTargetPath);
          normalizedBasePath = FilenameUtils.separatorsToUnix(normalizedBasePath);

      } else if (pathSeparator.equals("\\")) {
          normalizedTargetPath = FilenameUtils.separatorsToWindows(normalizedTargetPath);
          normalizedBasePath = FilenameUtils.separatorsToWindows(normalizedBasePath);

      } else {
          throw new IllegalArgumentException("Unrecognised dir separator '" + pathSeparator + "'");
      }

      String[] base = normalizedBasePath.split(Pattern.quote(pathSeparator));
      String[] target = normalizedTargetPath.split(Pattern.quote(pathSeparator));

      // First get all the common elements. Store them as a string,
      // and also count how many of them there are.
      StringBuilder common = new StringBuilder();

      int commonIndex = 0;
      while (commonIndex < target.length && commonIndex < base.length
              && target[commonIndex].equals(base[commonIndex])) {
          common.append(target[commonIndex]);
          common.append(pathSeparator);
          commonIndex++;
      }

      if (commonIndex == 0) {
          // No single common path element. This most
          // likely indicates differing drive letters, like C: and D:.
          // These paths cannot be relativized.
          throw new PathResolutionException("No common path element found for '" + normalizedTargetPath + "' and '" + normalizedBasePath + "'");
      }

      // The number of directories we have to backtrack depends on whether the base is a file or a dir
      // For example, the relative path from
      //
      // /foo/bar/baz/gg/ff to /foo/bar/baz
      // 
      // ".." if ff is a file
      // "../.." if ff is a directory
      //
      // The following is a heuristic to figure out if the base refers to a file or dir. It's not perfect, because
      // the resource referred to by this path may not actually exist, but it's the best I can do
      boolean baseIsFile = true;

      File baseResource = new File(normalizedBasePath);

      if (baseResource.exists()) {
          baseIsFile = baseResource.isFile();

      } else if (basePath.endsWith(pathSeparator)) {
          baseIsFile = false;
      }

      StringBuilder relative = new StringBuilder();

      if (base.length != commonIndex) {
          int numDirsUp = baseIsFile ? base.length - commonIndex - 1 : base.length - commonIndex;

          for (int i = 0; i < numDirsUp; i++) {
              relative.append(".." + pathSeparator);
          }
      }
      relative.append(normalizedTargetPath.substring(common.length()));
      return relative.toString();
  }


  public static class PathResolutionException extends RuntimeException 
  {
      PathResolutionException(String msg) 
      {
          super(msg);
      }
  }
}
