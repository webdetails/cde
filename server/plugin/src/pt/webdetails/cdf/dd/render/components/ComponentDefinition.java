/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import pt.webdetails.cdf.dd.render.properties.PropertyDefinition;

/**
 *
 * @author pdpi
 */
public class ComponentDefinition
{

  private String implementation,
          name = "",
          description = "",
          category  = "OTHERCOMPONENTS",
          catDescription = "Others";

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getCategory()
  {
    return category;
  }

  public void setCategory(String category)
  {
    this.category = category;
  }

  public String getCatDescription()
  {
    return catDescription;
  }

  public void setCatDescription(String catDescription)
  {
    this.catDescription = catDescription;
  }
  private Map<String, String> metadata;
  private List<PropertyDefinition> customProperties;
  private List<Property> properties;
  private List<Dependency> dependencies;
  private List<Dependency> styles;

  public ComponentDefinition()
  {
    customProperties = new ArrayList<PropertyDefinition>();
    properties = new ArrayList<Property>();
    dependencies = new ArrayList<Dependency>();
    styles = new ArrayList<Dependency>();
    metadata = new HashMap<String, String>();
  }

  public void addMetadata(String name, String value)
  {
    metadata.put(name,value);
  }

  public void addProperty(String name, PropertyDefinition prop)
  {
    Property property = new Property();
    property.name = prop.getName();
    property.attributes.put("name", name);
    properties.add(property);
    customProperties.add(prop);
  }

  public void addProperty(String name)
  {
    Property property = new Property();
    property.name = name;
    properties.add(property);
  }

  public Element toXML(Document doc)
  {
    Element el = doc.addElement("DesignerComponent");
    Element contents = el.addElement("Contents");

    drawHeader(el);
    drawMetadata(el);
    drawModel(contents);
    addImplementation(contents);
    return el;
  }

  private void drawHeader(Element el)
  {
    Element header = el.addElement("Header");
    header.addElement("Name").setText(name);
    header.addElement("IName").setText(name);
    header.addElement("Description").setText(description);
    header.addElement("Category").setText(category);
    header.addElement("CatDescription").setText(catDescription);
    header.addElement("Type").setText("PalleteEntry");
    header.addElement("Version").setText("1.0");
  }

  private void drawMetadata(Element el)
  {
    Element header = el.addElement("Metadata");
    for (String name : metadata.keySet())
    {
      Element foo = header.addElement("meta");
      foo.setText(metadata.get(name));
      foo.addAttribute("name", name);
    }

  }

  private void drawModel(Element contents)
  {
    Element model = contents.addElement("Model");
    for (Property prop : properties)
    {
      Element propertyElement = model.addElement("Property");
      propertyElement.setText(prop.name);
      for (String k : prop.attributes.keySet())
      {
        propertyElement.addAttribute(k, prop.attributes.get(k));
      }
    }
  }

  private void addImplementation(Element el)
  {
    Element impl = el.addElement("Implementation");
    impl.addElement("Code").addAttribute("src", implementation);

    /* Scripts */
    Element deps = impl.addElement("Dependencies");
    for (Dependency dep : dependencies)
    {
      Element delEl = deps.addElement("Dependency");
      delEl.addAttribute("src", dep.src);
      delEl.addAttribute("version", dep.version);
      delEl.setText(dep.name);
    }

    /* Styles */
    Element stylesEl = impl.addElement("Styles");
    for (Dependency dep : styles)
    {
      Element delEl = stylesEl.addElement("Style");
      delEl.addAttribute("src", dep.src);
      delEl.addAttribute("version", dep.version);
      delEl.setText(dep.name);
    }
    /* Custom Properties */
    Element cp = impl.addElement("CustomProperties");
    for (PropertyDefinition prop : customProperties)
    {
      prop.toXML(cp);
    }

  }

  private class Property
  {

    Map<String, String> attributes;
    String name;

    Property()
    {
      attributes = new HashMap<String, String>();
      name = "";
    }
  }

  private class Dependency
  {

    String name, version, src;

    Dependency()
    {
    }
  }
}
