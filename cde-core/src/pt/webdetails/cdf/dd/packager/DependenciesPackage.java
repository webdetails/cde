/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.packager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import pt.webdetails.cdf.dd.packager.dependencies.CssMinifiedDependency;
import pt.webdetails.cdf.dd.packager.dependencies.Dependency;
import pt.webdetails.cdf.dd.packager.dependencies.FileDependency;
import pt.webdetails.cdf.dd.packager.dependencies.JsMinifiedDependency;
import pt.webdetails.cdf.dd.packager.dependencies.PackagedFileDependency;
import pt.webdetails.cdf.dd.packager.dependencies.SnippetDependency;
import pt.webdetails.cdf.dd.packager.input.StaticSystemOrigin;
import pt.webdetails.cdf.dd.render.StringFilter;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IRWAccess;


/**
 * A set of css|js files that can be packaged into a single file.<br>
 * Encompasses former functionality of DependenciesEngine/Packager
 */
public class DependenciesPackage {

//  private static Log logger = LogFactory.getLog(DependenciesPackage.class);

  public enum PackagingMode
  {
    MINIFY, CONCATENATE
  };
  public enum PackageType
  {
    CSS, JS
  };

  //FIXME TODO raw dependencies, minified file output, use in DependenciesManager, check streams
  //TODO: separate raw from rest
  private String name;
  private Map<String, FileDependency> fileDependencies;
  private PackagedFileDependency packagedDependency;
  private PackageType type;

  private Map<String, SnippetDependency> rawDependencies;
  private IContentAccessFactory factory;

  /**
   * 
   * @param name
   * @param type
   * @param factory
   */
  public DependenciesPackage(String name, PackageType type, IContentAccessFactory factory) {
    this.name = name;
    fileDependencies = new LinkedHashMap<String, FileDependency>();
    rawDependencies = new LinkedHashMap<String, SnippetDependency>();
    this.type = type;
    this.factory = factory;
  }

  /**
   * Registers a dependency in this package
   * @param name
   * @param version
   * @param origin
   * @param path
   * @return
   */
  public boolean registerFileDependency(String name, String version, PathOrigin origin, String path) {
    FileDependency newDep = new FileDependency( version, origin, path);
    if (registerDependency( name, newDep, fileDependencies )) {
      //invalidate packaged if there
      packagedDependency = null;
      return true;
    }
    return false;
  }

  public boolean registerRawDependency(String name, String version, String contents) {
    SnippetDependency snip = new SnippetDependency( version, contents );
    return registerDependency( name, snip, rawDependencies );
  }

  protected <T extends Dependency> boolean registerDependency(String name, T dependency, Map<String, T> registry) {
    Dependency dep = registry.get(name);
    if (dep == null || dep.isOlderVersionThan( dependency )) {
      registry.put( name, dependency);
      return true;
    }
    return false;
  }

  //TODO: entryPoint
  /**
   * 
   * @param format
   * @param isPackaged 
   * @return
   */
  public String getDependencies(StringFilter format, boolean isPackaged) {
    return isPackaged ?
        getPackagedDependency( format ):
        getUnpackagedDependencies( format );
  }

  public String getRawDependencies(boolean isPackaged) {
    StringBuilder sb = new StringBuilder();
    for (SnippetDependency dep : rawDependencies.values()) {
      sb.append( dep.getContents() );
      sb.append( '\n' );
    }
    return sb.toString();
  }

  /**
   * Get references to the dependencies.
   * @param isPackaged if to return a single compressed file
   * @return script or link tag with file references
   */
  public String getDependencies(boolean isPackaged) {
    return getDependencies( getDefaultStringFilter( type ), isPackaged );
  }

  public String getName() {//TODO: never used
    return name;
  }

  public String getUnpackagedDependencies(StringFilter format) {
    StringBuilder sb = new StringBuilder();
    sb.append( "\n" );
//    sb.append( "\t<!-- " + getName() + "-->\n" );
    for (Dependency dep : fileDependencies.values()) {
      sb.append( format.filter( dep.getDependencyInclude() ) );
    }
    return sb.toString();
  }

  protected synchronized String getPackagedDependency(StringFilter format) {
    if (packagedDependency == null) {
      String packagedPath = name + "." + type.toString().toLowerCase();
      String baseDir = type.toString().toLowerCase();
      IRWAccess writer = factory.getPluginSystemWriter( baseDir );
      PathOrigin origin = new StaticSystemOrigin( baseDir );
      switch ( type ) {
        case CSS:
          packagedDependency = new CssMinifiedDependency( origin, packagedPath, writer, fileDependencies.values() );
          break;
        case JS:
          packagedDependency = new JsMinifiedDependency( origin, packagedPath, writer, fileDependencies.values() );
          break;
        default:
          break;//TODO:
      }
    }
    return format.filter( packagedDependency.getDependencyInclude() );
  }

  private static StringFilter getDefaultStringFilter(PackageType type) {
    switch (type) {
      case CSS:
        return new StringFilter()
        {
          public String filter(String input)
          {
            return String.format(
              "\t\t<link href=\"%s\" rel=\"stylesheet\" type=\"text/css\" />\n",
              input);
          }
        };
      case JS:
        return new StringFilter()
        {
          public String filter(String input)
          {
            return String.format(
              "\t\t<script language=\"javascript\" type=\"text/javascript\" src=\"%s\"></script>\n",
              input);
          }
        };
      default:
        return new StringFilter() {
          public String filter( String input ) {
            return input + "\n";
          }
        };
    }
  }

}
