/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.render.cda;

import org.w3c.dom.Element;
import net.sf.json.JSONObject;

/**
 *
 * @author pdpi
 */
public interface CdaElementRenderer {

  public void renderInto(Element dataAccess);
  public void setDefinition(JSONObject definition);
}
