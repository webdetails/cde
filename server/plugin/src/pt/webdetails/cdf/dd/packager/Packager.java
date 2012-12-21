/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.packager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.packager.Packager.Mode;

/**
 *
 * @author pdpi
 */
public class Packager
{

  public enum Filetype
  {

    CSS, JS
  };

  public enum Mode
  {

    MINIFY, CONCATENATE
  };
  static Log logger = LogFactory.getLog(Packager.class);
  private static Packager _instance;
  private Map<String, FileSet> fileSets;

  private Packager()
  {
    this.fileSets = new HashMap<String, FileSet>();
  }

  public static synchronized Packager getInstance()
  {
    if (_instance == null)
    {
      _instance = new Packager();
    }
    return _instance;

  }

  public void registerPackage(Filetype type, String root, String filename, String[] files)
  {
    registerPackage(filename, type, root, filename, files);
  }

  public void registerPackage(String name, Filetype type, String root, String filename, String[] files)
  {
    ArrayList<File> fileHandles = new ArrayList<File>();
    if (files != null)
    {
      for (String file : files)
      {
        fileHandles.add(new File((root + "/" + file).replaceAll("/+", "/")));
      }
    }
    registerPackage(name, type, root, filename, (File[]) fileHandles.toArray(new File[fileHandles.size()]));
  }

  public void registerPackage(String name, Filetype type, String root, String output, File[] files)
  {
    if (this.fileSets.containsKey(name))
    {
      Logger.getLogger(Packager.class.getName()).log(Level.WARNING, name + " is overriding an existing file package!");
    }
    try
    {
      FileSet fileSet = new FileSet(output, type, files, root);
      this.fileSets.put(name, fileSet);
    }
    catch (IOException ex)
    {
      Logger.getLogger(Packager.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (NoSuchAlgorithmException ex)
    {
      Logger.getLogger(Packager.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  public boolean isPackageRegistered(String pkg)
  {
    return this.fileSets.containsKey(pkg);
  }

  public String minifyPackage(String pkg)
  {
    return minifyPackage(pkg, Mode.MINIFY);
  }

  public synchronized String minifyPackage(String pkg, Mode mode)
  {
    try
    {
      return this.fileSets.get(pkg).update(mode);
    }
    catch (IOException ex)
    {
      Logger.getLogger(Packager.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (NoSuchAlgorithmException ex)
    {
      Logger.getLogger(Packager.class.getName()).log(Level.SEVERE, null, ex);
    }
    return "";
  }

  public void addFileToPackage(String pkg, String name, String path)
  {
    this.fileSets.get(pkg).addFile(name, path);
  }

  public void addFileToPackage(String pkg, String name, File file)
  {
    this.fileSets.get(pkg).addFile(name, file);
  }
}

class FileSet
{

  private boolean dirty;
  private String latestVersion;
  private Map<String, String> files;
  private String location;
  private Packager.Filetype filetype;
  private String rootdir;

  public void addFile(String name, String path)
  {
    if (!files.containsKey(name) || !files.get(name).equals(path))
    {
      this.dirty = true;
      this.files.put(name, path);
    }
  }

  public void addFile(String name, File file)
  {
    try
    {
      addFile(name, file.getCanonicalPath());
    }
    catch (IOException e)
    {
      Packager.logger.error("Couldn' add resource '" + name + "': ", e);
    }
  }

  public FileSet(String location, Packager.Filetype type, File[] fileSet, String rootdir) throws IOException, NoSuchAlgorithmException
  {
    this.files = new LinkedHashMap<String, String>();
    for (File file : fileSet)
    {
      String path = file.getCanonicalPath();
      this.files.put(path, path);
    }
    this.location = location;
    this.filetype = type;
    this.latestVersion = "";
    this.dirty = true;
    this.rootdir = rootdir;
  }

  public FileSet() throws IOException, NoSuchAlgorithmException
  {
    dirty = true;
    files = new HashMap<String, String>();
    latestVersion = null;
    location = null;
  }

  private String minify(Mode mode) throws IOException, NoSuchAlgorithmException
  {
    InputStream concatenatedStream;
    FileWriter wout;
    Reader freader;
    //FileWriter output;
    try
    {
      //output = new FileWriter(location);
      File[] filesArray = new File[this.files.size()];

      int i = 0;
      Set<String> keys = files.keySet();
      for (String key : keys)
      {
        filesArray[i++] = new File(this.files.get(key));
      }
      File location = new File(this.location);
      switch (this.filetype)
      {
        case JS:
          concatenatedStream = Concatenate.concat(filesArray);
          freader = new InputStreamReader(concatenatedStream, "UTF8");


          switch (mode)
          {
            case MINIFY:
              JSMin jsmin = new JSMin(concatenatedStream, new FileOutputStream(location));
              jsmin.jsmin();
              break;
            case CONCATENATE:
              OutputStream fos = new FileOutputStream(location);
              byte[] buffer = null;
              while (concatenatedStream.available() > 0)
              {
                concatenatedStream.read(buffer, 0, 4096);
                fos.write(buffer);
              }
          }
          break;
        case CSS:
          concatenatedStream = Concatenate.concat(filesArray, rootdir);
          freader = new InputStreamReader(concatenatedStream, "UTF8");

          //FileWriter 
          wout = new FileWriter(location);

          IOUtils.copy(freader, wout);

          //CSSMin.formatFile(freader, new FileOutputStream(location));
          wout.close();
          break;
      }
      FileInputStream script = new FileInputStream(location);
      byte[] fileContent = new byte[(int) location.length()];

      script.read(fileContent);

      this.dirty = false;

      this.latestVersion = byteToHex(MessageDigest.getInstance("MD5").digest(fileContent));

      return latestVersion;
    }
    catch (Exception ex)
    {
      Logger.getLogger(FileSet.class.getName()).log(Level.SEVERE, null, ex);


      return null;
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

  public String update() throws IOException, NoSuchAlgorithmException
  {
    return update(false);
  }

  public String update(Mode mode) throws IOException, NoSuchAlgorithmException
  {
    return update(false, mode);
  }

  public String update(boolean force) throws IOException, NoSuchAlgorithmException
  {
    return update(force, Mode.MINIFY);
  }

  public String update(boolean force, Mode mode) throws IOException, NoSuchAlgorithmException
  {
    // If we're not otherwise sure we must update, we actively check if the
    //minified file is older than any file in the set.
    if (!dirty && !force)
    {
      File location = new File(this.location);
      for (String filePath : files.values())
      {
        File file = new File(filePath);
        if (!location.exists() || file.lastModified() > location.lastModified())
        {
          this.dirty = true;
          break;
        }
      }
    }
    return (dirty || force) ? this.minify(mode) : this.latestVersion;
  }
}
