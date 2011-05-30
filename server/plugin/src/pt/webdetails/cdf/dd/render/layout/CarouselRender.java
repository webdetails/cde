package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;
import pt.webdetails.cdf.dd.util.XPathUtils;

public class CarouselRender extends DivRender
{

  public CarouselRender(JXPathContext context)
  {
    super(context);
  }

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
    getPropertyBag().addClass("hidden");
 
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
