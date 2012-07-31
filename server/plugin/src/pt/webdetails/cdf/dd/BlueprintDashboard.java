/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd;

import java.io.FileNotFoundException;

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

  private static final long serialVersionUID = 1L;
  private static Log logger = LogFactory.getLog(Dashboard.class);
  protected final static String TYPE = "blueprint";

  public BlueprintDashboard(IParameterProvider pathParams, IParameterProvider requestParams) throws FileNotFoundException
  {
    super(pathParams, requestParams);
  }

  protected String renderHeaders(String contents)
  {
    String dependencies, styles, cdfDependencies;
    final String title = "<title>" + getWcdf().getTitle() + "</title>";
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
      final String adornedRoot = (scheme.equals("") ? "" : (scheme + "://")) + absRoot;
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
