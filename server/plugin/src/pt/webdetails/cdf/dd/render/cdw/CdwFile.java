/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.cdw;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import pt.webdetails.cdf.dd.structure.WcdfDescriptor;
import pt.webdetails.cpf.repository.RepositoryAccess;

/**
 *
 * @author pdpi
 */
public class CdwFile
{

  private static final Log logger = LogFactory.getLog(CdwFile.class);
  private static final String ENCODING = "UTF-8";
  
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
    else {
      //empty the metadata
      while(metadata.hasChildNodes()){
        metadata.removeChild( metadata.getFirstChild() );
      }
    }
    metadata.appendChild(cdwDocument.createElement("title")).setTextContent(descriptor.getTitle());
    metadata.appendChild(cdwDocument.createElement("author")).setTextContent(descriptor.getAuthor());
    metadata.appendChild(cdwDocument.createElement("description")).setTextContent(descriptor.getDescription());
  }

  public void addChart(CggChart chartDefinition)
  {
    if (this.charts == null)
    {
      this.charts = (Element) root.appendChild(cdwDocument.createElement("charts"));
    }

    Element chart = (Element) this.charts.appendChild(cdwDocument.createElement("chart"));

    setHeaders(chart, chartDefinition);
    setSource(chart, chartDefinition);
    setParams(chart, chartDefinition.getParameters());
  }

  public boolean writeFile(String path, String name)
  {
    try
    {
      DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
      DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
      LSOutput output = impl.createLSOutput();

      Writer outputWriter = new StringWriter();
      output.setCharacterStream(outputWriter);
      output.setEncoding(ENCODING);
      LSSerializer writer = impl.createLSSerializer();
      writer.write(root, output);
      outputWriter.flush();
      
      byte[] fileContents = outputWriter.toString().getBytes(ENCODING);
      name = name.replaceAll("cdfde$", "cdw");
      
      switch(RepositoryAccess.getRepository().publishFile(path, name, fileContents, true)){
        case FAIL:
          logger.error("Could not save " + name);
          return false;
        case OK:
          return true;
      }
    }
    catch (Exception e)
    {
      logger.error("Failed to export cdw file: " + e.getCause().getMessage());
    }
    return false;
  }

  private void setParams(Element chart, Map<String, String> parameters)
  {
    Element paramsElement = (Element) chart.appendChild(cdwDocument.createElement("parameters"));
    for (Entry<String, String> elem : parameters.entrySet())
    {
      Element param = (Element) paramsElement.appendChild(cdwDocument.createElement("parameter"));
      param.setAttribute("type", elem.getValue());
      param.setTextContent(elem.getKey());
    }
  }

  private void setHeaders(Element chart, CggChart chartDefinition)
  {
    chart.setAttribute("id", chartDefinition.getId());
    chart.setAttribute("name", chartDefinition.getName());
  }

  private void setSource(Element chart, CggChart chartDefinition)
  {
    chart.appendChild(cdwDocument.createElement("script")).setTextContent(chartDefinition.getFilename());
  }
}
