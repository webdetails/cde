/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.cda;

import net.sf.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author pdpi
 */
public class DataFile implements CdaElementRenderer
{

  private JSONObject definition;

  public void renderInto(Element connection)
  {
    Document doc = connection.getOwnerDocument();
    Element df = doc.createElement("DataFile");
    df.appendChild(doc.createTextNode((String) definition.get("value")));
    connection.appendChild(df);
  }

  public void setDefinition(JSONObject definition)
  {
    this.definition = definition;

  }
}
