/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.render.cda;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author pdpi
 */
public class Variables implements CdaElementRenderer
{

  private JSONObject definition;
  private JXPathContext context;

  public void renderInto(Element dataAccess)
  {
    JSONArray vars = JSONArray.fromObject(definition.getString("value"));
    if (vars.size() == 0)
    {
      return;
    }
    Document doc = dataAccess.getOwnerDocument();
    for (Object o : vars.toArray())
    {
      JSONArray jsa = (JSONArray) o;
      Element variable = doc.createElement("variables");
      if (!jsa.getString(0).equals(""))
      {
        variable.setAttribute("datarow-name", jsa.getString(0));
      }
      if (!jsa.getString(1).equals(""))
      {
        variable.setAttribute("variable-name", jsa.getString(1));
      }
      dataAccess.appendChild(variable);
    }
  }

  public void setDefinition(JSONObject definition)
  {
    this.definition = definition;
  }

  public void setContext(JXPathContext context)
  {
    this.context = context;
  }
}
