package pt.webdetails.cdf.dd.render.properties;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.apache.commons.lang.StringUtils;

import pt.webdetails.cdf.dd.util.XPathUtils;

public class JFreeChartDataSourceProperty extends DatasourceProperty {
  
  @Override
  public String getName()
  {
    return "jFreeChartDataSource";
  }
  
  @Override
  public String render(String name, JXPathContext node)
  {
    boolean isCda = false;
    boolean isBuiltIn = false;
    String queryName = XPathUtils.getStringValue(node, "properties/value[../name='" + getName() + "']");
    if(StringUtils.isEmpty(queryName)){
//      String componentType = XPathUtils.getStringValue(node, "typeDesc");
//      String componentName = XPathUtils.getStringValue(node, "properties/value[../name='name']");
//      Logger.warn(this,componentType +  " named " + componentName + " still has legacy dataSource property and should be updated.");
      queryName = XPathUtils.getStringValue(node, "properties/value[../name='" + super.getName() +"']");
    }
    Pointer pointer = node.getPointer("/datasources/rows[properties[name='name' and value='" + queryName + "']]");

    if (!(pointer instanceof NullPointer))
    {
      JXPathContext query = node.getRelativeContext(pointer);
      isBuiltIn = !StringUtils.isEmpty(XPathUtils.getStringValue(query, "meta"));
      isCda = !StringUtils.isEmpty(XPathUtils.getStringValue(query, "properties/value[../name='dataAccessId']"));
    }

    if (isCda)
    {
      return renderCdaDatasource(name, node);
    }
    else if (isBuiltIn)
    {
      return renderBuiltinCdaDatasource(name, node);
    }
    else
    {
      return renderDatasource(name, node);
    }
  }

  protected String renderCdaDatasource(String name, JXPathContext node)
  {
    StringBuilder output = new StringBuilder();
    String queryName = XPathUtils.getStringValue(node, "properties/value[../name='" + getName() + "']");
    if(StringUtils.isEmpty(queryName)){
      queryName = XPathUtils.getStringValue(node, "properties/value[../name='" + super.getName() +"']");
    }
    
    if (queryName.length() > 0)
    {
      Pointer pointer = node.getPointer("/datasources/rows[properties/name='name'][properties/value='" + queryName + "']");
      if (!(pointer instanceof NullPointer))
      {

        JXPathContext query = node.getRelativeContext(pointer);
        output.append("dataAccessId: \"").append(XPathUtils.getStringValue(query, "properties/value[../name='dataAccessId']")).append("\",").append(newLine);

        // Check if we have a cdaFile
        if (XPathUtils.exists(query, "properties/value[../name='cdaPath']"))
        {
          output.append("cdaFile: \"").append(XPathUtils.getStringValue(query, "properties/value[../name='cdaPath']")).append("\",").append(newLine);
          output.append("queryType: \"").append("cda").append("\",").append(newLine);
        }
        else
        {
          // legacy //TODO:
          output.append("solution: \"").append(XPathUtils.getStringValue(query, "properties/value[../name='solution']")).append("\",").append(newLine);
          output.append("path: \"").append(XPathUtils.getStringValue(query, "properties/value[../name='path']")).append("\",").append(newLine);
          output.append("file: \"").append(XPathUtils.getStringValue(query, "properties/value[../name='file']")).append("\",").append(newLine);

        }
      }
    }
    return replaceParameters(output.toString());

  }
  
  
}
