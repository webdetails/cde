/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.cdw;

import java.util.Iterator;
import net.sf.json.JSONSerializer;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
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
  
  private static String CDW_ELEMENTS_JXPATH = "/components/rows[meta_cdwSupport='true' and meta_cdwRender='true']";

  public CdwRenderer(JXPathContext document, WcdfDescriptor descriptor)
  {
    this.document = document;
    this.descriptor = descriptor;
  }

  public CdwRenderer(String document, WcdfDescriptor descriptor)
  {
    this(JXPathContext.newContext(JSONSerializer.toJSON(document.replaceAll("^\n*", ""))), descriptor);
  }

  public void render(String path, String dashboadFileName)
  {
    render(this.document, path, dashboadFileName);
  }
  
  public boolean isEmpty(){
    if(document == null) return true;
    Pointer pointer = document.getPointer(CDW_ELEMENTS_JXPATH); 
    return pointer == null || pointer instanceof NullPointer;
  }

  protected void render(JXPathContext document, String path, String dashboadFileName)
  {
    @SuppressWarnings("unchecked")
    Iterator<Pointer> charts = document.iteratePointers(CDW_ELEMENTS_JXPATH);
    while (charts.hasNext())
    {
      Pointer chartSource = charts.next();
      CggChart chart = new CggChart(chartSource);
      chart.setPath(path);
      chart.renderToFile(dashboadFileName);
    }
  }
  
}
