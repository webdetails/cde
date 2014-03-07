/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.render.cda;

import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author pdpi
 */
public class Parameters implements CdaElementRenderer {

  private JSONObject definition;
  private final String NAME_ATTR = "name",
                       DEFAULT_ATTR = "default",
                       TYPE_ATTR = "type",
                       ACCESS_ATTR = "access",
                       ELEMENT_NAME = "Parameter";

  public void renderInto(Element dataAccess) {
    JXPathContext.newContext(definition);
    Document doc = dataAccess.getOwnerDocument();
    Element parameters = doc.createElement("Parameters");
    dataAccess.appendChild(parameters);
    JSONArray params = JSONArray.fromObject(definition.getString("value"));
    @SuppressWarnings("unchecked")
    Iterator<JSONArray> paramIterator = params.iterator();
    while (paramIterator.hasNext()) {
      JSONArray param = (JSONArray) paramIterator.next();
      Element parameter = doc.createElement(ELEMENT_NAME);
      parameter.setAttribute(NAME_ATTR, (String) param.get(0));
      parameter.setAttribute(DEFAULT_ATTR, (String) param.get(1));
      if(param.size() > 2){
        parameter.setAttribute(TYPE_ATTR,  (String) param.get(2));
        if(param.size() > 3){
          String access = (String) param.get(3);
          if(!StringUtils.isEmpty(access)){
            parameter.setAttribute(ACCESS_ATTR, access);
          }
        }
      }
      else{
        parameter.setAttribute(TYPE_ATTR, "String");
      }
      
      parameters.appendChild(parameter);
    }
  }

  public void setDefinition(JSONObject definition) {
    this.definition = definition;
  }
}
