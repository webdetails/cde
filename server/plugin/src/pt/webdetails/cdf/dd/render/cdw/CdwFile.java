/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.cdw;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import pt.webdetails.cdf.dd.structure.WcdfDescriptor;

/**
 *
 * @author pdpi
 */
public class CdwFile
{

  Document cdwDocument;
  Element root, charts, metadata;

  public CdwFile() throws RuntimeException
  {


    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      cdwDocument = builder.newDocument();
      root = cdwDocument.createElement("cdw");
      cdwDocument.appendChild(root);
    }
    catch (Exception e)
    {
      throw new RuntimeException();
    }

  }

  public void populateHeaders(WcdfDescriptor descriptor)
  {
    if (metadata == null)
    {
      metadata = (Element) root.appendChild(cdwDocument.createElement("metadata"));
    }
    // TODO: This needs to be resilient to multiple calls! Empty the metadata somehow
    metadata.appendChild(cdwDocument.createElement("title")).setNodeValue(descriptor.getTitle());
    metadata.appendChild(cdwDocument.createElement("author")).setNodeValue(descriptor.getAuthor());
    metadata.appendChild(cdwDocument.createElement("description")).setNodeValue(descriptor.getDescription());
  }

  public void addChart(CggChart chartDefinition)
  {
    if (this.charts == null)
    {
      this.charts = (Element) root.appendChild(cdwDocument.createElement("charts"));
    }

    Element chart = cdwDocument.createElement("chart");

    setParams();
    setHeaders();
    setSource();
  }

  public void writeFile()
  {
    
  }

  private void setParams()
  {
  }

  private void setHeaders()
  {
  }

  private void setSource()
  {
  }
}
