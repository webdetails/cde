/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.datasources;

import java.util.ArrayList;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

/**
 *
 * @author pdpi
 */
public class CdaDatasource extends BaseDataSource {

  private String iname;

  public CdaDatasource(Pointer pointer) {
    setDefinition((JSONObject) pointer.getNode());
    this.iname = pointer.asPath().replaceAll(".*name='(.*?)'.*", "$1");
    JXPathContext context = JXPathContext.newContext(this.definition);
    setName((String) context.getValue("metadata/name", String.class));

  }

  @Override
  public String getEntry() {
    JXPathContext context = JXPathContext.newContext(this.definition);
    String group = ((String) context.getValue("metadata/group", String.class));
    String groupDesc = ((String) context.getValue("metadata/groupdesc", String.class));
    String desc = name;

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
    JXPathContext context = JXPathContext.newContext(this.definition);
    String connType = (String) context.getValue("metadata/conntype");
    connType = connType != null ? connType : "";
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
      } else if(name.equalsIgnoreCase("kettle over kettleTransFromFile") && prop.equalsIgnoreCase("query")){
          output.append("			var customQueryProp = PropertiesManager.getProperty(\"kettleQuery\");"+newLine);
          output.append("			customQueryProp.name = \""+prop+"\";"+newLine);
          output.append("			_stub.properties.push(customQueryProp);"+newLine);
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

  @SuppressWarnings("unchecked")
  private String[] generateProperties() {
    ArrayList<String> props = new ArrayList<String>();
    JXPathContext context = JXPathContext.newContext(this.definition);
    JSONObject connection = ((JSONObject) context.getValue("definition/connection", JSONObject.class));
    if (connection != null) {
      props.addAll(connection.keySet());
    }
    JSONObject dataaccess = ((JSONObject) context.getValue("definition/dataaccess", JSONObject.class));
    if (dataaccess != null) {
      props.addAll(dataaccess.keySet());
    }
    
    return props.toArray(new String[props.size()]);
  }

}
