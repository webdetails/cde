/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.packager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author pdpi
 */
class Concatenate
{
  private static final Log logger = LogFactory.getLog(Concatenate.class);

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

      StringBuffer buffer = new StringBuffer();
      for (File file : files)
      {
        //TODO: review this!
        BufferedReader fr = null;
        try {
          StringBuffer tmp = new StringBuffer();
          fr = new BufferedReader(new FileReader(file));
          while (fr.ready())
          {
            tmp.append(fr.readLine());
          }
          rootpath = rootpath.replaceAll("\\\\", "/").replaceAll("/+", "/");
          // Quick and dirty hack: if the path aims at the custom components, we point at getResource, else we point at the static resource folders
  
          String filePath = file.getPath().replaceAll("\\\\", "/"); // Fix windows slashes'
          String fileLocation = "";
          if (filePath.contains("resources/custom"))
          {
            fileLocation = filePath.replaceAll(file.getName(), "") // Remove this file's name
                    .replaceAll(rootpath, "../");   //
            //fileLocation = "";
          }
          else if (filePath.matches(".*pentaho-cdf-dd/css/.*/.*$"))
          {
            fileLocation = filePath.replaceAll(file.getName(), "") // Remove this file's name
                    .replaceAll(rootpath, "../");
          }
          else if (filePath.matches(".*cde/components/.*/.*$"))
          {
            fileLocation = "../../res/" + filePath.substring(filePath.indexOf("cde/components/")).replaceAll(file.getName() + "$", "");
          } else if (filePath.matches(".*system/c\\w\\w.*"))
              fileLocation = "../" + filePath.substring(filePath.indexOf("system/")).replaceAll(file.getName() + "$", "");
          buffer.append(tmp.toString() //
                  // We need to replace all the URL formats
                  .replaceAll("(url\\(['\"]?)", "$1" + fileLocation.replaceAll("/+", "/"))); // Standard URLs
        }
        catch (FileNotFoundException e) {
          logger.error("concat: File " + file.getAbsolutePath() + " doesn't exist! Skipping...");
        }
        catch (Exception e) {
          logger.error("concat: Error while attempting to concatenate file "
              + file.getAbsolutePath() + ". Trying to continue...", e);
        }
        finally {
          IOUtils.closeQuietly(fr);
        }

      }
      try {
        return new ByteArrayInputStream(buffer.toString().getBytes("UTF8"));
      } catch (UnsupportedEncodingException e) {
        logger.error(e);
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
