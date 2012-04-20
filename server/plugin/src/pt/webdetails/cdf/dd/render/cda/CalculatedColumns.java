/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.cda;

import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author pdpi
 */
public class CalculatedColumns implements CdaElementRenderer {

  private JSONObject definition;

  public void renderInto(Element cols) {
    JSONArray columns = JSONArray.fromObject(definition.getString("value"));
    if (columns.size() == 0) {
      return;
    }
    Document doc = cols.getOwnerDocument();
    Iterator<JSONArray> paramIterator = columns.iterator();
    while (paramIterator.hasNext()) {
      JSONArray content = paramIterator.next();
      Element col = doc.createElement("CalculatedColumn");
      Element name = doc.createElement("Name");
      Element formula = doc.createElement("Formula");
      name.appendChild(doc.createTextNode((String) content.get(0)));
      formula.appendChild(doc.createTextNode((String) content.get(1)));
      col.appendChild(name);
      col.appendChild(formula);
      cols.appendChild(col);
    }
  }

  public void setDefinition(JSONObject definition) {
    this.definition = definition;
  }
}
