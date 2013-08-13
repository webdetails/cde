/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.writer.cggrunjs;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.CdeSettings;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.GenericComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.CdfRunJsThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cpf.repository.BaseRepositoryAccess.SaveFileStatus;
import pt.webdetails.cpf.repository.IRepositoryAccess;

/**
 * @author dcleao
 */
public class CggRunJsGenericComponentWriter extends JsWriterAbstract implements IThingWriter
{
  private static final Log    _logger = LogFactory.getLog(CggRunJsGenericComponentWriter.class);
  private static final String CGG_EXTENSION = ".js";
  
  public void write(Object output, IThingWriteContext context, Thing t) throws ThingWriteException
  {
    this.write((IRepositoryAccess)output, (CggRunJsDashboardWriteContext)context, (GenericComponent)t);
  }
  
  public void write(IRepositoryAccess repository, CggRunJsDashboardWriteContext context, GenericComponent comp) throws ThingWriteException
  {
    StringBuilder out = new StringBuilder();
    
    out.append("lib('protovis-bundle.js');")
       .append(NEWLINE)
       .append(NEWLINE);
    
    this.renderChart(out, context, comp);
    this.renderDatasource(out, context, comp);
    
    String chartName = comp.getName();
    String compVarName = "render_" + chartName;
    
    out.append(NEWLINE)
       .append("renderCccFromComponent(")
       .append(compVarName)
       .append(", data);")
       .append(NEWLINE);
    
    String dashboardFilePath = context.getDashboard().getSourcePath();
    String dashboardFileDir  = FilenameUtils.getFullPath(dashboardFilePath);
    String dashboardFileName = FilenameUtils.getName(dashboardFilePath);
    
    String chartScript = out.toString();
    
    writeFile(repository, chartScript, dashboardFileDir, chartName, dashboardFileName);
    writeFile(repository, chartScript, dashboardFileDir, chartName,  null);
  }
  
   private void writeFile(
           IRepositoryAccess repository, 
           String chartScript, 
           String dashboardFileDir,
           String chartName, 
           String dashboadFilName)
  {
    try
    {
      String prefix   = dashboadFilName == null ? "" : dashboadFilName.substring(0, dashboadFilName.lastIndexOf('.')) + '_'; 
      String fileName = prefix + chartName + CGG_EXTENSION;
      
      byte[] content = chartScript.getBytes(CdeSettings.getEncoding());
      
      if(repository.publishFile(dashboardFileDir, fileName, content, true) != SaveFileStatus.OK)
      {
        _logger.error("Failed to write CGG script file for chart '" + chartName + "'.");
      }
    }
    catch(Exception ex)
    {
      _logger.error("Failed to write script file for '" + chartName + "': " + ex.getCause().getMessage());
    }
  }
   
  private void renderChart(StringBuilder out, CggRunJsDashboardWriteContext context, GenericComponent comp) throws ThingWriteException
  {
    // Delegate writing the component to the corresponding CdfRunJs writer
    IThingWriterFactory writerFactory = new CdfRunJsThingWriterFactory();
    IThingWriter compWriter;
    try
    {
      compWriter = writerFactory.getWriter(comp);
    }
    catch(UnsupportedThingException ex)
    {
      throw new ThingWriteException("Error while obtaining a writer for rendering the generic component.", ex);
    }
    
    // Options are kind of irrelevant in this context, 
    // as we're only rendering one component, not a dashboard (and not a widget component).
    CdfRunJsDashboardWriteOptions options = new CdfRunJsDashboardWriteOptions(false, false, "", "");
    
    // Idem
    CdfRunJsDashboardWriteContext writeContext = 
      new CdfRunJsDashboardWriteContext(
            writerFactory, 
            /*indent*/"", 
            /*bypassCacheRead*/true, 
            context.getDashboard(), 
            context.getUserSession(),
            options);
    
    
    compWriter.write(out, writeContext, comp);
  }
  
  private void renderDatasource(StringBuilder out, CggRunJsDashboardWriteContext context, GenericComponent comp) throws ThingWriteException
  {
    Dashboard dash = context.getDashboard();
    String dataSourceName = comp.tryGetPropertyValue("dataSource", null);
    if(StringUtils.isNotEmpty(dataSourceName))
    {
      DataSourceComponent dsComp = dash.getDataSource(dataSourceName);
      
      IThingWriterFactory factory = context.getFactory();
      IThingWriter dsWriter;
      try
      {
        dsWriter = factory.getWriter(dsComp);
      }
      catch (UnsupportedThingException ex)
      {
        throw new ThingWriteException(ex);
      }
      
      CggRunJsComponentWriteContext compContext = 
              new CggRunJsComponentWriteContext(factory, dash, comp, context.getUserSession());
      dsWriter.write(out, compContext, dsComp);
    } 
    else
    {
      // For when no datasource is specified, the variable data must be defined anyway.
      out.append("var data;");
      out.append(NEWLINE);
    }
  }
}
