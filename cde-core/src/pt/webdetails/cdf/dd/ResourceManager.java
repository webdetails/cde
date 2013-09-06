/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.resources.IResourceLoader;


import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * User: pedro
 * Date: Dec 22, 2009
 * Time: 4:55:59 PM
 */
public class ResourceManager {


  public static ResourceManager instance;

  public static final String PLUGIN_DIR = CdeEngine.getInstance().getEnvironment().getRepositoryAccess().getSolutionPath(CdeEngine.getInstance().getEnvironment().getPluginUtils().getPluginDirectory().getPath());
  public static final String SOLUTION_DIR = CdeEngine.getInstance().getEnvironment().getRepositoryAccess().getSolutionPath(CdeConstants.SOLUTION_DIR + "/");
  
  private static final HashSet<String> CACHEABLE_EXTENSIONS = new HashSet<String>();
  private static final HashMap<String, String> cacheContainer = new HashMap<String, String>();

  private boolean isCacheEnabled = true;

  private static final Log logger = LogFactory.getLog(ResourceManager.class);

  public ResourceManager() {

    CACHEABLE_EXTENSIONS.add("html");
    CACHEABLE_EXTENSIONS.add("json");
    CACHEABLE_EXTENSIONS.add("cdfde");

    final IResourceLoader resLoader = CdeEngine.getInstance().getEnvironment().getResourceLoader();
    this.isCacheEnabled = Boolean.parseBoolean(resLoader.getPluginSetting(this.getClass(), "pentaho-cdf-dd/enable-cache"));

  }


  public static ResourceManager getInstance() {

    if (instance == null) {
      instance = new ResourceManager();
    }

    return instance;
  }

  public String getResourceAsString(final String path, final HashMap<String, String> tokens) throws IOException {

    final String extension = getResourceExtension(path);
    final String cacheKey = buildCacheKey(path, tokens);

    // If it's cachable and we have it, return it.
    if (isCacheEnabled && CACHEABLE_EXTENSIONS.contains(extension) && cacheContainer.containsKey(cacheKey)) {
      // return from cache. Make sure we return a clone of the original object
      return cacheContainer.get(cacheKey);
    }

    // Read file
    File file = new File(PLUGIN_DIR + path);
    // if not under plugin dir, try cde's solution dir
    if(!file.exists())
    {
      file = new File(SOLUTION_DIR + path);
      
      //if not under plugin dir nor plugins solution dir try the custom dirs
      if(!file.exists()){
        file = new File(CdeEngine.getInstance().getEnvironment().getRepositoryAccess().getSolutionPath("")+path);
      }
    }

    
    String resourceContents = FileUtils.readFileToString(file);

    if (tokens != null) {
      for (final String key : tokens.keySet()) 
      {
        resourceContents = StringUtils.replace(resourceContents, key, tokens.get(key));
      }
    }

    // We have the resource. Should we cache it?
    if (isCacheEnabled && CACHEABLE_EXTENSIONS.contains(extension)) {
      cacheContainer.put(cacheKey, resourceContents);
    }

    return resourceContents;
  }


  public String getResourceAsString(final String path) throws IOException 
  {  
    return getResourceAsString(path, null);
  }


  private String buildCacheKey(final String path, final HashMap<String, String> tokens) {

    final StringBuilder keyBuilder = new StringBuilder(path);

    if (tokens != null) {
      for (final String key : tokens.keySet()) {
        keyBuilder.append(key.hashCode());
        keyBuilder.append(tokens.get(key).hashCode());
      }
    }

    return keyBuilder.toString();
  }


  private String getResourceExtension(final String path) {

    return path.substring(path.lastIndexOf('.') + 1);

  }

  public void cleanCache() {

    cacheContainer.clear();
  }


  public void getSolutionResource(final OutputStream out, final String resource) throws IOException
  {
    String[] roots = new String[3];
    roots[0] = CdeEngine.getInstance().getEnvironment().getRepositoryAccess().getSolutionPath(CdeConstants.PLUGIN_PATH);
    roots[1] = CdeEngine.getInstance().getEnvironment().getRepositoryAccess().getSolutionPath("");
    roots[2] = CdeEngine.getInstance().getEnvironment().getRepositoryAccess().getSolutionPath(CdeConstants.MOLAP_PLUGIN_PATH);
    getSolutionResource(out, resource, roots);
  }

  public void getSolutionResource(final OutputStream out, final String resource, final String[] allowedRoots)
          throws IOException
  {
    final String path = Utils.getSolutionPath(resource); //$NON-NLS-1$ //$NON-NLS-2$

    final IResourceLoader resLoader =  CdeEngine.getInstance().getEnvironment().getResourceLoader();
    String formats = resLoader.getPluginSetting(this.getClass(), "resources/downloadable-formats");

    if (formats == null)
    {
      logger.error(
              "Could not obtain resources/downloadable-formats settings entry, " +
                      "please check plugin.xml and make sure settings are refreshed.");

      formats = ""; //avoid NPE
    }

    List<String> allowedFormats = Arrays.asList(formats.split(","));
    String extension = resource.replaceAll(".*\\.(.*)", "$1");
    if (allowedFormats.indexOf(extension) < 0)
    {
      // We can't provide this type of file
      throw new SecurityException("Not allowed");
    }

    final File file = new File(path);
    final String system = CdeEngine.getInstance().getEnvironment().getRepositoryAccess().getSolutionPath(CdeConstants.SYSTEM_PATH);
    File rootFile;
    boolean allowed = false;
    for (String root : allowedRoots)
    {
      if (isFileWithinPath(file, root))
      {
        /* If the file's within the specified root, it looks good. But if the
         * file is within /system/, we need to check whether the root specifically
         * allows for files in there as well.
         */
        rootFile = new File(root);
        if (!isFileWithinPath(file, system) || isFileWithinPath(rootFile, system))
        {
          allowed = true;
          break;
        }
      }
    }

    if (!allowed)
    {
      throw new SecurityException("Not allowed");
    }

    InputStream in = null;
    try
    {
      in = new FileInputStream(file);
      IOUtils.copy(in, out);
    }
    catch (FileNotFoundException e)
    {
      logger.warn("Couldn't find file " + file.getCanonicalPath());
      throw e;
    }
    finally
    {
      IOUtils.closeQuietly(in);
    }
  }

  private boolean isFileWithinPath(File file, String absPathBase){
    try {
      // Using commons.io.FilenameUtils normalize method to make sure we can
      // support symlinks here
      return FilenameUtils.normalize(file.getAbsolutePath()).startsWith(FilenameUtils.normalize(absPathBase));
    } catch (Exception e){
      return false;
    }
  }

}
