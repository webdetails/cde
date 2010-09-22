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
public class CompoundComponent implements Renderer {

  private JSONObject definition;

  public void renderInto(Element compound) {
    JXPathContext context = JXPathContext.newContext(definition);
    compound.setAttribute("id", (String) context.getValue("value", String.class));
  }

  public void setDefinition(JSONObject definition) {
    this.definition = definition;
  }

  private static String capitalize(String s) {
    if (s.length() == 0) {
      return s;
    }
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
  }
}
