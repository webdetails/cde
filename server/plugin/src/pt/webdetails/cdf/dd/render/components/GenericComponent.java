package pt.webdetails.cdf.dd.render.components;

import java.util.Collection;
import java.util.Iterator;

import java.util.HashMap;
import java.util.List;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.Node;

import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator;
import pt.webdetails.cdf.dd.util.XPathUtils;
import pt.webdetails.cdf.dd.render.properties.*;

public class GenericComponent extends BaseComponent implements IComponent
{

  private static Log log = LogFactory.getLog(DashboardDesignerContentGenerator.class);
  protected Node definition;
  private HashMap<String, GenericProperty> properties;
  protected String componentName;

  public GenericComponent(JXPathContext context)
  {
    super(context);
    this.id = "render_" + XPathUtils.getStringValue(getNode(), "properties/value[../name='name']").replace(" ", "_");
    this.properties = new HashMap<String, GenericProperty>();
  }

  public GenericComponent()
  {
    this.properties = new HashMap<String, GenericProperty>();
  }

  public void setDefinition(Node definition)
  {
    this.definition = definition;
    this.componentName = XmlDom4JHelper.getNodeText("Header/IName", definition);
    Collection<Node> nodes = definition.selectNodes("Contents/Model//Property");
    for (Node property : nodes)
    {
      String internalName = XmlDom4JHelper.getNodeText("@name", property);
      String propertyName = XmlDom4JHelper.getNodeText(".", property);
      // if the property isn't given a specific internal name, we default to its usual name
      if (internalName == null || internalName.equals(""))
      {
        internalName = propertyName;
      }
      GenericProperty prop = PropertyManager.getInstance().getPrivateProperty(this, propertyName);
      if (prop == null)
      {
        prop = PropertyManager.getInstance().getProperty(propertyName);
      }
      if (prop != null)
      {
        properties.put(propertyName, prop);
      }
      else
      {
        log.error("ERROR: Component " + this.getName() + " failed to resolve handler for property " + propertyName);
      }
    }
  }

  @Override
  public String getProperties()
  {

    addProperty("type", XmlDom4JHelper.getNodeText("Header/IName", definition), true);

    Collection<Node> nodes = definition.selectNodes("Contents/Model/*");
    for (Node property : nodes)
    {
      String nodeType = property.getName();//XmlDom4JHelper.getNodeText("name()", myNode);

      if (nodeType.equals("Property"))
      {
        String internalName = XmlDom4JHelper.getNodeText("@name", property);
        String propertyName = XmlDom4JHelper.getNodeText(".", property);
        // if the property isn't given a specific internal name, we default to its usual name
        if (internalName == null)
        {
          internalName = propertyName;
        }
        properties.put(internalName, PropertyManager.getInstance().getProperty(propertyName));
        String type = XmlDom4JHelper.getNodeText("@type", property);
        if (type.equals("string"))
        {
          addStringProperty(internalName, propertyName);
        }
        else if (type.equals("boolean"))
        {
          addBooleanProperty(internalName, propertyName);
        }
        else if (type.equals("number"))
        {
          addNumberProperty(internalName, propertyName);
        }
        else if (type.equals("function"))
        {
          addFunctionProperty(internalName, propertyName);
        }
      }
      else if (nodeType.equals("Definition"))
      {
        String name = XmlDom4JHelper.getNodeText("@name", property);
        addDefinition(name, property.selectNodes("*"));
      }
    }

    return super.getProperties();
  }

  public String getProperties(JXPathContext context)
  {
    StringBuilder values = new StringBuilder();
    values.append("\ttype: \"" + XmlDom4JHelper.getNodeText("Header/IName", definition) + "\"," + newLine);
    values.append("\tname: \"" + this.id + "\"," + newLine);
    List<Node> nodes = definition.selectNodes("Contents/Model/*");
    for (Node property : nodes)
    {
      if (property.getName().equals("Property"))
      {
        String outputName = XmlDom4JHelper.getNodeText("@name", property);
        String propertyName = XmlDom4JHelper.getNodeText(".", property);
        // if the property isn't given a specific name to use for output,
        // we default to its standard name
        if (outputName == null)
        {
          outputName = propertyName;
        }
        String value = XPathUtils.getStringValue(getNode(), "properties/value[../name='" + property + "']");
        GenericProperty prop = properties.get(propertyName);
        try
        {
          String renderedProperty = prop.render(outputName, getNode());
          if (!renderedProperty.equals(""))
          {
            values.append("\t" + renderedProperty);
          }
        }
        catch (Exception e)
        {
          log.error("ERROR: Component " + this.getName() + " failed to render property " + propertyName);
        }
      }
      else if (property.getName().equals("Definition"))
      {
        values.append(renderDefinition(XmlDom4JHelper.getNodeText("@name", property), property.selectNodes("*")));
      }
    }
    return values.toString().replaceAll(",$", "");
  }

  @Override
  public String render(JXPathContext context)
  {
    StringBuilder def = new StringBuilder();
    def.append("var " + getId() + " = {" + newLine);
    def.append(getProperties(context));
    def.append(newLine + "};" + newLine);
    return def.toString();
  }

  @Override
  public String render()
  {
    StringBuilder def = new StringBuilder();
    def.append("var " + getId() + " = {" + newLine);
    def.append(getProperties());
    def.append(newLine + "};" + newLine);
    return def.toString();
  }

  @Override
  public String getName()
  {
    return componentName;
  }

  @Override
  public String getEntry()
  {
    String iname = XmlDom4JHelper.getNodeText("Header/IName", definition);
    String name = XmlDom4JHelper.getNodeText("Header/Name", definition);
    String desc = XmlDom4JHelper.getNodeText("Header/Description", definition);
    String type = XmlDom4JHelper.getNodeText("Header/Type", definition);
    String cat = XmlDom4JHelper.getNodeText("Header/Category", definition);
    String catDesc = XmlDom4JHelper.getNodeText("Header/CatDescription", definition);


    String entryString =
            "        var " + iname + "Entry = " + type + ".extend({" + newLine
            + "		id: \"" + iname.toUpperCase() + "_ENTRY\"," + newLine
            + "		name: \"" + name + "\"," + newLine
            + "		description: \"" + desc + "\"," + newLine
            + "		category: \"" + cat + "\"," + newLine
            + "		categoryDesc: \"" + catDesc + "\"," + newLine
            + "		getStub: function(){" + newLine
            + "			 return Components" + iname + "Model.getStub();" + newLine
            + "		}" + newLine
            + "	});";
    return entryString;
  }

  @Override
  public String getModel()
  {
    String iname = XmlDom4JHelper.getNodeText("Header/IName", definition);
    String desc = XmlDom4JHelper.getNodeText("Header/Description", definition);


    StringBuilder metaTags = new StringBuilder();
    for (Object obj : definition.selectNodes("Metadata/*"))
    {
      Element node = (Element) obj;
      metaTags.append("		meta_" + node.attribute("name").getText() + ": \"" + node.getText() + "\"," + newLine);
    }


    StringBuilder output = new StringBuilder();


    output.append("var Components" + iname + "Model = BaseModel.extend({" + newLine
            + "	},{" + newLine
            + "		MODEL: 'Components" + iname + "'," + newLine
            + "		getStub: function(){ var _stub = { id: TableManager.generateGUID()," + newLine
            + "				type: Components" + iname + "Model.MODEL," + newLine
            + "				typeDesc: \"" + desc + "\"," + newLine
            + metaTags.toString()
            + "				parent: IndexManager.ROOTID, properties: [] };" + newLine
            + "			_stub.properties.push(PropertiesManager.getProperty(\"name\"));" + newLine);

    Iterator<Node> props = definition.selectNodes("Contents/Model//Property").iterator();
    while (props.hasNext())
    {
      Node prop = props.next();
      String propName = XmlDom4JHelper.getNodeText(".", prop);

      output.append("			_stub.properties.push(PropertiesManager.getProperty(\"" + propName + "\"));" + newLine);
    }
    output.append("			return _stub;" + newLine
            + "		}" + newLine
            + "	});" + newLine
            + "" + newLine
            + "BaseModel.registerModel(Components" + iname + "Model);" + newLine
            + "CDFDDComponentsArray.push(new " + iname + "Entry());" + newLine + newLine);


    return output.toString();
  }

  private void addBooleanProperty(String node, String property)
  {
    addProperty(node, XPathUtils.getBooleanValue(getNode(), "properties/value[../name='" + property + "']") ? "true" : "false", false);
  }

  private void addNumberProperty(String node, String property)
  {
    String value = XPathUtils.getStringValue(getNode(), "properties/value[../name='" + property + "']");
    addProperty(node, value, false);
  }

  private void addStringProperty(String node, String property)
  {
    String value = XPathUtils.getStringValue(getNode(), "properties/value[../name='" + property + "']");
    addProperty(node, value, true);
  }

  private void addFunctionProperty(String node, String property)
  {
    String value = XPathUtils.getStringValue(getNode(), "properties/value[../name='" + property + "']");
    addProperty(node, getFunctionParameter(value, false), false);
  }

  private void addDefinition(String node, List<Node> contents)
  {
    for (Node property : contents)
    {

      String type = XmlDom4JHelper.getNodeText("@type", property);
      // if the property isn't given a specific internal name, we default to its usual name
      String internalName = XmlDom4JHelper.getNodeText("@name", property);
      String propertyName = XmlDom4JHelper.getNodeText(".", property);
      if (internalName == null)
      {
        internalName = propertyName;
      }
      properties.put(internalName, PropertyManager.getInstance().getProperty(propertyName));
      if (type.equals("query"))
      {
        addQuery(node, XPathUtils.getStringValue(getNode(), "properties/value[../name='" + propertyName + "']"), getNode());
      }
      else if (type.equals("string"))
      {
        addDefinitionProperty(node, internalName, XPathUtils.getStringValue(getNode(), "properties/value[../name='" + propertyName + "']"), true);
      }
      else if (type.equals("function"))
      {
        addDefinitionProperty(node, internalName, getFunctionParameter(XPathUtils.getStringValue(getNode(), "properties/value[../name='" + propertyName + "']"), true), false);
      }
      else if (type.equals("number"))
      {
        addDefinitionProperty(node, internalName, XPathUtils.getStringValue(getNode(), "properties/value[../name='" + propertyName + "']"), false);
      }
      else if (type.equals("boolean"))
      {
        addDefinitionProperty(node, internalName, XPathUtils.getBooleanValue(getNode(), "properties/value[../name='" + propertyName + "']") ? "true" : "false", false);
      }
    }
  }

  private String renderDefinition(String node, List<Node> contents)
  {
    StringBuilder values = new StringBuilder();
    for (Node property : contents)
    {
      String outputName = XmlDom4JHelper.getNodeText("@name", property);
      String propertyName = XmlDom4JHelper.getNodeText(".", property);
      // if the property isn't given a specific name to use for output,
      // we default to its standard name
      if (outputName == null)
      {
        outputName = propertyName;
      }
      GenericProperty prop = properties.get(propertyName);
      try
      {
        String rendereredProperty = prop.render(outputName, getNode());
        if (!rendereredProperty.equals(""))
        {
          values.append("\t\t" + prop.render(outputName, getNode()));
        }
      }
      catch (Exception e)
      {
        log.error("ERROR: Component " + this.getName() + " failed to render property " + propertyName,e);
      }
    }
    String output = !values.toString().equals("")
            ? "\t" + node + ": {" + newLine + values.toString().replaceAll(",$", "") + "\t}," + newLine
            : "";
    return output;
  }

  public Node getDefinition()
  {
    return definition;
  }
}
