/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.cda;

import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Element;

/**
 *
 * @author pdpi
 */
public class CompoundComponent implements CdaElementRenderer {

  private JSONObject definition;

  public void renderInto(Element compound) {
    JXPathContext context = JXPathContext.newContext(definition);
    compound.setAttribute("id", (String) context.getValue("value", String.class));
  }

  public void setDefinition(JSONObject definition) {
    this.definition = definition;
  }

}
