/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.ResourceManager;
import pt.webdetails.cdf.dd.model.core.*;
import pt.webdetails.cdf.dd.model.core.writer.*;
import pt.webdetails.cdf.dd.model.core.writer.js.*;
import pt.webdetails.cdf.dd.model.inst.*;
import pt.webdetails.cdf.dd.render.*;
import pt.webdetails.cdf.dd.render.DependenciesManager.Engines;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cpf.IPluginCall;
import pt.webdetails.cpf.plugin.CorePlugin;

/**
 * @author dcleao
 */
public abstract class CdfRunJsDashboardWriter extends JsWriterAbstract implements IThingWriter
{
  private static final Log _logger = LogFactory.getLog(CdfRunJsDashboardWriter.class);

  private static final String RESOURCE_FOOTER = "resources/patch-footer.html";
  
  public  static final String DASHBOARD_HEADER_TAG  = "\\@HEADER\\@";
  private static final String DASHBOARD_CONTENT_TAG = "\\@CONTENT\\@";
  private static final String DASHBOARD_FOOTER_TAG  = "\\@FOOTER\\@";
  
  private static final String EPILOGUE = wrapJsScriptTags("Dashboards.init();");
  
  // ------------
  
  public void write(Object output, IThingWriteContext context, Thing t) throws ThingWriteException
  {
    this.write((CdfRunJsDashboardWriteResult.Builder)output, 
               (CdfRunJsDashboardWriteContext)context, 
               (Dashboard)t);
  }
  
  public abstract String getType();
  
  public void write(
          CdfRunJsDashboardWriteResult.Builder builder,
          CdfRunJsDashboardWriteContext ctx,
          Dashboard dash)
          throws ThingWriteException
  {
    assert dash == ctx.getDashboard();
    
    DashboardWcdfDescriptor wcdf = dash.getWcdf();
    
    // ------------
    
    String template;
    try
    {
      template = this.readTemplate(wcdf);
    }
    catch(IOException ex)
    {
      throw new ThingWriteException("Could not read style template file.", ex);
    }
    
    template = ctx.replaceTokens(template);
    
    // ------------
    
    String footer;
    try
    {
      footer = ResourceManager.getInstance().getResourceAsString(RESOURCE_FOOTER);
    }
    catch(IOException ex)
    {
      throw new ThingWriteException("Could not read footer file.", ex);
    }
    
    // ------------
    
    String layout     = ctx.replaceTokensAndAlias(this.writeLayout    (ctx, dash));
    String components = ctx.replaceTokensAndAlias(this.writeComponents(ctx, dash));
    String content    = writeContent(layout, components);
    String header     = ctx.replaceTokens(writeHeaders(content, ctx));
    
    // Leave the DASHBOARD_HEADER_TAG to replace additional stuff on render.
    template = template
      .replaceAll(DASHBOARD_HEADER_TAG,  Matcher.quoteReplacement(header) + DASHBOARD_HEADER_TAG)
      .replaceAll(DASHBOARD_FOOTER_TAG,  Matcher.quoteReplacement(footer))
      .replaceAll(DASHBOARD_CONTENT_TAG, Matcher.quoteReplacement(content));
    
    // Export
    builder
      .setTemplate(template)
      .setHeader(header)
      .setLayout(layout)
      .setComponents(components)
      .setContent(content)
      .setFooter(footer)
      .setLoadedDate(ctx.getDashboard().getSourceDate());
  }
  
  protected String readTemplate(DashboardWcdfDescriptor wcdf) throws IOException
  {
    return readStyleTemplateOrDefault(wcdf.getStyle());
  }
  
  // -----------------
  
  protected String writeLayout(CdfRunJsDashboardWriteContext context, Dashboard dash)
  {
    // TODO: HACK: uses pass-through XPath node...
    if(dash.getLayoutCount() == 1)
    {
      JXPathContext docXP = dash.getLayout("TODO").getLayoutXPContext();
      try
      {
        return getLayoutRenderer(docXP, context)
                .render(context.getOptions().getAliasPrefix());
      }
      catch(Exception ex)
      {
        _logger.error("Error rendering layout", ex);
      }
    }
    
    return "";
  }
  
  protected Renderer getLayoutRenderer(JXPathContext docXP, CdfRunJsDashboardWriteContext context)
  {
    return new RenderLayout(docXP, context);
  }
  
  // -----------------
  
  protected String writeComponents(CdfRunJsDashboardWriteContext context, Dashboard dash) throws ThingWriteException
  {
    DashboardWcdfDescriptor wcdf = dash.getWcdf();
    
    StringBuilder out = new StringBuilder();
    StringBuilder widgetsOut = new StringBuilder();
    
    // Output WCDF
    out.append("wcdfSettings = ");
    out.append(wcdf.toJSON().toString(2));
    out.append(';');
    out.append(NEWLINE);
    out.append(NEWLINE);
    
    boolean isFirstComp = true;
    
    StringBuilder addCompIds = new StringBuilder();
    boolean isFirstAddComp = true;
    
    IThingWriterFactory factory = context.getFactory();
    
    Iterable<Component> comps = dash.getRegulars();
    for(Component comp : comps)
    {
      if(StringUtils.isNotEmpty(comp.getName()))
      {
        IThingWriter writer;
        try
        {
          writer = factory.getWriter(comp);
        }
        catch(UnsupportedThingException ex)
        {
          throw new ThingWriteException(ex);
        }

        boolean isWidget = comp instanceof WidgetComponent;

        StringBuilder out2 = isWidget ? widgetsOut : out;
        if(!isFirstComp) { out2.append(NEWLINE); }

        // NOTE: Widgets don't really exist at runtime, 
        // only their (leaf-)content does.
        if(comp instanceof VisualComponent && !(comp instanceof WidgetComponent))
        {
          if(isFirstAddComp) { isFirstAddComp = false; }
          else { addCompIds.append(", "); }

          addCompIds.append(context.getId(comp));
        }

        writer.write(out2, context, comp);
      
        isFirstComp = false;
      }
    }
    
    if(!isFirstAddComp)
    {
      out.append(NEWLINE);
      out.append("Dashboards.addComponents(["); out.append(addCompIds); out.append("]);");
      out.append(NEWLINE);
    }
    
    out.append(widgetsOut);
    
    return out.toString();
  }
  
  // -----------------
  
  protected String writeHeaders(
          String contents, 
          CdfRunJsDashboardWriteContext context)
  {
    CdfRunJsDashboardWriteOptions options = context.getOptions();
    
    DashboardWcdfDescriptor wcdf = context.getDashboard().getWcdf();
    
    final String title = "<title>" + wcdf.getTitle() + "</title>";
    
    // Get CDF headers
    String cdfDeps;
    try
    {
      cdfDeps = getCdfIncludes(
                contents, 
                this.getType(), 
                options.isDebug(), 
                options.getAbsRoot(), 
                options.getScheme());
    }
    catch(Exception ex)
    {
      _logger.error("Failed to get cdf includes");
      cdfDeps = "";
    }
    
    // Get CDE headers
    final String baseUrl = (options.isAbsolute() ? options.getSchemedRoot() : "") +
    		CdeEngine.getInstance().getEnvironment().getApplicationBaseUrl();
    
    StringFilter cssFilter = new StringFilter()
    {
      public String filter(String input)
      {
        return String.format(
          "\t\t<link href=\"%sgetCssResource/%s\" rel=\"stylesheet\" type=\"text/css\" />",
          baseUrl, input);
      }
    };

    StringFilter jsFilter = new StringFilter()
    {
      public String filter(String input)
      {
        return String.format(
          "\t\t<script language=\"javascript\" type=\"text/javascript\" src=\"%sgetJsResource/%s\"></script>",
          baseUrl, input);
      }
    };
    
    DependenciesManager depMgr = DependenciesManager.getInstance();
    boolean isPackaged = !options.isDebug();
    String scriptDeps = depMgr.getEngine(Engines.CDF    ).getDependencies(jsFilter,  isPackaged);
    String styleDeps  = depMgr.getEngine(Engines.CDF_CSS).getDependencies(cssFilter, isPackaged);
    String rawDeps    = depMgr.getEngine(Engines.CDF_RAW).getDependencies();
    
    return title + cdfDeps + rawDeps + scriptDeps + styleDeps;
  }
  
  // -----------------
  
  protected static String readStyleTemplateOrDefault(String styleName) throws IOException
  {
    if(StringUtils.isNotEmpty(styleName)) 
    {
      try { return readStyleTemplate(styleName); } catch(IOException ex) { }
    }
    
    // Couldn't open template file, attempt to use default
    return readStyleTemplate(CdeConstants.DEFAULT_STYLE);
  }
  
  protected static String readStyleTemplate(String styleName) throws IOException
  {
    return readTemplateFile(CdeEngine.getInstance().getEnvironment().getPluginResourceLocationManager().getStyleResourceLocation(styleName));
  }
  
  protected static String readTemplateFile(String templateFile) throws IOException
  {
    try
    {
      return ResourceManager.getInstance().getResourceAsString(templateFile);
    }
    catch(IOException ex)
    {
      _logger.error(MessageFormat.format("Couldn't open template file '{0}'.", templateFile), ex);
      throw ex;
    }
  }

  private String writeContent(String layout, String components)
  {
    StringBuilder out = new StringBuilder();
    
    out.append(layout);

    wrapJsScriptTags(out, components);
    
    out.append(EPILOGUE);
    
    return out.toString();
  }
  
  public static String getCdfIncludes(String dashboard, String type, boolean debug, String absRoot, String scheme) throws IOException {
	    Map<String, Object> params = new HashMap<String, Object>();
	    params.put("dashboardContent", dashboard);
	    params.put("debug", debug);
	    params.put("scheme", scheme);
	    if (type != null) {
	      params.put("dashboardType", type);
	    }
	    if (!StringUtils.isEmpty(absRoot)) {
	      params.put("root", absRoot);
	    }
	    
	    IPluginCall pluginCall = CdeEngine.getInstance().getEnvironment().getInterPluginCall();
	    pluginCall.init(CorePlugin.CDF, "GetHeaders", params);
	    
	    return  pluginCall.call();
	 }
}