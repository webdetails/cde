/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.editor;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cpf.repository.IRepositoryAccess;

/**
 * External Editor (stub)
 */
public class ExternalFileEditor {

  private static Log logger = LogFactory.getLog(ExternalFileEditor.class);
  
  private static final String ENCODING = "UTF-8";
  
  public static String getFileContents(final String filePath) throws IOException
  {
    if(StringUtils.isEmpty(filePath)){
      logger.error("getFileContents: no path given");
      return null;
    }
    
    IRepositoryAccess repository = CdeEngine.getInstance().getEnvironment().getRepositoryAccess();
    
    if(repository.resourceExists(filePath))
    {
      return repository.getResourceAsString(filePath);
    }
    else 
    {//TODO: better treatment needed here
      return StringUtils.EMPTY; 
    }
  }

  public static boolean canEdit(final String filePath){
    return CdeEngine.getInstance().getEnvironment().getRepositoryAccess().canWrite(filePath);
  }

  public static boolean createFolder(String path) throws IOException
  {
   
    boolean status = CdeEngine.getInstance().getEnvironment().getRepositoryAccess().createFolder(path);
    
    if (status)
    {
      return true;
    }
    else
    {
      logger.error("createFolder: creating " + path + " returned error. Folder Already exists?");
      return false;
    }
  }


  public static boolean writeFile(String path, String contents) throws IOException
  {    
    
    IRepositoryAccess repository = CdeEngine.getInstance().getEnvironment().getRepositoryAccess(); 
    
    if(repository.canWrite(path)){
      switch(repository.publishFile(path, contents.getBytes(ENCODING), true)){
        case OK:
          return true;
        case FAIL:
          default:
          logger.error("writeFile: failed saving " + path);
          return false;
      }
    }
    else
    {
      logger.error("writeFile: no permissions to write file " + path );
      return false;
    }
  }
 
  
  
}
