/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.datasources;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.jxpath.JXPathContext;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.structure.XmlStructure;

/**
 *
 * @author pdpi
 */
public class CdaManager {
  public static final String SOLUTION_PATH = XmlStructure.SOLUTION_PATH;
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
    JXPathContext context;
    try {
      ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);
      JSONObject json = (JSONObject) JSONSerializer.toJSON(jsonString);
      context = JXPathContext.newContext(json);
      Document doc = DocumentFactory.getInstance().createDocument();
      Iterator it = context.iteratePointers("/components/rows");
      int status = solutionRepository.publish(SOLUTION_PATH, file[0], file[1], json.toString().getBytes("UTF-8"), true);
    } catch (PentahoAccessControlException ex) {
      Logger.getLogger(CdaManager.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
