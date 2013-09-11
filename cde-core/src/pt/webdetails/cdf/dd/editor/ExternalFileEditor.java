/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.editor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

/**
 * External Editor (stub)
 */
public class ExternalFileEditor {

  private static Log logger = LogFactory.getLog(ExternalFileEditor.class);
  
  private static final String ENCODING = "UTF-8";
  
  public static InputStream getFileContents(final String filePath) throws IOException
  {
    if(StringUtils.isEmpty(filePath)){
      logger.error("getFileContents: no path given");
      return null;
    }
    
    IUserContentAccess access = CdeEnvironment.getUserContentAccess();
    
    if(access.fileExists(filePath)) {
      return access.getFileInputStream(filePath);
    
    } else {
      return null; 
    }
  }

  public static boolean canEdit(final String filePath){
	 return CdeEnvironment.getUserContentAccess().hasAccess(filePath, FileAccess.WRITE);
  }

  public static boolean createFolder(String path) throws IOException {
   
    boolean success = CdeEnvironment.getUserContentAccess().createFolder(path);
    
    if (!success) {
    	logger.error("createFolder: creating " + path + " returned error. Folder Already exists?");
    } 
    
    return success;
  }


  public static boolean writeFile(String path, String contents) throws IOException {    
    
	IUserContentAccess access = CdeEnvironment.getUserContentAccess();
	  
    if(access.hasAccess(path, FileAccess.WRITE)) {
    	
      boolean success = access.saveFile(path, new ByteArrayInputStream(contents.getBytes(ENCODING)));
    	
      if(!success){
    	  logger.error("writeFile: failed saving " + path);
      }
      
      return success;
    
    } else {
      logger.error("writeFile: no permissions to write file " + path);
      return false;
    }
  }
}
