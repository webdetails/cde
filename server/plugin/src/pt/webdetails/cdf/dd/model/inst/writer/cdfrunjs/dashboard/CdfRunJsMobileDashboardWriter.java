
package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import java.io.IOException;
import org.apache.commons.jxpath.JXPathContext;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.render.RenderMobileLayout;
import pt.webdetails.cdf.dd.render.Renderer;
import pt.webdetails.cdf.dd.structure.WcdfDescriptor;

/**
 * @author dcleao
 */
public final class CdfRunJsMobileDashboardWriter extends CdfRunJsDashboardWriter
{
  protected static final String TYPE = "mobile";
  protected static final String MOBILE_TEMPLATE = "resources/mobile/index.html";
  
  public String getType()
  {
    return TYPE;
  }
  
  @Override
  protected String readTemplate(WcdfDescriptor wcdf) throws IOException
  {
    return readTemplateFile(MOBILE_TEMPLATE);
  }

  @Override
  protected Renderer getLayoutRenderer(JXPathContext docXP, CdfRunJsDashboardWriteContext context)
  {
    return new RenderMobileLayout(docXP, context, PentahoSessionHolder.getSession()); // TODO:
  }
}