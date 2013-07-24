/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.datasources;

import net.sf.json.JSONObject;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

/**
 *
 * @author Rafael P. Gomes<rafael.gomes@webdetails.pt>
 */
public class CpkDataSource extends BaseDataSource {
  
  private String pluginId;
  private String endpoint;

  public CpkDataSource(Pointer pointer) {
    this.definition = (JSONObject) pointer.getNode();
    this.iname = pointer.asPath().replaceAll(".*name='(.*?)'.*", "$1");
    JXPathContext context = JXPathContext.newContext(definition);
    this.name = ((String) context.getValue("metadata/name", String.class));
    
    this.pluginId = (String) context.getValue("metadata/pluginId", String.class);
    this.endpoint = (String) context.getValue("metadata/endpoint", String.class);
  }

  @Override
  public String getModel() {
    String model = "var Components" + iname + "Model = BaseModel.extend({},{" + newLine
        + "\tMODEL: 'Components" + iname + "'," + newLine
        + "\t" + "getStub: function () {" + newLine
        + "\t\t" + "var _stub = {" + newLine 
        + "\t\t\tid: TableManager.generateGUID()," + newLine
        + "\t\t\ttype: Components" + iname + "Model.MODEL," + newLine
        + "\t\t\ttypeDesc: \"" + name + "\"," + newLine
        + "\t\t\tmeta: \"CPK\"," + newLine
        + "\t\t\tmeta_pluginId: \"" + this.pluginId +"\"," + newLine
        + "\t\t\tmeta_endpoint: \"" + this.endpoint +"\"," + newLine
        + "\t\t\tparent: IndexManager.ROOTID," + newLine
        + "\t\t\tproperties: []" + newLine
        + "\t\t};" + newLine
        + "\t\t_stub.properties.push(PropertiesManager.getProperty(\"name\"));" + newLine
        + "\t\t" + "return _stub;" + newLine
        + "\t}" + newLine
        + "});" + newLine + newLine
        + "BaseModel.registerModel(Components" + iname + "Model);" + newLine
        + "CDFDDDatasourcesArray.push(new " + iname + "Entry());" + newLine + newLine;
    
    return model;
  }

  @Override
  public String getEntry() {
    final JXPathContext context = JXPathContext.newContext(definition);
    final String group = ((String) context.getValue("metadata/group", String.class));
    final String groupDesc = ((String) context.getValue("metadata/groupdesc", String.class));
    final String desc = name;

    //@formatter:off
    String entry = new StringBuilder("\nvar ")
        .append(iname)
        .append("Entry = PalleteEntry.extend({\n")
        // id
        .append("\tid: \"")
        .append(iname.toUpperCase())
        .append("\",\n")
        // name
        .append("\tname: \"")
        .append(name)
        .append("\",\n")
        // description
        .append("\tdescription: \"")
        .append(desc)
        .append("\",\n")
        // category
        .append("\tcategory: \"")
        .append(group)
        .append("\",\n")
        // category description
        .append("\tcategoryDesc: \"")
        .append(groupDesc)
        .append("\",\n")
        // stub
        .append("\tgetStub: function(){\n")
        .append("\t\treturn Components")
        .append(iname)
        .append("Model.getStub();")
        .append("\n\t}\n});\n").toString();
    //@formatter:on

    return entry;
  }

}
