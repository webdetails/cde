package pt.webdetails.cdf.dd.render.components;

import org.apache.commons.jxpath.JXPathContext;

import pt.webdetails.cdf.dd.util.XPathUtils;

public class FunctionComponent extends BaseComponent
{

  public FunctionComponent(JXPathContext context, String alias)
  {
    super(context, alias);
  }
  public FunctionComponent(JXPathContext context)
  {
    this(context, "");
  }
  public void setNode(JXPathContext node)
  {
    setNode(node,"");
  }

  public void setNode(JXPathContext node, String alias)
  {
    clearProperties();
    super.setNode(node);
    String baseId = XPathUtils.getStringValue(getNode(), "properties/value[../name='name']").replace(" ", "_");
    setId(baseId);
  }

  public FunctionComponent()
  {
    super();
  }

  public String render()
  {
    return XPathUtils.getStringValue(getNode(), "properties/value[../name='javaScript']") + newLine;
  }

  public String render(JXPathContext context)
  {
    return XPathUtils.getStringValue(context, "properties/value[../name='javaScript']") + newLine;
  }

  public String getName()
  {
    return "Function";
  }
}
