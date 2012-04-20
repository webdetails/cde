/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.datasources;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import pt.webdetails.cpf.repository.RepositoryAccess;

/**
 *
 * @author pdpi
 */
public class CdaManager {
  
  private static Log logger = LogFactory.getLog(CdaManager.class);
  
  public static CdaManager _engine;
  

  public CdaManager() {
    init("");
  }

  public CdaManager(String path) {
    init(path);
  }

  public static synchronized CdaManager getInstance() {
    if (_engine == null) {
      _engine = new CdaManager();
    }
    return _engine;
  }

  private void init(String path) {
  }

  public void saveDefinition (String[] file, String jsonString, IPentahoSession userSession) throws Exception {


      JSONObject json = (JSONObject) JSONSerializer.toJSON(jsonString);
      JXPathContext.newContext(json);//TODO:needed?
//      Document doc = DocumentFactory.getInstance().createDocument();
//      Iterator<Pointer> it = context.iteratePointers("/components/rows");
      
      switch(RepositoryAccess.getRepository(userSession).publishFile(file[0], file[1], json.toString().getBytes("UTF-8"), true)){
        case FAIL:
          logger.error("Could not save definition " + StringUtils.join(file, "/"));
      }

  }
}
