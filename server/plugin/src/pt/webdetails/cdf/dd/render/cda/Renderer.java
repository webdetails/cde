/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.webdetails.cdf.dd.render.cda;

import org.w3c.dom.Element;
import net.sf.json.JSONObject;

/**
 *
 * @author pdpi
 */
public interface Renderer {

  public void renderInto(Element dataAccess);
  public void setDefinition(JSONObject definition);
}
