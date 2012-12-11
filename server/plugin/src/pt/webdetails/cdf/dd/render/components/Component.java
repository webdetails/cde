package pt.webdetails.cdf.dd.render.components;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.render.Renderer;

import pt.webdetails.cdf.dd.util.XPathUtils;

@SuppressWarnings("unchecked")
public abstract class Component
{

  public static final String newLine = System.getProperty("line.separator");
  protected static final Log logger = LogFactory.getLog(Component.class);
  private Hashtable<String, Object> properties = new Hashtable<String, Object>();
  private JXPathContext node;
  protected String id = "", alias = "", priority = "";

  public Component(JXPathContext node)
  {
    this(node, "");
  }

  public Component(JXPathContext node, String alias)
  {
    this.node = node;
    this.alias = alias;
  }

  protected void setId(String prefix, String alias, String baseId)
  {
    String parsedAlias = alias != null && alias.length() > 0 ? alias : "",
           parsedPrefix = prefix != null && prefix.length() > 0 ? prefix + "_"  : "";
    this.id = parsedPrefix + Renderer.aliasName(parsedAlias, baseId);
  }

  protected void setId(String prefix, String baseId)
  {
    this.setId(prefix, this.alias, baseId);
  }

  protected void setId(String baseId)
  {
    this.setId("", this.alias, baseId);
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }
  public Component()
  {
  }

  public JXPathContext getNode()
  {
    return node;
  }

  public void setNode(JXPathContext node)
  {
    this.node = node;
  }

  public abstract String render();

  public void clearProperties()
  {
    properties = new Hashtable();
  }

  public void addProperty(String key, String value, boolean encapsulateValue)
  {
    if (value.length() > 0)
    {
      value = getValue(value, encapsulateValue);
      if (!properties.containsKey("genericProperties"))
      {
        properties.put("genericProperties", new Vector());
      }
      ((Vector) properties.get("genericProperties")).add(key + " : " + value);
    }
  }

  public void addDefinitionProperty(String definition, String key, String value, boolean encapsulateValue)
  {
    if (value.length() > 0)
    {
      value = getValue(value, encapsulateValue);
      if (!properties.containsKey("definitionProperties"))
      {
        properties.put("definitionProperties", new Hashtable());
      }
      Hashtable definitionProperties = (Hashtable) properties.get("definitionProperties");
      if (!definitionProperties.containsKey(definition))
      {
        definitionProperties.put(definition, new Vector());
      }
      ((Vector) definitionProperties.get(definition)).add(key + " : " + value);
    }
  }

  public void addQuery(String definition, String id, JXPathContext node)
  {
    if (id.length() > 0)
    {
      Pointer pointer = node.getPointer("/datasources/rows[properties/name='name'][properties/value='" + id + "']");
      if (!(pointer instanceof NullPointer))
      {

        JXPathContext query = node.getRelativeContext(pointer);
        addDefinitionProperty(definition, "jndi", XPathUtils.getStringValue(query, "properties/value[../name='jndi']"), true);
        addDefinitionProperty(definition, "catalog", XPathUtils.getStringValue(query, "properties/value[../name='catalog']"), true);
        addDefinitionProperty(definition, "cube", XPathUtils.getStringValue(query, "properties/value[../name='cube']"), true);

        //String queryType = XPathUtils.getStringValue(query, "properties/value[../name='queryType']");
        String mdxQuery = XPathUtils.getStringValue(query, "properties/value[../name='mdxquery']");

        if (!mdxQuery.equals(""))
        {
          String processedQuery = getFunctionParameter(mdxQuery, true);
          addDefinitionProperty(definition, "query", processedQuery, false);
          addDefinitionProperty(definition, "queryType", "mdx", true);
        }
        else
        {
          String processedQuery = getFunctionParameter(XPathUtils.getStringValue(query, "properties/value[../name='sqlquery']"), true);
          addDefinitionProperty(definition, "query", processedQuery, false);
          addDefinitionProperty(definition, "queryType", "sql", true);
        }

      }
    }
  }

  public String getProperties()
  {

    String str = "";
    if (properties.containsKey("genericProperties"))
    {
      str = getProperties(((Vector<String>) properties.get("genericProperties")), " ");
    }

    if (properties.containsKey("definitionProperties"))
    {
      Hashtable definitions = (Hashtable) properties.get("definitionProperties");
      Enumeration e = definitions.keys();
      while (e.hasMoreElements())
      {
        String definition = (String) e.nextElement();
        Vector<String> properties = (Vector<String>) definitions.get(definition);
        str += "," + newLine + " " + definition + " : {" + newLine;
        str += getProperties(properties, "    ");
        str += newLine + " }";
      }
    }

    return str.replaceAll(",$", "");
  }

  public String getId()
  {
    return id;
  }

  public String getFunctionParameter(String stringValue, Boolean placeReturnString)
  {

    if (stringValue.matches("(?is)^ *function.*"))
    {
      return stringValue;
    }

    // It's a string; We need to encapsulate it:
    // 1 -> append/prepend function(){ return " / ";}
    // 2 -> remove all newlines
    // 3 -> change " with \"
    // 4 -> change ${} with " + ${} + "

    stringValue = stringValue.replace("\n", " ");
    stringValue = stringValue.replace("\r", " ");
    stringValue = stringValue.replace("\"", "\\\"");
    stringValue = stringValue.replaceAll("(\\$\\{[^}]*\\})", "\"+ $1 + \"");
    return "function(){" + (placeReturnString ? " return \"" + stringValue + "\"" : stringValue) + ";}";
  }

  private String getProperties(Vector<String> properties, String ident)
  {

    String str = "";
    Iterator propertyIterator = properties.iterator();
    while (propertyIterator.hasNext())
    {
      str += ident + propertyIterator.next() + "," + newLine;
    }
    return str.replaceAll("," + newLine + "$", "");
  }

  private String getValue(String value, boolean encapsulateValue)
  {
    String strValue = encapsulateValue ? "\"" + value + "\"" : value;
    Pattern pattern = Pattern.compile("\\$\\{[^}]*\\}");
    Matcher matcher = pattern.matcher(value);
    while (matcher.find())
    {
      String parameter = matcher.group();
      strValue = strValue.replace(matcher.group(), "Dashboards.ev(" + parameter.substring(2, parameter.length() - 1) + ")");
    }
    return strValue;
  }
}
