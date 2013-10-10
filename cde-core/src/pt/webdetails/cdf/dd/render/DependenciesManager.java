/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.render;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.packager.DependenciesPackage;
import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cdf.dd.packager.DependenciesPackage.PackageType;
import pt.webdetails.cdf.dd.packager.input.StaticSystemOrigin;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import sun.security.util.PendingException;

/**
 *
 * @author pdpi
 */
public final class DependenciesManager
{
  private static Log logger = LogFactory.getLog(DependenciesManager.class);
  
  private static final String INCLUDES_PROP = "includes.properties";
  
  private static DependenciesManager manager;
  private final HashMap<String, DependenciesPackage> packages;

  public DependenciesManager()
  {
    packages = new HashMap<String, DependenciesPackage>();
  }

  public static synchronized void refresh()
  {
    manager = null;
  }
  
  private static DependenciesManager createInstance()
  {
    DependenciesManager manager = new DependenciesManager();
    IUrlProvider urlProvider = CdeEngine.getEnv().getPluginEnv().getUrlProvider();
    IContentAccessFactory factory = CdeEnvironment.getContentAccessFactory();
    manager.registerPackage( StdPackages.COMPONENT_STYLES, PackageType.CSS);
    manager.registerPackage( StdPackages.COMPONENT_DEF_SCRIPTS, PackageType.JS);
    manager.registerPackage(
        StdPackages.COMPONENT_SNIPPETS,
        new DependenciesPackage( StdPackages.COMPONENT_SNIPPETS,PackageType.JS, factory, urlProvider ) );// TODO change
    manager.registerPackage( StdPackages.CDFDD, PackageType.JS );

    //read include.properties
    Properties props = new Properties();
    try {
      InputStream in = null;
      try {
        in = CdeEnvironment.getPluginSystemReader().getFileInputStream( INCLUDES_PROP );
        props.load( in );
      } 
      finally {
        IOUtils.closeQuietly( in );
      }
      PathOrigin origin = new StaticSystemOrigin("");

      manager.registerPackage( StdPackages.EDITOR_JS_INCLUDES, PackageType.JS );
      DependenciesPackage scripts = manager.getPackage( StdPackages.EDITOR_JS_INCLUDES );
      for (String path : props.get("scripts").toString().split(",")) {
        scripts.registerFileDependency( path, null, origin, path );
      }

      manager.registerPackage( StdPackages.EDITOR_CSS_INCLUDES, PackageType.CSS );
      DependenciesPackage styles = manager.getPackage( StdPackages.EDITOR_CSS_INCLUDES );
      for (String path : props.get("styles").toString().split(",")) {
        styles.registerFileDependency( path, null, origin, path );
      }
    } catch ( IOException e ) {
      logger.error("Error attempting to read " + INCLUDES_PROP, e);
    }
    return manager;
  }

  public static synchronized DependenciesManager getInstance()
  {
    if(manager == null)
    {
      manager = createInstance();
    }
    return manager;
  }

  public static final class StdPackages {//TODO: change file names as well

    public final static String COMPONENT_DEF_SCRIPTS = "CDF";

    public final static String COMPONENT_STYLES = "CDF-CSS";

    public final static String COMPONENT_SNIPPETS = "CDF-RAW";

    public final static String CDFDD = "CDFDD";

    public final static String EDITOR_JS_INCLUDES = "scripts";
    public final static String EDITOR_CSS_INCLUDES = "styles";
  }

  public DependenciesPackage getPackage(String id) {
    return packages.get( id );
  }

  /**
   * @return if there was a package already registered for that id
   */
  public boolean registerPackage(String id, DependenciesPackage pkg) {
    return packages.put( id, pkg ) != null;
  }

  public boolean registerPackage(String id, PackageType type) {
    IUrlProvider urlProvider = CdeEngine.getEnv().getPluginEnv().getUrlProvider();
    return registerPackage( id, new DependenciesPackage( id, type, CdeEnvironment.getContentAccessFactory(), urlProvider) );
  }
}
