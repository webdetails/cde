
package pt.webdetails.cdf.dd.model.meta.writer.xml;

import org.dom4j.Branch;
import org.dom4j.Element;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;

/**
 * @author dcleao
 */
public class XmlPropertyTypeWriter implements IThingWriter
{
  public void write(java.lang.Object output, IThingWriteContext context, Thing t) throws ThingWriteException
  {
    PropertyType prop = (PropertyType)t;
    Branch parent = (Branch)output; // Element or Document

    boolean isAdvanced = PropertyType.CAT_ADVANCED.equals(prop.getCategory());

    Element propElem = parent.addElement("DesignerProperty");
    Element headerElem = propElem.addElement("Header");

    headerElem.addElement("Name").setText(prop.getName());
    headerElem.addElement("Parent").setText(prop.getBase());
    headerElem.addElement("DefaultValue").setText(prop.getDefaultValue()); // TODO
    headerElem.addElement("Description").setText(prop.getLabel());
    headerElem.addElement("Tooltip").setText(prop.getTooltip());
    headerElem.addElement("Advanced").setText(isAdvanced ? "true" : "false");
    headerElem.addElement("InputType").setText(prop.getInputType());
    headerElem.addElement("OutputType").setText(prop.getValueType().toString());
    headerElem.addElement("Order").setText(String.valueOf(prop.getOrder()));
    headerElem.addElement("Version").setText(prop.getVersion());
    headerElem.addElement("Visible").setText(prop.getVisible() ? "true" : "false");
  }
}