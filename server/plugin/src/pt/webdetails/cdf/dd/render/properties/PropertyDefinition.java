/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.properties;

import org.dom4j.Element;
import org.dom4j.Document;

/**
 *
 * @author pdpi
 */
public class PropertyDefinition
{

  private String name,
          base = "BaseProperty",
          defaultValue = "",
          description = "",
          tooltip = "",
          inputType = "String",
          outputType = "String";
  private boolean advanced = false;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getParent()
  {
    return base;
  }

  public void setParent(String base)
  {
    this.base = base;
  }

  public String getDefaultValue()
  {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue)
  {
    this.defaultValue = defaultValue;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getTooltip()
  {
    return tooltip;
  }

  public void setTooltip(String tooltip)
  {
    this.tooltip = tooltip;
  }

  public String getInputType()
  {
    return inputType;
  }

  public void setInputType(String inputType)
  {
    this.inputType = inputType;
  }

  public String getOutputType()
  {
    return outputType;
  }

  public void setOutputType(String outputType)
  {
    this.outputType = outputType;
  }

  public boolean isAdvanced()
  {
    return advanced;
  }

  public void setAdvanced(boolean advanced)
  {
    this.advanced = advanced;
  }

  public Element toXML(Element target)
  {
    Element el = target.addElement("DesignerProperty");
    Element header = el.addElement("Header");
    header.addElement("Name").setText(name);
    header.addElement("Parent").setText(base != null ? base : "BaseProperty");
    header.addElement("DefaultValue").setText(defaultValue != null ? defaultValue : "");
    header.addElement("Description").setText(description != null ? description : "");
    header.addElement("Tooltip").setText(tooltip != null ? tooltip : "");
    header.addElement("Advanced").setText(advanced ? "true" : "false");
    header.addElement("InputType").setText(inputType != null ? inputType : "String");
    header.addElement("OutputType").setText(outputType != null ? outputType : "String");
    header.addElement("Order").setText("1");
    header.addElement("Version").setText("1.0");
    return el;
  }
}
