/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.PropertyBinding;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.meta.DataSourceComponentType;
import pt.webdetails.cdf.dd.util.JsonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dcleao
 */
public class CdfRunJsDataSourcePropertyBindingWriter extends CdfRunJsPropertyBindingWriter
{
	protected static final Log logger = LogFactory.getLog(CdfRunJsDataSourcePropertyBindingWriter.class);
	
	private static final String META_TYPE_CDA = "CDA";
	private static final String META_TYPE_CPK = "CPK";
  
  public void write(StringBuilder out, CdfRunJsDashboardWriteContext context, PropertyBinding propBind) throws ThingWriteException
  {
    DataSourceComponent dataSourceComp = this.getDataSourceComponent(context, propBind);
    if(dataSourceComp == null)
    {
      return;
    }
    
    String dataAccessId = dataSourceComp.tryGetPropertyValue("dataAccessId", null);
    if(dataAccessId != null)
    {
      renderCdaDatasource(out, context, dataSourceComp, dataAccessId);
    }
    else 
    {
      // "meta" attribute has the value "CDA", "CPK" ?
      // See DataSourceModelReader#readDataSourceComponent
      String metaType = dataSourceComp.getMeta().tryGetAttributeValue("", "");
      if(StringUtils.isEmpty(metaType))
      {
        renderDatasource(out, context, dataSourceComp);
      } 
      else if(metaType.equals(META_TYPE_CDA)) 
      {
        renderBuiltinCdaDatasource(out, context, dataSourceComp);
      }
      else if(metaType.equals(META_TYPE_CPK)) 
      {
        renderCpkDatasource(out, context, dataSourceComp);
      }
      else
      {
        throw new ThingWriteException("Cannot render a data source property of meta type '" + metaType + "'.");
      }
    }
  }
  
  protected DataSourceComponent getDataSourceComponent(CdfRunJsDashboardWriteContext context, PropertyBinding propBind)
  {
    String dataSourceName = propBind.getValue();
    return StringUtils.isEmpty(dataSourceName) ? 
           null : 
           context.getDashboard().tryGetDataSource(dataSourceName);
  }
  
  protected void renderCdaDatasource(
          StringBuilder out, 
          CdfRunJsDashboardWriteContext context,
          DataSourceComponent dataSourceComp, 
          String dataAccessId)
  {
    String indent = context.getIndent();
    
    addJsProperty(out, "dataAccessId", buildJsStringValue(dataAccessId), indent, context.isFirstInList());

    context.setIsFirstInList(false);
    
    String outputIndexId = dataSourceComp.tryGetPropertyValue("outputIndexId", null);
    if(outputIndexId != null)
    {
      addJsProperty(out, "outputIndexId", buildJsStringValue(outputIndexId), indent, false);
    }
    
    // Check if we have a cdaFile
    String cdaPath = dataSourceComp.tryGetPropertyValue("cdaPath", null);
    if(cdaPath != null)
    {
      addJsProperty(out, "path", buildJsStringValue(cdaPath), indent, false);
    }
    else 
    {
      // legacy
      addJsProperty(out, "solution", buildJsStringValue(dataSourceComp.tryGetPropertyValue("solution", "")), indent, false);
      addJsProperty(out, "path",     buildJsStringValue(dataSourceComp.tryGetPropertyValue("path",     "")), indent, false);
      addJsProperty(out, "file",     buildJsStringValue(dataSourceComp.tryGetPropertyValue("file",     "")), indent, false);
    }
  }
  
  protected void renderBuiltinCdaDatasource(
          StringBuilder out, 
          CdfRunJsDashboardWriteContext context,
          DataSourceComponent dataSourceComp)
  {
    String indent = context.getIndent();
    
    addJsProperty(out, "dataAccessId", buildJsStringValue(dataSourceComp.getName()), indent, context.isFirstInList());
    
    context.setIsFirstInList(false);
    
    String cdeFilePath = context.getDashboard().getSourcePath();
    if(cdeFilePath.contains(".wcdf")){
      logger.error("renderBuiltinCdaDatasource: [fileName] receiving a .wcdf when a .cdfde was expected!");
      cdeFilePath = cdeFilePath.replace(".wcdf", ".cda");
    }
    
    String cdaFilePath = cdeFilePath.replaceAll(".cdfde", ".cda");
    
    addJsProperty(out, "path", JsonUtils.toJsString(cdaFilePath), indent, false);
  }
  
  protected void renderCpkDatasource(
          StringBuilder out, 
          CdfRunJsDashboardWriteContext context,
          DataSourceComponent dataSourceComp)
  {
    
    String indent = context.getIndent();
    
    DataSourceComponentType compType= dataSourceComp.getMeta();
    
    addJsProperty(out, "endpoint", buildJsStringValue(compType.tryGetAttributeValue("endpoint", "")), indent, context.isFirstInList());
    context.setIsFirstInList(false);
    
    addJsProperty(out, "pluginId", buildJsStringValue(compType.tryGetAttributeValue("pluginId", "")), indent, false);

    String stepName = dataSourceComp.getPropertyBindingByName( "stepname" ).getValue();
    this.addJsProperty( out, "stepName", this.buildJsStringValue( stepName ), indent, false );

    String kettleOutput = dataSourceComp.getPropertyBindingByName( "kettleoutput" ).getValue();
    this.addJsProperty( out, "kettleOutput", this.buildJsStringValue( kettleOutput ), indent, false );


    String queryType = "cpk";
    
    addJsProperty(out, "queryType", JsonUtils.toJsString(queryType), indent, false);
  }
  
  protected void renderDatasource(
          StringBuilder out, 
          CdfRunJsDashboardWriteContext context,
          DataSourceComponent dataSourceComp)
  {
    
    String indent = context.getIndent();
    
    addJsProperty(out, "jndi",    buildJsStringValue(dataSourceComp.tryGetPropertyValue("jndi", "")), indent, context.isFirstInList());
    context.setIsFirstInList(false);
    addJsProperty(out, "catalog", buildJsStringValue(dataSourceComp.tryGetPropertyValue("catalog", "")), indent, false);
    addJsProperty(out, "cube",    buildJsStringValue(dataSourceComp.tryGetPropertyValue("cube", "")), indent, false);
    
    
    String query = dataSourceComp.tryGetPropertyValue("mdxquery", null);
    String queryType;
    if (query != null)
    {
      queryType = "mdx";
    }
    else
    {
      queryType = "sql";
      query = dataSourceComp.tryGetPropertyValue("sqlquery", null);
    }
    
    if(query != null) 
    {
      query = writeFunction(query);
    }
    
    addJsProperty(out, "query",     replaceParameters(query),        indent, false);
    addJsProperty(out, "queryType", JsonUtils.toJsString(queryType), indent, false);
  }

  // ---------------------
  
  protected static String buildJsStringValue(String value)
  {
    return JsonUtils.toJsString(replaceParameters(value == null ? "" : value));
  }
  
  protected static String replaceParameters(String value)
  {
    // TODO: Someone explain this SHIT!
    if(value != null)
    {
      Pattern pattern = Pattern.compile("\\$\\{[^}]*\\}");
      Matcher matcher = pattern.matcher(value);
      while (matcher.find())
      {
        String parameter = matcher.group();
        value = value.replace(matcher.group(), "Dashboards.ev(" + parameter.substring(2, parameter.length() - 1) + ")");
      }
    }
    return value;
  }
}
