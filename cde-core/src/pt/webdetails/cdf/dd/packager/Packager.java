/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.packager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.packager.Packager.Mode;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IRWAccess;;

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
    List<IBasicFile> fileHandles = new ArrayList<IBasicFile>();
    if (files != null) {
      
    	for (String file : files) {
    		fileHandles.add(CdeEnvironment.getPluginSystemReader().fetchFile(file));
    	}
    }
    registerPackage(name, type, root, filename, (IBasicFile[]) fileHandles.toArray(new IBasicFile[fileHandles.size()]));
  }

  public void registerPackage(String name, Filetype type, String root, String output, IBasicFile[] files) {
    
	  if (this.fileSets.containsKey(name)) {
		  Logger.getLogger(Packager.class.getName()).log(Level.WARNING, name + " is overriding an existing file package!");
      }
	  
    try {
    	
      // [TEMPORARY FIX] do magic here - convert 'root' to intended IReadAccess
      IRWAccess access = CdeEnvironment.getUserContentAccess();
      if(!StringUtils.isEmpty(root)){
    	  String rootPath = StringUtils.strip(root, "/").toLowerCase();
    	  if(rootPath.startsWith(CdeEnvironment.getSystemDir())){
    		  access = CdeEnvironment.getPluginSystemWriter();
    	  }else if(rootPath.startsWith(CdeEnvironment.getPluginRepositoryDir())){
    		  access = CdeEnvironment.getPluginRepositoryWriter();
    	  }
      }	
      // end temporary fix
      
      
      FileSet fileSet = new FileSet(output, type, files, access);
      this.fileSets.put(name, fileSet);
    
    } catch (IOException ex) {
      Logger.getLogger(Packager.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NoSuchAlgorithmException ex) {
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

  public void addFileToPackage(String pkg, String name, IBasicFile file)
  {
    this.fileSets.get(pkg).addFile(name, file);
  }
}

class FileSet {

  private boolean dirty;
  private String latestVersion;
  private Map<String, String> files;
  private String location;
  private Packager.Filetype filetype;
  private IRWAccess access;

  public void addFile(String name, String path)
  {
    if (!files.containsKey(name) || !files.get(name).equals(path))
    {
      this.dirty = true;
      this.files.put(name, path);
    }
  }

  public void addFile(String name, IBasicFile file) {
    try {
      addFile(name, file.getPath());
    } catch (Throwable t) {
      Packager.logger.error("Couldn' add resource '" + name + "': ", t);
    }
  }

  public FileSet(String location, Packager.Filetype type, IBasicFile[] fileSet, IRWAccess access) throws IOException, NoSuchAlgorithmException {
    this.files = new LinkedHashMap<String, String>();
    for (IBasicFile file : fileSet) {
      String path = file.getPath();
      this.files.put(path, path);
    }
    this.location = location;
    this.filetype = type;
    this.latestVersion = "";
    this.dirty = true;
    this.access = access;
  }

  public FileSet() throws IOException, NoSuchAlgorithmException {
    dirty = true;
    files = new HashMap<String, String>();
    latestVersion = null;
    location = null;
  }

  private String minify(Mode mode) throws IOException, NoSuchAlgorithmException {
    InputStream concatenatedStream;
    OutputStream fos = null;
    try {
      IBasicFile[] filesArray = new IBasicFile[this.files.size()];

      int i = 0;
      Set<String> keys = files.keySet();
      for (String key : keys) {
        filesArray[i++] = CdeEnvironment.getPluginSystemReader().fetchFile(this.files.get(key));
      }
      
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      
      switch (this.filetype) {
        case JS:
          concatenatedStream = Concatenate.concat(filesArray);
          
          switch (mode) {
            case MINIFY:
              JSMin jsmin = new JSMin(concatenatedStream, byteArrayOutputStream);
              jsmin.jsmin();
              access.saveFile(location, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
              break;
            case CONCATENATE:
              access.saveFile(location, concatenatedStream);
          }
          break;
          
        case CSS:
          concatenatedStream = Concatenate.concat(filesArray, access);
          access.saveFile(location, concatenatedStream);
          break;
      }
      InputStream script = access.getFileInputStream(location);
      
      byte[] fileContent = new byte[(int) location.length()];

      script.read(fileContent);

      this.dirty = false;

      this.latestVersion = byteToHex(MessageDigest.getInstance("MD5").digest(fileContent));

      return latestVersion;
    } catch (Exception ex) {
      Logger.getLogger(FileSet.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    } finally{
    	if(fos != null){ fos.close(); }
    }
  }

  private String byteToHex(byte[] bytes) {
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < bytes.length; i++) {
      String byteValue = Integer.toHexString(0xFF & bytes[i]);
      hexString.append(byteValue.length() == 2 ? byteValue : "0" + byteValue);
    }
    return hexString.toString();
  }

  public String update() throws IOException, NoSuchAlgorithmException {
    return update(false);
  }

  public String update(Mode mode) throws IOException, NoSuchAlgorithmException {
    return update(false, mode);
  }

  public String update(boolean force) throws IOException, NoSuchAlgorithmException {
    return update(force, Mode.MINIFY);
  }

  public String update(boolean force, Mode mode) throws IOException, NoSuchAlgorithmException {
    // If we're not otherwise sure we must update, we actively check if the
    //minified file is older than any file in the set.
    if (!dirty && !force) {
      
      for (String filePath : files.values()) {
        if (access.getLastModified(filePath) > access.getLastModified(this.location)) {
          this.dirty = true;
          break;
        }
      }
    }
    return (dirty || force) ? this.minify(mode) : this.latestVersion;
  }
}
