/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd;

import java.io.FileNotFoundException;
import java.util.Date;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cdf.dd.render.RenderComponents;
import pt.webdetails.cdf.dd.util.JsonUtils;

// Imports for the cache
import pt.webdetails.cdf.dd.render.RenderMobileLayout;
import pt.webdetails.cpf.repository.RepositoryAccess;

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

  public MobileDashboard(IParameterProvider pathParams, IParameterProvider requestParams) throws FileNotFoundException
  {
    super(pathParams, requestParams);
    RepositoryAccess solutionRepository = RepositoryAccess.getRepository();

    final String absRoot = pathParams.hasParameter("root") ? !pathParams.getParameter("root").toString().isEmpty() ? "http://" + pathParams.getParameter("root").toString() : "" : "";
    final boolean absolute = (!absRoot.isEmpty()) || pathParams.hasParameter("absolute") && pathParams.getParameter("absolute").equals("true");

    final RenderMobileLayout layoutRenderer = new RenderMobileLayout();
    final RenderComponents componentsRenderer = new RenderComponents();

    try
    {
      final JSONObject json = (JSONObject) JsonUtils.readJsonFromInputStream(solutionRepository.getResourceInputStream(dashboardLocation));

      json.put("settings", getWcdf().toJSON());
      final JXPathContext doc = JXPathContext.newContext(json);

      final StringBuilder dashboardBody = new StringBuilder();

      dashboardBody.append(layoutRenderer.render(doc));
      dashboardBody.append(componentsRenderer.render(doc));

      // set all dashboard members
      this.content = replaceTokens(dashboardBody.toString(), absolute, absRoot);

      this.header = renderHeaders(this.content.toString());
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
