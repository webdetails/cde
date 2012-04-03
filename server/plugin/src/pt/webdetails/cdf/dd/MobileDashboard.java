/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd;

import java.util.Date;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.render.RenderComponents;
import pt.webdetails.cdf.dd.util.JsonUtils;

// Imports for the cache
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import pt.webdetails.cdf.dd.render.RenderMobileLayout;

/**
 *
 * @author pdpi
 */
public class MobileDashboard extends AbstractDashboard
{
  /* CONSTANTS */

  private static final long serialVersionUID = 1L;
  // Dashboard rendering
  private static Log logger = LogFactory.getLog(Dashboard.class);
  /* FIELDS */
  protected final static String TYPE = "mobile";
  protected final static String MOBILE_TEMPLATE = "resources/mobile/index.html";

  public MobileDashboard(IParameterProvider pathParams, IParameterProvider requestParams)
  {
    super(pathParams, requestParams);
    IPentahoSession userSession = PentahoSessionHolder.getSession();
    final ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);


//    final String absRoot = requestParams.hasParameter("root") ? requestParams.getParameter("root").toString() : "";
//    final boolean absolute = (!absRoot.equals("")) || requestParams.hasParameter("absolute") && requestParams.getParameter("absolute").equals("true");
    
    
//    final String absRoot = pathParams.hasParameter("root") ? !pathParams.getParameter("root").toString().isEmpty() ? "http://" + pathParams.getParameter("root").toString() : "" : "";
//    final boolean absolute = (!absRoot.isEmpty()) || pathParams.hasParameter("absolute") && pathParams.getParameter("absolute").equals("true");

    final RenderMobileLayout layoutRenderer = new RenderMobileLayout();
    final RenderComponents componentsRenderer = new RenderComponents();

    try
    {
      final JSONObject json = (JSONObject) JsonUtils.readJsonFromInputStream(solutionRepository.getResourceInputStream(dashboardLocation, true, ISolutionRepository.ACTION_EXECUTE));

      json.put("settings", getWcdf().toJSON());
      final JXPathContext doc = JXPathContext.newContext(json);

      final StringBuilder dashboardBody = new StringBuilder();

      dashboardBody.append(layoutRenderer.render(doc));
      dashboardBody.append(componentsRenderer.render(doc));

      // set all dashboard members
      this.content = replaceTokens(dashboardBody.toString(), absolute, absRoot);

      this.header = renderHeaders(this.content.toString());
      
      
      this.templateFile = CdfStyles.getInstance().getResourceLocation(getWcdf().getStyle());
      
      
      //If it is clean.html, change to default mobile template
      if (templateFile.equals("resources/styles/Clean.html")) 
        this.templateFile = MOBILE_TEMPLATE;
      
      
      
      this.template = replaceTokens(ResourceManager.getInstance().getResourceAsString(this.templateFile), absolute, absRoot);
      this.loaded = new Date();

    }
    catch (Exception e)
    {
      logger.error(e);
    }
  }

  public String getType()
  {
    return TYPE;
  }
}
