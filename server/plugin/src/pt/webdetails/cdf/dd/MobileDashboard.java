/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
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
import pt.webdetails.cdf.dd.structure.WcdfDescriptor;
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
    final String absRoot = pathParams.hasParameter("root") ? !pathParams.getParameter("root").toString().isEmpty() ? "http://" + pathParams.getParameter("root").toString() : "" : "";
    final boolean absolute = (!absRoot.isEmpty()) || pathParams.hasParameter("absolute") && pathParams.getParameter("absolute").equals("true");
    construct(absolute, absRoot);
  }

  private void construct(boolean absolute, String absRoot){
  
    RepositoryAccess solutionRepository = RepositoryAccess.getRepository();



    final RenderMobileLayout layoutRenderer = new RenderMobileLayout();
    final RenderComponents componentsRenderer = new RenderComponents();

    try
    {
      final JSONObject json = (JSONObject) JsonUtils.readJsonFromInputStream(solutionRepository.getResourceInputStream(dashboardLocation));

      json.put("settings", getWcdf().toJSON());
      final JXPathContext doc = JXPathContext.newContext(json);

      // set all dashboard members
      this.layout = replaceTokens(layoutRenderer.render(doc), absolute, absRoot);
      this.components = replaceTokens(componentsRenderer.render(doc), absolute, absRoot);
      this.header = renderHeaders(this.layout + this.components);
      this.templateFile = MOBILE_TEMPLATE;
      this.template = replaceTokens(ResourceManager.getInstance().getResourceAsString(this.templateFile), absolute, absRoot);
      this.loaded = new Date();

    }
    catch (Exception e)
    {
      logger.error(e);
    }
  }

    public MobileDashboard(WcdfDescriptor wcdf, boolean absolute, String absRoot, boolean debug, String scheme) throws FileNotFoundException
  {
    super(wcdf, absolute, absRoot, debug, scheme);
    construct(absolute, absRoot);
  }
  public MobileDashboard(String wcdfPath, boolean absolute, String absRoot, boolean debug, String scheme) throws FileNotFoundException
  {
    super(wcdfPath, absolute, absRoot, debug, scheme);
    construct(absolute, absRoot);
  }

  public String getType()
  {
    return TYPE;
  }
}
