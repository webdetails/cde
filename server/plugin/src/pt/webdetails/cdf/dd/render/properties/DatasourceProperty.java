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
import org.pentaho.platform.util.logging.Logger;
import org.slf4j.LoggerFactory;

import pt.webdetails.cdf.dd.util.XPathUtils;

/**
 *
 * @author pdpi
 */
public class DatasourceProperty extends GenericProperty
{
  public static final String META_TYPE_CPK = "CPK";
  private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

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
      String metaType = XPathUtils.getStringValue(query, "meta");
      isBuiltIn = !StringUtils.isEmpty(metaType) && !metaType.equalsIgnoreCase(META_TYPE_CPK);
      isCda = !StringUtils.isEmpty(XPathUtils.getStringValue(query, "properties/value[../name='dataAccessId']"));
    }

    if (isCda)
    {
      logger.trace("rendering CDA Data Source");
      return renderCdaDatasource(name, node);
    }
    else if (isBuiltIn)
    {
      logger.trace("rendering Built In CDA Data Source");
      return renderBuiltinCdaDatasource(name, node);
    }
    else
    {
      // TODO(rafa): implementation to support generic datasources (add support to CPK Endpoints)
      logger.trace("rendering Generic Data Source");
      return renderDatasource(name, node);
    }
  }

  @Override
  public String getName()
  {
    return "dataSource";
  }

  protected String renderCdaDatasource(String name, JXPathContext node)
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
        
        if (XPathUtils.exists(query, "properties/value[../name='outputIndexId']"))
        {       
        	output.append("outputIndexId: \"").append(XPathUtils.getStringValue(query, "properties/value[../name='outputIndexId']")).append("\",").append(newLine);
        }
        
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

      JXPathContext query = node.getRelativeContext(pointer);
      
      String metaType = XPathUtils.getStringValue(query, "meta");
      String processedQuery = "";
      String queryType = "unknown";
      
      if (metaType.equalsIgnoreCase(META_TYPE_CPK)) {
        queryType = "cpk";
        output.append("endpoint: \"").append(XPathUtils.getStringValue(query, "meta_endpoint")).append("\",").append(newLine);
        output.append("\t\tpluginId: \"").append(XPathUtils.getStringValue(query, "meta_pluginId")).append("\",").append(newLine);
      } else {

        String jndiProp = XPathUtils.getStringValue(query, "properties/value[../name='jndi']");
        if (!jndiProp.equals("")) {
          output.append("\t\tjndi: \"" + jndiProp + "\"," + newLine);
        }

        String catalogProp = XPathUtils.getStringValue(query, "properties/value[../name='catalog']");
        if (!catalogProp.equals("")) {
          output.append("\t\tcatalog: \"" + catalogProp + "\"," + newLine);
        }

        String cubeProp = XPathUtils.getStringValue(query, "properties/value[../name='cube']");
        if (!cubeProp.equals("")) {
          output.append("\t\tcube: \"" + cubeProp + "\"," + newLine);
        }

        String mdxQuery = XPathUtils.getStringValue(query, "properties/value[../name='mdxquery']");

        if (!mdxQuery.equals("")) {
          processedQuery = getFunctionParameter(mdxQuery, true);
          queryType = "mdx";
        } else {
          processedQuery = getFunctionParameter(
              XPathUtils.getStringValue(query, "properties/value[../name='sqlquery']"), true);
          queryType = "sql";
        }
      }

      if (!processedQuery.equals("")) {
        output.append("\t\tquery: " + processedQuery + "," + newLine);
      }
      output.append("\t\tqueryType: \"").append(queryType).append("\",").append(newLine);
    }
    return replaceParameters(output.toString());
  }

  protected String renderBuiltinCdaDatasource(String name, JXPathContext node)
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
        String fileName = XPathUtils.getStringValue(query, "/filename");
        if(fileName.contains(".wcdf")){
          Logger.error(this, "renderBuiltinCdaDatasource: [fileName] receiving a .wcdf when a .cdfde was expected!");
          fileName = fileName.replace(".wcdf", ".cda");
        }
        output.append("path: \"" + fileName.replaceAll(".cdfde", ".cda") + "\"," + newLine);
      }
    }
    return replaceParameters(output.toString());
  }
}
