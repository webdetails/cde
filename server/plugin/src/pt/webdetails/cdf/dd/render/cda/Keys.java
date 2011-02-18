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
public class Keys implements CdaElementRenderer {

  private JSONObject definition;

  public void renderInto(Element dataAccess) {
    JSONArray columns = JSONArray.fromObject(definition.getString("value"));
    Iterator paramIterator = columns.iterator();
    StringBuilder indexes = new StringBuilder();
    while (paramIterator.hasNext()) {
      String col = (String) paramIterator.next();
      indexes.append(col);
      if (paramIterator.hasNext()) {
        indexes.append(",");
      }
    }
    dataAccess.setAttribute("keys", indexes.toString());
  }

  public void setDefinition(JSONObject definition) {
    this.definition = definition;
  }
}
