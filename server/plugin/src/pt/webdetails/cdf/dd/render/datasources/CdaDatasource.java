/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.datasources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.dom4j.Node;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import pt.webdetails.cdf.dd.render.components.BaseComponent;

/**
 *
 * @author pdpi
 */
public class CdaDatasource extends BaseComponent {

  private JSONObject def;
  private String name;
  private String iname;

  public CdaDatasource(Pointer pointer) {
    this.def = (JSONObject) pointer.getNode();
    this.iname = pointer.asPath().replaceAll(".*name='(.*?)'.*", "$1");
    JXPathContext context = JXPathContext.newContext(def);
    this.name = ((String) context.getValue("metadata/name", String.class));

  }

  @Override
  public String getEntry() {
    JXPathContext context = JXPathContext.newContext(def);
    String group = ((String) context.getValue("metadata/group", String.class));
    String groupDesc = ((String) context.getValue("metadata/groupdesc", String.class));
    String desc = name;

    //String cat = XmlDom4JHelper.getNodeText("//DesignerComponent/Header/Category", definition);
    //String catDesc = XmlDom4JHelper.getNodeText("//DesignerComponent/Header/CatDescription", definition);

    String entryString =
            "        var " + iname + "Entry = PalleteEntry.extend({" + newLine
            + "		id: \"" + iname.toUpperCase() + "_ENTRY\"," + newLine
            + "		name: \"" + name + "\"," + newLine
            + "		description: \"" + desc + "\"," + newLine
            + "		category: \"" + group + "\"," + newLine
            + "		categoryDesc: \"" + groupDesc + "\"," + newLine
            + "		getStub: function(){" + newLine
            + "			 return Components" + iname + "Model.getStub();" + newLine
            + "		}" + newLine
            + "	});";
    return entryString;
  }

  @Override
  public String getModel() {

    String desc = "";
    JXPathContext context = JXPathContext.newContext(def);
    String connType = (String) context.getValue("metadata/conntype");
    String daType = (String) context.getValue("metadata/datype");
    StringBuilder output = new StringBuilder();
    output.append("var Components" + iname + "Model = BaseModel.extend({" + newLine
            + "	},{" + newLine
            + "		MODEL: 'Components" + iname + "'," + newLine
            + "		getStub: function(){ var _stub = { id: TableManager.generateGUID()," + newLine
            + "				type: Components" + iname + "Model.MODEL," + newLine
            + "				typeDesc: \"" + name + "\"," + newLine
            + "				meta: \"CDA\"," + newLine
            + "				meta_conntype: \"" + connType + "\"," + newLine
            + "				meta_datype: \"" + daType + "\"," + newLine
            + "				parent: IndexManager.ROOTID, properties: [] };" + newLine
            + "			_stub.properties.push(PropertiesManager.getProperty(\"name\"));" + newLine);

    String[] props = generateProperties();
    for (String prop : props) {
      if (prop.equals("id") || prop.equals("connection")) {
        continue;
      } else if (prop.equals("columns")) {
        output.append("			_stub.properties.push(PropertiesManager.getProperty(\"cdacolumns\"));" + newLine);
        output.append("			_stub.properties.push(PropertiesManager.getProperty(\"cdacalculatedcolumns\"));" + newLine);
      } else if (prop.equals("output")) {
        output.append("			_stub.properties.push(PropertiesManager.getProperty(\"output\"));" + newLine);
        output.append("			_stub.properties.push(PropertiesManager.getProperty(\"outputMode\"));" + newLine);
      } else if (prop.equals("left")) {
        output.append("			_stub.properties.push(PropertiesManager.getProperty(\"left\"));" + newLine);
        output.append("			_stub.properties.push(PropertiesManager.getProperty(\"leftkeys\"));" + newLine);
      } else if (prop.equals("right")) {
        output.append("			_stub.properties.push(PropertiesManager.getProperty(\"right\"));" + newLine);
        output.append("			_stub.properties.push(PropertiesManager.getProperty(\"rightkeys\"));" + newLine);
      } else {
        output.append("			_stub.properties.push(PropertiesManager.getProperty(\"" + prop + "\"));" + newLine);
      }
    }
    output.append("			return _stub;" + newLine
            + "		}" + newLine
            + "	});" + newLine
            + "" + newLine
            + "BaseModel.registerModel(Components" + iname + "Model);" + newLine
            + "CDFDDDatasourcesArray.push(new " + iname + "Entry());" + newLine + newLine);
    return output.toString();
  }

  @Override
  public String render() {
    throw new UnsupportedOperationException("Datasources can't be rendered.");
  }

  private String[] generateProperties() {
    ArrayList<String> props = new ArrayList<String>();
    JXPathContext context = JXPathContext.newContext(def);
    JSONObject connection = ((JSONObject) context.getValue("definition/connection", JSONObject.class));
    if (connection != null) {
      props.addAll(connection.keySet());
    }
    JSONObject dataaccess = ((JSONObject) context.getValue("definition/dataaccess", JSONObject.class));
    if (dataaccess != null) {
      props.addAll(dataaccess.keySet());
    }
    //props.remove("connection");
    return props.toArray(new String[props.size()]);
  }

  @Override
  public String getName() {
    return name;
  }
}
