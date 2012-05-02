/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cdf.dd.render.DependenciesManager;
import pt.webdetails.cdf.dd.render.StringFilter;

/**
 *
 * @author pdpi
 */
public class BlueprintDashboard extends AbstractDashboard
{
  /* CONSTANTS */

//  // Dashboard rendering
//  private static final String DASHBOARD_HEADER_TAG = "\\@HEADER\\@";
//  private static final String DASHBOARD_CONTENT_TAG = "\\@CONTENT\\@";
//  private static final String DASHBOARD_FOOTER_TAG = "\\@FOOTER\\@";
//  private static final String RESOURCE_FOOTER = "resources/patch-footer.html";
//  private static final String I18N_BOILERPLATE = "resources/i18n-boilerplate.js";
  private static Log logger = LogFactory.getLog(Dashboard.class);
  // Cache
//  private static final String CACHE_CFG_FILE = "ehcache.xml";
//  private static final String CACHE_NAME = "pentaho-cde";
  /* FIELDS */
  protected final static String TYPE = "blueprint";

  public BlueprintDashboard(IParameterProvider pathParams, IParameterProvider requestParams)
  {
    super(pathParams,requestParams);
  }

  protected String renderHeaders(String contents)
  {
    String dependencies, styles, cdfDependencies;  
    final String title = "<title>"+getWcdf().getTitle()+"</title>";
    // Acquire CDF headers
    try
    {
      cdfDependencies = DashboardDesignerContentGenerator.getCdfIncludes(contents, getType(), debug, absRoot, scheme);
    }
    catch (Exception e)
    {
      logger.error("Failed to get cdf includes");
      cdfDependencies = "";
    }
    // Acquire CDE-specific headers
    if (absolute)
    {
      final String adornedRoot = scheme + "://" + absRoot; 
      StringFilter css = new StringFilter()
      {

        public String filter(String input)
        {
          //input = input.replaceAll("\\?", "&");
          return "\t\t<link href='" + adornedRoot + DashboardDesignerContentGenerator.SERVER_URL_VALUE + "getCssResource/" + input + "' rel='stylesheet' type='text/css' />\n";
        }
      };
      StringFilter js = new StringFilter()
      {

        public String filter(String input)
        {
          //input = input.replaceAll("\\?", "&");
          return "\t\t<script language=\"javascript\" type=\"text/javascript\" src=\"" + adornedRoot + DashboardDesignerContentGenerator.SERVER_URL_VALUE + "getJsResource/" + input + "\"></script>\n";
        }
      };
      if (debug)
      {
        dependencies = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF).getDependencies(js);
        styles = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF_CSS).getDependencies(css);
      }
      else
      {
        dependencies = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF).getPackagedDependencies(js);
        styles = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF_CSS).getPackagedDependencies(css);
      }
    }
    else
    {
      if (debug)
      {
        dependencies = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF).getDependencies();
        styles = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF_CSS).getDependencies();
      }
      else
      {
        dependencies = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF).getPackagedDependencies();
        styles = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF_CSS).getPackagedDependencies();
      }
    }

    String raw = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF_RAW).getDependencies();
    return title + cdfDependencies + raw + dependencies + styles;
  }

  public String getType()
  {
    return TYPE;
  }
}
