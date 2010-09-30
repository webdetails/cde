/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.packager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 *
 * @author pdpi
 */
class Concatenate
{

  public static InputStream concat(File[] files)
  {
    ListOfFiles mylist = new ListOfFiles(files);

    return new SequenceInputStream(mylist);
  }

  public static InputStream concat(File[] files, String rootpath)
  {
    if (rootpath == null || StringUtils.isEmpty(rootpath))
    {
      return concat(files);
    }
    try
    {
      StringBuffer buffer = new StringBuffer();
      for (File file : files)
      {
        StringBuffer tmp = new StringBuffer();
        BufferedReader fr = new BufferedReader(new FileReader(file));
        while (fr.ready())
        {
          tmp.append(fr.readLine());
        }
        rootpath = rootpath.replaceAll("\\\\", "/").replaceAll("/+", "/");
        // Quick and dirty hack: if the path aims at the custom components, we point at getResource, else we point at the static resource folders

        String fileLocation;
        if (file.getPath().contains("resources/custom"))
        {
          // fileLocation = file.getPath().replaceAll(file.getName(), "").replaceAll("\\\\", "/").replaceAll(rootpath, "getResource?resource=");
          fileLocation = "";
        }
        else
        {
          fileLocation = file.getPath().replaceAll(file.getName(), "").replaceAll("\\\\", "/").replaceAll(rootpath, "..");
        }
        buffer.append(tmp.toString() //
                // We need to replace all the URL formats
                .replaceAll("(url\\(['\"]?)", "$1" + fileLocation) // Standard URLs
                .replaceAll("(progid:DXImageTransform.Microsoft.AlphaImageLoader\\(src=')", "$1" + fileLocation + "../")); // these are IE-Only


      }
      return new ByteArrayInputStream(buffer.toString().getBytes("UTF8"));
    }
    catch (Exception e)
    {
      return null;
    }
  }
}

class ListOfFiles implements Enumeration<FileInputStream>
{

  private File[] listOfFiles;
  private int current = 0;

  public ListOfFiles(File[] listOfFiles)
  {
    this.listOfFiles = listOfFiles;
  }

  public boolean hasMoreElements()
  {
    if (current < listOfFiles.length)
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  public FileInputStream nextElement()
  {
    FileInputStream in = null;

    if (!hasMoreElements())
    {
      throw new NoSuchElementException("No more files.");
    }
    else
    {
      File nextElement = listOfFiles[current];
      current++;
      try
      {
        in = new FileInputStream(nextElement);
      }
      catch (FileNotFoundException e)
      {
        System.err.println("ListOfFiles: Can't open " + nextElement);
      }
    }
    return in;
  }
}
