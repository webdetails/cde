package pt.webdetails.cdf.dd.render.components;

import org.apache.commons.jxpath.JXPathContext;

import pt.webdetails.cdf.dd.util.XPathUtils;

public class ParameterComponent extends BaseComponent
{

  public ParameterComponent(JXPathContext context)
  {
    super(context);
  }

  public ParameterComponent(JXPathContext context, String alias)
  {
    super(context, alias);
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
    String baseId = XPathUtils.getStringValue(getNode(), "properties/value[../name='name']").replace(" ", "_");
    setId(baseId);
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
    String name = getId();
    String viewRole = XPathUtils.getStringValue(context, "properties/value[../name='parameterViewRole']");
    String value = XPathUtils.getStringValue(context, "properties/value[../name='propertyValue']");
    if (bookmarkable)
    {
      result = "Dashboards.setBookmarkable('" + name + "');" + newLine;
    }
    else
    {
      result = "";
    }
    if (viewRole == "")
    {
      viewRole = "unused";
    }
    result += "Dashboards.setParameterViewMode('" + name + "','" + viewRole + "');" + newLine;
    return "var " + name + " = \"" + value + "\";" + newLine + result;
  }
}
