/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;
import pt.webdetails.cdf.dd.util.XPathUtils;

public class FilterHeaderRender extends DivRender
{

  public FilterHeaderRender(JXPathContext context)
  {
    super(context);
  }

  @Override
  public String renderClose()
  {
    return "</h3>";
  }


  @Override
  public String renderStart()
  {
    return "<h3>" + getPropertyString("title");
  }

  protected String getId()
  {
    String id = getPropertyString("name");
    return id.length() > 0 ? id : XPathUtils.getStringValue(getNode(), "id");
  }
}
