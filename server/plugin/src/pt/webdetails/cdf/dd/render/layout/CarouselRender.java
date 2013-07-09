/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;
import pt.webdetails.cdf.dd.util.XPathUtils;

public class CarouselRender extends DivRender
{

  public CarouselRender(JXPathContext context)
  {
    super(context);
  }

  @Override
  public String renderClose()
  {
    return "</ul></div>";
  }

  @Override
  public void processProperties()
  {
    super.processProperties();
    getPropertyBag().addId(getId());
    getPropertyBag().addClass("cdfCarousel");
    if (getPropertyBoolean("showTitle"))
    {
      getPropertyBag().addClass("showTitle");
    }
    getPropertyBag().addClass("cdfCarousel");

  }

  @Override
  public String renderStart()
  {

    String div = "<div class='cdfCarouselHolder'><ul ";
    div += getPropertyBagString() + ">";
    return div;
  }

  protected String getId()
  {
    String id = getPropertyString("name");
    return id.length() > 0 ? id : XPathUtils.getStringValue(getNode(), "id");
  }
}
