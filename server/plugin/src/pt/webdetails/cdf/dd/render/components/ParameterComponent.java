package pt.webdetails.cdf.dd.render.components;

import org.apache.commons.jxpath.JXPathContext;

import pt.webdetails.cdf.dd.util.XPathUtils;

public class ParameterComponent extends BaseComponent
{

  public ParameterComponent(JXPathContext context)
  {
    super(context);
  }

  public ParameterComponent()
  {
    super();
  }

  @Override
  public void setNode(JXPathContext node)
  {
    clearProperties();
    super.setNode(node);
    this.id = XPathUtils.getStringValue(getNode(), "properties/value[../name='name']").replace(" ", "_");
  }

  @Override
  public String getName()
  {
    return "Parameter";
  }

  @Override
  public String render()
  {
   return render(getNode());
  }

  @Override
  public String render(JXPathContext context)
  {
        String result;
    boolean bookmarkable = XPathUtils.getBooleanValue(context, "properties/value[../name='bookmarkable']");
    String name = XPathUtils.getStringValue(context, "properties/value[../name='name']");
    String value = XPathUtils.getStringValue(context, "properties/value[../name='propertyValue']");
    if (bookmarkable)
    {
      result = "Dashboards.setBookmarkable('" + name + "');" + newLine;
    }
    else
    {
      result = "";
    }
    return "var " + name + " = \"" + value + "\";" + newLine + result;
  }
}
