package pt.webdetails.cdf.dd;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;

import pt.webdetails.cpf.repository.RepositoryAccess;

/**
 * External Editor (stub)
 */
public class ExternalFileEditorBackend {

  private static Log logger = LogFactory.getLog(ExternalFileEditorBackend.class);
  
  private static final String ENCODING = "UTF-8";
  
  protected static String getFileContents(final String filePath, IPentahoSession userSession) throws IOException
  {
    if(StringUtils.isEmpty(filePath)){
      logger.error("getFileContents: no path given");
      return null;
    }
    
    RepositoryAccess repository = RepositoryAccess.getRepository(userSession); 
    
    if(repository.resourceExists(filePath))
    {
      return repository.getResourceAsString(filePath);
    }
    else 
    {//TODO: better treatment needed here
      return StringUtils.EMPTY; 
    }
  }
  
  protected static boolean canEdit(final String filePath, IPentahoSession userSession){
    return RepositoryAccess.getRepository(userSession).canWrite(filePath);
  }
  
  protected static boolean createFolder(String path, IPentahoSession userSession) throws IOException, PentahoAccessControlException
  {

    RepositoryAccess repository = RepositoryAccess.getRepository(userSession);    
    boolean status = repository.createFolder(path);
    
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
  
  
  protected static boolean writeFile(String path, String solution, IPentahoSession userSession, String contents) throws IOException, PentahoAccessControlException //TODO:
  {    
    
    RepositoryAccess repository = RepositoryAccess.getRepository(userSession); 
    
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
