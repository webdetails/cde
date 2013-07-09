/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.render;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator;
import pt.webdetails.cdf.dd.packager.Packager;
import pt.webdetails.cdf.dd.packager.Packager.Mode;

/**
 *
 * @author pdpi
 */
public class DependenciesEngine
{

  private static Log logger = LogFactory.getLog(DashboardDesignerContentGenerator.class);
  public static final String newLine = System.getProperty("line.separator");
  private final String sourcePath;
  private Map<String, Dependency> dependencyPool;
  private StringFilter format;
  static private Packager packager = Packager.getInstance();
  private static String rootdir = PentahoSystem.getApplicationContext().getSolutionPath("system/" + DashboardDesignerContentGenerator.PLUGIN_NAME);
  private String packagedPath;
  private String name;

  public DependenciesEngine(String name, StringFilter format, String sourcePath, Packager.Filetype type)
  {
    this.name = name;
    packagedPath = type.toString().toLowerCase() + "/" + name + "." + type.toString().toLowerCase();
    packager.registerPackage(name, type, rootdir, rootdir + "/" + packagedPath, (String[]) null);
    dependencyPool = new LinkedHashMap<String, Dependency>();
    this.format = format;
    this.sourcePath = sourcePath;
  }

  public String getName()
  {
    return name;
  }

  public String getPackagedDependencies()
  {
    return getPackagedDependencies(format);
  }

  public String getPackagedDependencies(StringFilter filter)
  {
    final IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
    final String minification = resLoader.getPluginSetting(this.getClass(), "packager/minification").toUpperCase();
    Mode mode = Mode.valueOf(minification != null ? minification : "MINIFY");
    String hash = packager.minifyPackage(name, mode);
    return (filter == null ? format : filter).filter(packagedPath + "?v=" + hash);
  }

  public String getDependencies()
  {
    return getDependencies(format);
  }

  public String getDependencies(StringFilter filter)
  {
    StringBuilder output = new StringBuilder();
    for (Dependency dep : dependencyPool.values())
    {
      output.append((filter == null ? format : filter).filter(dep.getDeps()) + newLine);
    }
    return output.toString();
  }

  public String getDependencies(StringFilter filter, boolean isPackaged)
  {
    return isPackaged ? getPackagedDependencies(filter) : getDependencies(filter);
  }

  public void register(String name, String version, String path) throws Exception
  {
    Dependency dep;
    File f = new File((sourcePath + File.separator + path).replaceAll("\\\\", "/").replaceAll("/+", "/"));

    
    try
    {
      dep = dependencyPool.get(name);
    }
    catch (Exception e)
    {
      dep = null;
    }
    if (dep != null)
    {
      dep.update(version, path);
      packager.addFileToPackage(this.name, name, f.getCanonicalPath());
    }
    else
    {

      FileInputStream fis = null;
      try
      {
        fis = new FileInputStream(f);
        byte[] fileContent = new byte[(int) f.length()];
        fis.read(fileContent);
        String hash = byteToHex(MessageDigest.getInstance("MD5").digest(fileContent));
        dep = new Dependency(version, path, hash);
        dependencyPool.put(name, dep);
        packager.addFileToPackage(this.name, name, f.getCanonicalPath());
      }
      finally
      {
        IOUtils.closeQuietly(fis);
      }
    }
  }

  public void registerRaw(String name, String version, String contents) throws Exception
  {
    Dependency dep;
    try
    {
      dep = dependencyPool.get(name);
    }
    catch (Exception e)
    {
      dep = null;
    }
    if (dep != null && dep instanceof RawDependency)
    {
      dep.update(version, contents);
    }
    else
    {
      dep = new RawDependency(version, contents);
      dependencyPool.put(name, dep);
    }
  }

  private String byteToHex(byte[] bytes)
  {
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < bytes.length; i++)
    {
      String byteValue = Integer.toHexString(0xFF & bytes[i]);
      hexString.append(byteValue.length() == 2 ? byteValue : "0" + byteValue);
    }
    return hexString.toString();
  }

  private class Dependency
  {

    private String path;
    private String hash;
    protected String version;

    protected Dependency()
    {
    }

    public Dependency(String version, String path, String hash)
    {
      this.path = path;
      this.hash = hash;
      this.version = version;
    }

    public void update(String version, String path)
    {

      // Update only if the submitted version is newer than the existing one.
      // I assume version numberings always increase lexicographically
      if (this.version == null || this.version.compareTo(version) < 0)
      {
        this.path = path;
        this.version = version;
      }
    }

    public String getDeps()
    {
      return path + ((hash == null) ? "" : "?v=" + hash);
    }
  }

  private class RawDependency extends Dependency
  {

    private String contents;

    public RawDependency(String version, String contents)
    {
      super();
      this.contents = contents;
      this.version = version;
    }

    @Override
    public void update(String version, String contents)
    {

      // Update only if the submitted version is newer than the existing one.
      // I assume version numberings always increase lexicographically
      if (this.version == null || this.version.compareTo(version) < 0)
      {
        this.contents = contents;
        this.version = version;
      }
    }

    public String getDeps()
    {
      return contents;
    }
  }
}
