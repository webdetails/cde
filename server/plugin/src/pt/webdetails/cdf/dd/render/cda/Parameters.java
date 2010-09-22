/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.cda;

import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author pdpi
 */
public class Parameters implements Renderer {

  private JSONObject definition;

  public void renderInto(Element dataAccess) {
    JXPathContext context = JXPathContext.newContext(definition);
    Document doc = dataAccess.getOwnerDocument();
    Element parameters = doc.createElement("Parameters");
    dataAccess.appendChild(parameters);
    JSONArray params = JSONArray.fromObject(definition.getString("value"));
    Iterator paramIterator = params.iterator();
    while (paramIterator.hasNext()) {
      JSONArray param = (JSONArray) paramIterator.next();
      Element parameter = doc.createElement("Parameter");
      parameter.setAttribute("type", "String");
      parameter.setAttribute("name", (String) param.get(0));
      parameter.setAttribute("default", (String) param.get(1));
      parameters.appendChild(parameter);
    }
  }

  public void setDefinition(JSONObject definition) {
    this.definition = definition;
  }
}
