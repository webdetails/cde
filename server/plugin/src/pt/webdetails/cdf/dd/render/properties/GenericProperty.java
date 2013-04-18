/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.properties;

import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Node;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import pt.webdetails.cdf.dd.render.renderer.GenericRenderer;
import pt.webdetails.cdf.dd.render.renderer.RendererManager;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cdf.dd.util.XPathUtils;

/**
 *
 * @author pdpi
 */
public class GenericProperty
{

  public static enum OutputType
  {

    STRING, BOOLEAN, NUMBER, FUNCTION, ARRAY, QUERY, LITERAL, VOID
  };

  public static enum RendererType
  {

    CUSTOM, VALUELIST, DYNAMICLIST
  }
  public static final String newLine = System.getProperty("line.separator");
  private HashMap<String, String> attributes;
  private String path;
  private String name;
  private Node definition;
  private String version;
  private OutputType type;
  private Pattern functionPattern;

  public GenericProperty(String path, Node doc)
  {
    this();
    init(path, doc);
  }

  public GenericProperty()
  {
    functionPattern = Pattern.compile("(?s)\"(function\\(.*?})\"");
  }

  public String getName()
  {
    return name;
  }

  public String getVersion()
  {
    return version;
  }

  public OutputType getOutputType()
  {
    return this.type;
  }

  public String getDefinition()
  {
    Set<String> attribs = attributes.keySet();

    StringBuilder entryString = new StringBuilder();
    entryString.append(
            "var " + this.name + "Property = BasePropertyType.extend({" + newLine
            + "\ttype: " + attributes.get("name") + "," + newLine
            + "\tstub: {" + newLine);
    for (String attrib : attribs)
    {
      entryString.append("\t\t" + attrib + ": " + attributes.get(attrib) + "," + newLine);
    }
    entryString.append(
            "\t}" + newLine
            + "});" + newLine
            + "PropertiesManager.register(new " + this.name + "Property());" + newLine);
    return entryString.toString();
  }

  public String render(String name, JXPathContext node)
  {
    if (node == null)
    {
      return "";
    }
    switch (this.type)
    {
      case ARRAY:
        return renderArray(name, node);
      case BOOLEAN:
        return renderBoolean(name, node);
      case FUNCTION:
        return renderFunction(name, node);
      case NUMBER:
        return renderNumber(name, node);
      case STRING:
        return renderString(name, node);
      case QUERY:
        return renderQuery(node);
      case LITERAL:
        return renderLiteral(name, node);
      case VOID:
        return "";
    }
    throw new TypeNotPresentException("", null);
  }

  private String renderString(String name, JXPathContext node)
  {
    String value = XPathUtils.getStringValue(node, "properties/value[../name='" + this.name.substring(0, 1).toLowerCase() + this.name.substring(1) + "']");
    if (value.equals(""))
    {
      return "";
    }

    if (value.charAt(0) != '\"')
    {
      value = "\"" + value + "\"";
    }
    //escape newlines
    value = StringUtils.replace(value, "\n", "\\n");
    value = StringUtils.replace(value, "\r", "\\r");

    return name + ": " + value + "," + newLine;
  }

  private String renderLiteral(String name, JXPathContext node)
  {
    String value = XPathUtils.getStringValue(node, "properties/value[../name='" + this.name.substring(0, 1).toLowerCase() + this.name.substring(1) + "']");
    if (value.equals(""))
    {
      return "";
    }
    return name + ": " + value + "," + newLine;
  }

  private String renderBoolean(String name, JXPathContext node)
  {
    String value = XPathUtils.getStringValue(node, "properties/value[../name='" + this.name + "']");
    if (value.equals(""))
    {
      return "";
    }
    return name + ": " + (value.equals("true") ? "true" : "false") + "," + newLine;
  }

  private String renderFunction(String name, JXPathContext node)
  {
    String value = XPathUtils.getStringValue(node, "properties/value[../name='" + this.name + "']");
    if (value.equals(""))
    {
      return "";
    }
    return name + ": " + getFunctionParameter(value, true) + "," + newLine;
  }

  private String renderNumber(String name, JXPathContext node)
  {
    String value = XPathUtils.getValue(node, "properties/value[../name='" + this.name + "']").toString();
    if (value.equals(""))
    {
      return "";
    }
    return name + ": " + value + "," + newLine;
  }

  private String renderArray(String name, JXPathContext node)
  {
    String value = XPathUtils.getStringValue(node, "properties/value[../name='" + this.name + "']");
    Matcher m = functionPattern.matcher(value);
    if (m.find())
    {
      StringBuffer sb = new StringBuffer();
      m.reset();
      while (m.find())
      {
        m.appendReplacement(sb, m.group(1).replaceAll("\\\"", "\"").replaceAll("\\$", "\\\\\\$"));
      }
      m.appendTail(sb);
      value = sb.toString();
    }

    if (value.equals(""))
    {
      return "";
    }
    return name + ": " + value + "," + newLine;
  }

  private String renderQuery(JXPathContext node)
  {
    throw new UnsupportedOperationException("Feature implemented in DatasourceProperty -- something went wrong!");
  }

  public void init(String path, Node doc)
  {
    this.definition = doc;
    String typeString = XmlDom4JHelper.getNodeText("Header/InputType/@type", definition);
    typeString = typeString == null ? "" : typeString.toUpperCase();
    RendererType rendererType = typeString.equals("") ? RendererType.CUSTOM : RendererType.valueOf(typeString);
    this.name = XmlDom4JHelper.getNodeText("Header/Name", definition);
    this.version = XmlDom4JHelper.getNodeText("Header/Version", definition);
    this.type = OutputType.valueOf(XmlDom4JHelper.getNodeText("Header/OutputType", definition).toUpperCase());
    this.path = path;
    attributes = new HashMap<String, String>();
    attributes.put("description", JsonUtils.toJsString(XmlDom4JHelper.getNodeText("Header/Description", definition)));
    attributes.put("tooltip",     JsonUtils.toJsString(XmlDom4JHelper.getNodeText("Header/Tooltip",     definition)));
    switch (rendererType)
    {
      case CUSTOM:
        attributes.put("type", JsonUtils.toJsString(XmlDom4JHelper.getNodeText("Header/InputType", definition)));
        break;
      case VALUELIST:
        GenericRenderer renderer = new GenericRenderer(this.definition);
        RendererManager rmanager = RendererManager.getInstance();
        rmanager.registerRenderer(renderer);
        attributes.put("type", JsonUtils.toJsString(renderer.getName()));
        break;
      case DYNAMICLIST:
        break;
    }
    attributes.put("order", XmlDom4JHelper.getNodeText("Header/Order", definition));
    String advanced = XmlDom4JHelper.getNodeText("Header/Advanced", definition);
    if (advanced != null && advanced.toLowerCase().equals("true"))
    {
      attributes.put("classType", "\"advanced\"");

    }
    String value = XmlDom4JHelper.getNodeText("Header/DefaultValue", definition);
    if (value.equals(""))
    {
      switch (this.type)
      {
        case ARRAY:
          value = "\"[]\"";
          break;
        case NUMBER:
          value = "\"\"";
          break;
        case BOOLEAN:
          value = "\"\"";
          break;
        case FUNCTION:
          value = "\"\"";
          break;
        case STRING:
          value = "\"\"";
          break;
        default:
          value = "";
      }
    }
    else if (this.type == OutputType.STRING)
    {
      value = "\"" + value + "\"";
    }
    attributes.put("value", value);
    String innerName = null;
    if(this.name != null && this.name.length() > 1){
      innerName = this.name.substring(0, 1).toLowerCase() + this.name.substring(1);
    }else{
      innerName = this.name.toLowerCase();
    }
    attributes.put("name", "\"" + innerName + "\"");
  }
  
  protected String replaceParameters(String value)
  {
    Pattern pattern = Pattern.compile("\\$\\{[^}]*\\}");
    Matcher matcher = pattern.matcher(value);
    while (matcher.find())
    {
      String parameter = matcher.group();
      value = value.replace(matcher.group(), "Dashboards.ev(" + parameter.substring(2, parameter.length() - 1) + ")");
    }
    return value;
  }

  protected String getFunctionParameter(String stringValue, Boolean placeReturnString)
  {
	// REGEX: (\"|\s)*function\s*\u0028.*\u0029{1}\s*\u007b.*(\u007d(\"|\s)*)?|(\"|\s)*function\s*[a-zA-Z0-9\u002d\u005f]+\u0028.*\u0029{1}\s*\u007b.*(\u007d(\"|\s)*)?
	// JAVA STRING REGEX: "(\\\"|\\s)*function\\s*\\u0028.*\\u0029{1}\\s*\\u007b.*(\\u007d(\\\"|\\s)*)?|(\\\"|\\s)*function\\s*[a-zA-Z0-9\\u002d\\u005f]+\\u0028.*\\u0029{1}\\s*\\u007b.*(\\u007d(\\\"|\\s)*)?" 
	Pattern pattern = Pattern.compile("(\\\"|\\s)*function\\s*\\u0028.*\\u0029{1}\\s*\\u007b.*(\\u007d(\\\"|\\s)*)?|(\\\"|\\s)*function\\s*[a-zA-Z0-9\\u002d\\u005f]+\\u0028.*\\u0029{1}\\s*\\u007b.*(\\u007d(\\\"|\\s)*)?");
	Matcher matcher = pattern.matcher(stringValue);

	if (matcher.find()) 
	{
	  return stringValue;
	}

	// if (stringValue.matches("(?is)^ *function.*")) {return stringValue; } 
	  
    // It's a string; We need to encapsulate it:
    // 1 -> remove all newlines
    // 2 -> change " with \"
    // 3 -> change ${} with " + ${} + "
    // 4 -> append/prepend function(){ return " / ";}
	stringValue = stringValue.replace("\n", " ");
    stringValue = stringValue.replace("\r", " ");
    stringValue = stringValue.replace("\"", "\\\"");
    stringValue = stringValue.replaceAll("(\\$\\{[^}]*\\})", "\"+ $1 + \"");
    return "function(){" + (placeReturnString ? " return \"" + stringValue + "\"" : stringValue) + ";}";
    
  }

  private void matcher(Pattern regex) {
    // TODO Auto-generated method stub
  }
}
