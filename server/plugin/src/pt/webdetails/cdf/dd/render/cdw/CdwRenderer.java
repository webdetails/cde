/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.cdw;

import java.util.Iterator;
import net.sf.json.JSONSerializer;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import pt.webdetails.cdf.dd.structure.WcdfDescriptor;

/**
 *
 * @author pdpi
 */
public class CdwRenderer
{

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

  public void render()
  {
    render(this.document);
  }

  protected void render(JXPathContext document)
  {
    CdwFile cdw = new CdwFile();
    cdw.populateHeaders(descriptor);

    Iterator<Pointer> charts = document.iteratePointers("/components/rows[meta_cdwSupport='true']");
    while (charts.hasNext())
    {
      Pointer chartSource = charts.next();
      CggChart chart = new CggChart(chartSource);
      chart.render();
      cdw.addChart(chart);
    }

    cdw.writeFile();

  }
}
