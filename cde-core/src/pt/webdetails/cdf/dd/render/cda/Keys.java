/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.render.cda;

import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.w3c.dom.Element;

/**
 *
 * @author pdpi
 */
public class Keys implements CdaElementRenderer {

  private JSONObject definition;

  public void renderInto(Element dataAccess) {
    JSONArray columns = JSONArray.fromObject(definition.getString("value"));
    @SuppressWarnings("unchecked")
    Iterator<String> paramIterator = columns.iterator();
    StringBuilder indexes = new StringBuilder();
    while (paramIterator.hasNext()) {
      String col = paramIterator.next();
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
