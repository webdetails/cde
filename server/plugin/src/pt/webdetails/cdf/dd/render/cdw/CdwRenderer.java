/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.cdw;

import java.util.Iterator;
import net.sf.json.JSONSerializer;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.structure.WcdfDescriptor;

/**
 *
 * @author pdpi
 */
public class CdwRenderer
{

  private static final Log logger = LogFactory.getLog(CdwRenderer.class);
  JXPathContext document;
  WcdfDescriptor descriptor;

  public CdwRenderer(JXPathContext document, WcdfDescriptor descriptor)
  {
    this.document = document;
    this.descriptor = descriptor;
  }

  public CdwRenderer(String document, WcdfDescriptor descriptor)
  {
    this(JXPathContext.newContext(JSONSerializer.toJSON(document.replaceAll("^\n*", ""))), descriptor);
  }

  public void render(String path, String name)
  {
    render(this.document, path, name);
  }

  protected void render(JXPathContext document, String path, String name)
  {
    CdwFile cdw = new CdwFile();
    cdw.populateHeaders(descriptor);

    Iterator<Pointer> charts = document.iteratePointers("/components/rows[meta_cdwSupport='true']");
    while (charts.hasNext())
    {
      Pointer chartSource = charts.next();
      CggChart chart = new CggChart(chartSource);
      chart.setPath(path);
      chart.renderToFile();
      cdw.addChart(chart);
    }

    cdw.writeFile(path, name);

  }
}
