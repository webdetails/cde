/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.properties;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.dom4j.Node;
import pt.webdetails.cdf.dd.util.XPathUtils;

/**
 *
 * @author pdpi
 */
public class CdaDataSourceProperty extends GenericProperty {

  public CdaDataSourceProperty(String path, Node doc) {
    // We don't actually want this constructor to do stuff
    super();
  }

  public CdaDataSourceProperty() {
    super();
  }

  @Override
  public String getDefinition() {
    return "";
  }

  @Override
  public String render(String name, JXPathContext node) {
    StringBuilder output = new StringBuilder();
    if (name.length() > 0) {
      String queryName = XPathUtils.getStringValue(node, "properties/value[../name='" + getName() + "']");
      if (queryName.length() == 0) {
        return "";
      }
      Pointer pointer = node.getPointer("/datasources/rows[properties/name='name'][properties/value='" + queryName + "']");
      if (!(pointer instanceof NullPointer)) {

        JXPathContext query = node.getRelativeContext(pointer);
        output.append("dataAccessId: \"" + XPathUtils.getStringValue(query, "properties/value[../name='dataAccessId']") + "\"," + newLine);
        output.append("solution: \"" + XPathUtils.getStringValue(query, "properties/value[../name='solution']") + "\"," + newLine);
        output.append("path: \"" + XPathUtils.getStringValue(query, "properties/value[../name='path']") + "\"," + newLine);
        output.append("file: \"" + XPathUtils.getStringValue(query, "properties/value[../name='file']") + "\"," + newLine);
      }

    }
    return replaceParameters(output.toString());
  }

  @Override
  public String getName() {
    return "cdaDataSource";
  }
}

