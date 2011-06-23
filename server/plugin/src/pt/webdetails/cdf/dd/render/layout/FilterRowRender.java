package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;
import pt.webdetails.cdf.dd.util.XPathUtils;

public class FilterRowRender extends DivRender
{

  public FilterRowRender(JXPathContext context)
  {
    super(context);
  }

  public String renderClose()
  {
    return "</span></span>";
  }

  @Override
  public void processProperties()
  {
    super.processProperties();
    getPropertyBag().addId(getId());
  }

  @Override
  public String renderStart()
  {
    String id = getPropertyString("name"),
            label = getPropertyString("label");
    String div = "<span class='filter'><span class='label'>";
    div += label + ": </span><span id ='" + id + "' class='selector'>";
    return div;
  }

  protected String getId()
  {
    String id = getPropertyString("name");
    return id.length() > 0 ? id : XPathUtils.getStringValue(getNode(), "id");
  }
}
