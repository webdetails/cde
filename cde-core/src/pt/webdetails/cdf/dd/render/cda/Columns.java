/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

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
public class Columns implements CdaElementRenderer {

  private JSONObject definition;

  public void renderInto(Element cols) {
    JSONArray columns = JSONArray.fromObject(definition.getString("value"));
    if (columns.size() == 0) {
      return;
    }
    Document doc = cols.getOwnerDocument();
    @SuppressWarnings("unchecked")
    Iterator<JSONArray> paramIterator = columns.iterator();
    while (paramIterator.hasNext()) {
      JSONArray content = paramIterator.next();
      Element col = doc.createElement("Column");
      Element name = doc.createElement("Name");
      col.setAttribute("idx", (String) content.get(0));
      name.appendChild(doc.createTextNode((String) content.get(1)));
      col.appendChild(name);
      cols.appendChild(col);
    }
  }

  public void setDefinition(JSONObject definition) {
    this.definition = definition;
  }
}
