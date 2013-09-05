/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import pt.webdetails.cpf.resources.IResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

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

}
