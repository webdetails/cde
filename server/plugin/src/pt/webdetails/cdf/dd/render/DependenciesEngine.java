/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator;
import pt.webdetails.cdf.dd.packager.Packager;

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

  public DependenciesEngine(String name, StringFilter format, String sourcePath, String type)
  {
    this.name = name;
    packagedPath = type.toLowerCase() + "/" + name + "." + type.toLowerCase();
    packager.registerPackage(name, Packager.Filetype.valueOf(type), rootdir, rootdir + "/" + packagedPath, (String[]) null);
    dependencyPool = new LinkedHashMap<String, Dependency>();
    this.format = format;
    this.sourcePath = sourcePath;
  }

  public String getPackagedDependencies()
  {
    return getPackagedDependencies(format);
  }

  public String getPackagedDependencies(StringFilter filter)
  {
    String hash = packager.minifyPackage(name);
    return filter.filter(packagedPath + "?v=" + hash);
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
      output.append(filter.filter(dep.getDeps()) + newLine);
    }
    return output.toString();
  }

  public void register(String name, String version, String path) throws Exception
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
    if (dep != null)
    {
      dep.update(version, path);
    }
    else
    {
      File f = new File((sourcePath + "/" + path).replaceAll("\\\\", "/").replaceAll("/+", "/"));
      FileInputStream fis = new FileInputStream(f);
      byte[] fileContent = new byte[(int) f.length()];
      fis.read(fileContent);
      String hash = byteToHex(MessageDigest.getInstance("MD5").digest(fileContent));
      dep = new Dependency(version, path, hash);
      dependencyPool.put(name, dep);
      packager.addFileToPackage(this.name, f);
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
    private String version;

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
      return path + ((hash == null) ? "" : "&v=" + hash);
    }
  }
}
