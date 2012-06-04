/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

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
