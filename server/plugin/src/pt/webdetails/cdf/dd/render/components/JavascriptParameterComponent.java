package pt.webdetails.cdf.dd.render.components;

import org.apache.commons.jxpath.JXPathContext;

import pt.webdetails.cdf.dd.render.Renderer;
import pt.webdetails.cdf.dd.util.XPathUtils;

public class JavascriptParameterComponent extends BaseComponent
{

  public JavascriptParameterComponent(JXPathContext context)
  {
    this(context, "");
  }

  public JavascriptParameterComponent(JXPathContext context, String alias)
  {
    super(context, alias);
  }

  public JavascriptParameterComponent()
  {
    super();
  }

  
 @Override
  protected void setId(String prefix, String alias, String baseId)
  {
    String parsedAlias = alias != null && alias.length() > 0 ? alias : "",
           parsedPrefix = prefix != null && prefix.length() > 0 ? prefix + "_"  : "";
    
    //Do not alias Dashboard.storage ids
    if (baseId.startsWith("Dashboards.storage"))
      parsedAlias = "";
    this.id = parsedPrefix + Renderer.aliasName(parsedAlias, baseId);
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
    String baseId = XPathUtils.getStringValue(getNode(), "properties/value[../name='name']").replace(" ", "_");
    setId(baseId);
  }

  @Override
  public String getName()
  {
    return "JavascriptParameter";
  }
}
