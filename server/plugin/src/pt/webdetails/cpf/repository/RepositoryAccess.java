package pt.webdetails.cpf.repository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFilter;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISolutionRepositoryService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * Attempt to centralize CTools repository access
 */
@SuppressWarnings("deprecation")
public class RepositoryAccess {
  
  private static Log logger = LogFactory.getLog(RepositoryAccess.class);

  private IPentahoSession userSession;

  public enum FileAccess {//TODO:use masks?
    READ,
    EDIT,
    EXECUTE,
    DELETE,
    CREATE, 
    NONE;
    
    public int toResourceAction(){
      switch(this){
        case NONE:
          return IPentahoAclEntry.PERM_NOTHING;
        case CREATE:
          return IPentahoAclEntry.PERM_CREATE;
        case DELETE:
          return IPentahoAclEntry.PERM_DELETE;
        case EDIT:
          return IPentahoAclEntry.PERM_UPDATE;
        case READ:
        case EXECUTE:
        default:
          return IPentahoAclEntry.PERM_EXECUTE;
      }
    }
  }
  
  public enum SaveFileStatus {
    //TODO: do we need more than this? use bool?
    OK, 
    FAIL
  }

  protected RepositoryAccess(IPentahoSession userSession) {
    this.userSession = userSession == null ? PentahoSessionHolder.getSession() : userSession;
  }
  
  public SaveFileStatus publishFile(String fileAndPath, byte[] data, boolean overwrite){
    return publishFile(FilenameUtils.getFullPath(fileAndPath), FilenameUtils.getName(fileAndPath), data, overwrite);
  }

  public SaveFileStatus publishFile(String solutionPath, String fileName, byte[] data, boolean overwrite){
    return publishFile(PentahoSystem.getApplicationContext().getSolutionPath(""), solutionPath, fileName, data, overwrite);
  }
  
  public SaveFileStatus publishFile(String baseUrl, String path, String fileName, byte[] data, boolean overwrite) {
    try {
      int status = getSolutionRepository().publish(baseUrl, path, fileName, data, overwrite);
      switch(status){
        case ISolutionRepository.FILE_ADD_SUCCESSFUL:
          return SaveFileStatus.OK;
        case ISolutionRepository.FILE_ADD_FAILED:
        case ISolutionRepository.FILE_ADD_INVALID_PUBLISH_PASSWORD:
        case ISolutionRepository.FILE_ADD_INVALID_USER_CREDENTIALS:
        default:
          return SaveFileStatus.FAIL;
        
      }
    } catch (PentahoAccessControlException e) {
      logger.error(e);
      return SaveFileStatus.FAIL;
    }
  }
  
  public boolean removeFile(String solutionPath){
     return getSolutionRepository().removeSolutionFile(solutionPath);
  }
  
  public boolean removeFileIfExists(String solutionPath){
    return !resourceExists(solutionPath) || removeFile(solutionPath);
  }
  
  public boolean resourceExists(String solutionPath){
    return getSolutionRepository().resourceExists(solutionPath, ISolutionRepository.ACTION_EXECUTE);
  }
  
  public boolean createFolder(String solutionFolderPath) throws IOException {
    solutionFolderPath = FilenameUtils.getFullPathNoEndSeparator(solutionFolderPath);//strip trailing / if there
    String folderName = FilenameUtils.getBaseName(solutionFolderPath);
    String folderPath = solutionFolderPath.substring(0, StringUtils.lastIndexOf(solutionFolderPath, folderName));
    return getSolutionRepositoryService().createFolder(userSession, "", folderPath, folderName, "");
  }
  
  public boolean canWrite(String filePath){
    ISolutionRepository solutionRepository = getSolutionRepository();
    //first check read permission
    ISolutionFile file = solutionRepository.getSolutionFile(filePath, ISolutionRepository.ACTION_EXECUTE);
    
    if(resourceExists(filePath))
    {
      return solutionRepository.hasAccess(file,ISolutionRepository.ACTION_UPDATE);
    }
    else 
    {
      return solutionRepository.hasAccess(file,ISolutionRepository.ACTION_CREATE);
    }
  }
  
  public boolean hasAccess(String filePath, FileAccess access){
    return getSolutionRepository().getSolutionFile(filePath, access.toResourceAction()) != null;
  }
  
  private ISolutionRepository getSolutionRepository() {
    return PentahoSystem.get(ISolutionRepository.class, userSession);
  }
  
  private ISolutionRepositoryService getSolutionRepositoryService(){
    return PentahoSystem.get(ISolutionRepositoryService.class, userSession);
  }

  public static RepositoryAccess getRepository() {
    return new RepositoryAccess(null);
  }

  public static RepositoryAccess getRepository(IPentahoSession userSession) {
    return new RepositoryAccess(userSession);
  }
  
  public InputStream getResourceInputStream(String filePath) throws FileNotFoundException {
    return getResourceInputStream(filePath, FileAccess.READ);
  }

  public InputStream getResourceInputStream(String filePath, FileAccess fileAccess) throws FileNotFoundException{
    return getResourceInputStream(filePath, fileAccess, true);
  }
  
  public InputStream getResourceInputStream(String filePath, FileAccess fileAccess, boolean getLocalizedResource) throws FileNotFoundException{
    return getSolutionRepository().getResourceInputStream(filePath,getLocalizedResource, fileAccess.toResourceAction());
  }

  public Document getResourceAsDocument(String solutionPath) throws IOException {
    return getResourceAsDocument(solutionPath, FileAccess.READ);
  }
  
  public Document getResourceAsDocument(String solutionPath, FileAccess fileAccess) throws IOException {
    return getSolutionRepository().getResourceAsDocument(solutionPath, fileAccess.toResourceAction());
  }
  
  public Document getFullSolutionTree(FileAccess access, ISolutionFilter filter ){
    return getSolutionRepository().getFullSolutionTree(access.toResourceAction(), filter);
  }

  public String getResourceAsString(String solutionPath) throws IOException {
   return getSolutionRepository().getResourceAsString(solutionPath, FileAccess.READ.toResourceAction());
  }

  public ISolutionFile getSolutionFile(String solutionPath, FileAccess access) {
    return getSolutionRepository().getSolutionFile(solutionPath, access.toResourceAction());
  }
  

}
