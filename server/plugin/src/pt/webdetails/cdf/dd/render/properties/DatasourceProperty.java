/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.properties;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Node;
import pt.webdetails.cdf.dd.util.XPathUtils;

/**
 *
 * @author pdpi
 */
public class DatasourceProperty extends GenericProperty
{

  public DatasourceProperty(String path, Node doc)
  {
    // We don't actually want this constructor to do stuff
    super();
  }

  public DatasourceProperty()
  {
    super();
  }

  @Override
  public String getDefinition()
  {
    return "";
  }

  @Override
  public String render(String name, JXPathContext node)
  {
    boolean isCda = false;
    boolean isBuiltIn = false;
    String queryName = XPathUtils.getStringValue(node, "properties/value[../name='" + getName() + "']");
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

  @Override
  public String getName()
  {
    return "dataSource";
  }

  private String renderCdaDatasource(String name, JXPathContext node)
  {
    StringBuilder output = new StringBuilder();
    String queryName = XPathUtils.getStringValue(node, "properties/value[../name='" + getName() + "']");
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
          output.append("path: \"").append(XPathUtils.getStringValue(query, "properties/value[../name='cdaPath']")).append("\",").append(newLine);
        }
        else
        {
          // legacy
          output.append("solution: \"").append(XPathUtils.getStringValue(query, "properties/value[../name='solution']")).append("\",").append(newLine);
          output.append("path: \"").append(XPathUtils.getStringValue(query, "properties/value[../name='path']")).append("\",").append(newLine);
          output.append("file: \"").append(XPathUtils.getStringValue(query, "properties/value[../name='file']")).append("\",").append(newLine);

        }
      }
    }
    return replaceParameters(output.toString());

  }

  public String renderDatasource(String name, JXPathContext node)
  {
    StringBuilder output = new StringBuilder();




    if (name.length() > 0)
    {
      String queryName = XPathUtils.getStringValue(node, "properties/value[../name='" + getName() + "']");




      if (queryName.length() == 0)
      {
        return "";




      }
      Pointer pointer = node.getPointer("/datasources/rows[properties/name='name'][properties/value='" + queryName + "']");




      if (!(pointer instanceof NullPointer))
      {

        JXPathContext query = node.getRelativeContext(pointer);
        output.append("jndi: \"" + XPathUtils.getStringValue(query, "properties/value[../name='jndi']") + "\"," + newLine);
        output.append("\t\tcatalog: \"" + XPathUtils.getStringValue(query, "properties/value[../name='catalog']") + "\"," + newLine);
        output.append("\t\tcube: \"" + XPathUtils.getStringValue(query, "properties/value[../name='cube']") + "\"," + newLine);


        String mdxQuery = XPathUtils.getStringValue(query, "properties/value[../name='mdxquery']");
        String processedQuery;
        String queryType;




        if (!mdxQuery.equals(""))
        {
          processedQuery = getFunctionParameter(mdxQuery, true);
          queryType = "\"mdx\"";




        }
        else
        {
          processedQuery = getFunctionParameter(XPathUtils.getStringValue(query, "properties/value[../name='sqlquery']"), true);
          queryType = "\"sql\"";




        }
        output.append("\t\tquery: " + processedQuery + "," + newLine);
        output.append("\t\tqueryType:" + queryType + "," + newLine);




      }

    }
    return replaceParameters(output.toString());




  }

  private String renderBuiltinCdaDatasource(String name, JXPathContext node)
  {
    StringBuilder output = new StringBuilder();
    String queryName = XPathUtils.getStringValue(node, "properties/value[../name='" + getName() + "']");




    if (queryName.length() > 0)
    {
      Pointer pointer = node.getPointer("/datasources/rows[properties[name='name' and value='" + queryName + "']]");




      if (!(pointer instanceof NullPointer))
      {

        JXPathContext query = node.getRelativeContext(pointer);
        output.append("dataAccessId: \"" + XPathUtils.getStringValue(query, "properties/value[../name='name']") + "\"," + newLine);
        output.append("path: \"" + XPathUtils.getStringValue(query, "/filename").replaceAll(".cdfde", ".cda") + "\"," + newLine);




      }
    }
    return replaceParameters(output.toString());



  }
}
