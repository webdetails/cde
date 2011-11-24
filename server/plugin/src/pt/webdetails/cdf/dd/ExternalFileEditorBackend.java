package pt.webdetails.cdf.dd;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISolutionRepositoryService;
import org.pentaho.platform.engine.core.system.PentahoSystem;

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
    
    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);
    
    if(solutionRepository.resourceExists(filePath))
    {
      return solutionRepository.getResourceAsString(filePath, ISolutionRepository.ACTION_EXECUTE);
    }
    else 
    {//TODO: better treatment needed here
      return StringUtils.EMPTY; 
    }
  }
  
  protected static boolean canEdit(final String filePath, IPentahoSession userSession){
    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);
    
    //first check read permission
    ISolutionFile file = solutionRepository.getSolutionFile(filePath, ISolutionRepository.ACTION_EXECUTE);
    
    if(solutionRepository.resourceExists(filePath))
    {
      return solutionRepository.hasAccess(file,ISolutionRepository.ACTION_UPDATE);
    }
    else 
    {
      return solutionRepository.hasAccess(file,ISolutionRepository.ACTION_CREATE);
    }
  }
  
  protected static boolean createFolder(String path, IPentahoSession userSession) throws IOException, PentahoAccessControlException
  {
    String[] folder = StringUtils.split(path, "/");
    String folderName = folder[folder.length - 1];
    String folderPath = path.substring(0, path.indexOf(folderName));



    ISolutionRepositoryService service = PentahoSystem.get(ISolutionRepositoryService.class, userSession);


    boolean status = service.createFolder(userSession, "", folderPath, folderName, "");


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
    String[] file = StringUtils.split(path, "/");
    String fileName = file[file.length - 1];
    String filePath = path.substring(0, path.indexOf(fileName));
        
    String rootDir = PentahoSystem.getApplicationContext().getSolutionPath("");

    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);

    final boolean resourceExists = solutionRepository.resourceExists(path);
    // ok if either new file or has update permissions
    if (!resourceExists || solutionRepository.getSolutionFile(path, ISolutionRepository.ACTION_CREATE) != null)
    {            
      int status = solutionRepository.publish(rootDir, filePath, fileName, contents.getBytes(ENCODING), true);
      if (status == ISolutionRepository.FILE_ADD_SUCCESSFUL)
      {
        return true;
      }
      else
      {
        logger.error("writeFile: saving " + file + " returned " + new Integer(status).toString());//TODO: proper status?
        return false;
      }
    }
    else
    {
      logger.error("writeFile: no permissions to write file " + file );
      return false;
    }
  }
 
  
  
}
