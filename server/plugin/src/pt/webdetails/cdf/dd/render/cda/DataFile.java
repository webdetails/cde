/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

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
