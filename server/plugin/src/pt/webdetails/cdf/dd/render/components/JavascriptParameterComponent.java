package pt.webdetails.cdf.dd.render.components;

import org.apache.commons.jxpath.JXPathContext;

import pt.webdetails.cdf.dd.util.XPathUtils;

public class JavascriptParameterComponent extends BaseComponent
{

  public JavascriptParameterComponent(JXPathContext context)
  {
    super(context);
  }

  public JavascriptParameterComponent()
  {
    super();
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
    String value = XPathUtils.getStringValue(context, "properties/value[../name='javaScript']");
    if (bookmarkable)
    {
      result = "Dashboards.setBookmarkable('" + name + "');" + newLine;
    }
    else
    {
      result = "";
    }
    return name + " = " + value + ";" + newLine + result;
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
    return "JavascriptParameter";
  }
}
