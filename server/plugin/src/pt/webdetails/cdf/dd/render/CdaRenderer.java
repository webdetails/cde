/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import pt.webdetails.cdf.dd.render.components.ComponentManager;
import pt.webdetails.cdf.dd.render.cda.*;

/**
 *
 * @author pdpi
 */
public class CdaRenderer
{

  private static CdaRenderer _instance;
  private JXPathContext doc;
  private JSON cdaDefinitions;

  private CdaRenderer()
  {
    this.cdaDefinitions = ComponentManager.getInstance().getCdaDefinitions();
  }

  public static synchronized CdaRenderer getInstance()
  {
    if (_instance == null)
    {
      _instance = new CdaRenderer();
    }
    return _instance;
  }

  public void setContext(JXPathContext doc)
  {
    this.doc = doc;
  }

  public String render(JXPathContext doc) throws Exception
  {
    this.setContext(doc);
    return this.render();
  }

  public String render() throws Exception
  {
    if (cdaDefinitions == null)
    {
      this.cdaDefinitions = ComponentManager.getInstance().getCdaDefinitions();
    }
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document cdaFile = builder.newDocument();
    Element root = cdaFile.createElement("CDADescriptor");
    cdaFile.appendChild(root);
    Element connections = cdaFile.createElement("DataSources");
    root.appendChild(connections);
    Iterator<Pointer> pointers = doc.iteratePointers("/datasources/rows[meta='CDA']");
    while (pointers.hasNext())
    {
      Pointer pointer = pointers.next();
      JXPathContext context = JXPathContext.newContext(pointer.getNode());
      String connectionId = (String) context.getValue("properties/.[name='name']/value", String.class);
      Element conn = null;
      try
      {
        conn = exportConnection(cdaFile, context, connectionId);
        connections.appendChild(conn);
      }
      catch (Exception e)
      {
        // things without connections end up here. All is fine,
        // we just need to make sure exportDataAccess doesn't try to generate a connection link
        connectionId = null;
      }
      Element dataAccess = exportDataAccess(cdaFile, context, connectionId);

      root.appendChild(dataAccess);
    }

    TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = tFactory.newTransformer();

    DOMSource source = new DOMSource(cdaFile);
    StreamResult res = new StreamResult(new OutputStreamWriter(result, "UTF-8"));
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    transformer.transform(source, res);
    return result.toString();
  }

  private Element exportConnection(Document doc, JXPathContext context, String id) throws Exception
  {
    JXPathContext cda = JXPathContext.newContext(cdaDefinitions);
    String type = ((String) context.getValue("type", String.class)).replaceAll("Components(.*)", "$1");
    String conntype = ((String) context.getValue("meta_conntype", String.class));
    if (conntype.equals("null"))
    {
      throw new Exception("No connection here!");
    }
    JXPathContext conn = JXPathContext.newContext((JSONObject) cda.getValue(type + "/definition/connection", JSONObject.class));
    Element connection = doc.createElement("Connection");
    connection.setAttribute("id", id);
    connection.setAttribute("type", conntype);
    Iterator<Pointer> params = conn.iteratePointers("*");
    while (params.hasNext())
    {
      Pointer pointer = params.next();
      JSONObject param = (JSONObject) pointer.getNode();
      String paramName = pointer.asPath().replaceAll(".*name='(.*?)'.*", "$1");
      String placement = ((String) conn.getValue(pointer.asPath() + "/placement", String.class)).toLowerCase();

      if (placement.equals("attrib"))
      {
        if (paramName.equals("id"))
        {
          continue;
        }
        else
        {
          String value = (String) context.getValue("properties/.[name='" + paramName + "']/value", String.class);
          connection.setAttribute(paramName, value);
        }
      }
      else if (paramName.equals("variables"))
      {
        Variables vars = new Variables();
        vars.setContext(context);
        vars.setDefinition((JSONObject) context.getValue("properties/.[name='" + paramName + "']"));
        vars.renderInto(connection);
      }
      else if (paramName.matches("olap4j.*"))
      {
        Olap4jProperties properties = new Olap4jProperties(paramName);
        properties.setDefinition((JSONObject) context.getValue("properties/.[name='" + paramName + "']"));
        properties.renderInto(connection);
      }
      else if (paramName.equals("dataFile"))
      {
        DataFile dataFile = new DataFile();
        dataFile.setDefinition((JSONObject) context.getValue("properties/.[name='" + paramName + "']"));
        dataFile.renderInto(connection);
      }
      /*else if (paramName.equals("ktrFile"))
      {
      String value = (String) context.getValue("properties/.[name='" + paramName + "']/value", String.class);
      Element child = doc.createElement("KtrFile");
      child.appendChild(doc.createTextNode(value));
      connection.appendChild(child);
      }*/
      else
      {
        String value = (String) context.getValue("properties/.[name='" + paramName + "']/value", String.class);
        Element child = doc.createElement(capitalize(paramName));
        child.appendChild(doc.createTextNode(value));
        connection.appendChild(child);
      }
    }
    return connection;
  }

  private Element exportDataAccess(Document doc, JXPathContext context, String connectionId)
  {
    String tagName = "DataAccess";
    Boolean compound = false;
    JXPathContext cda = JXPathContext.newContext(cdaDefinitions);
    String type = ((String) context.getValue("type", String.class)).replaceAll("Components(.*)", "$1");
    String daType = ((String) context.getValue("meta_datype", String.class));
    if (type.equals("join") || type.equals("union"))
    {
      tagName = "CompoundDataAccess";
      compound = true;
    }
    String name = (String) context.getValue("properties/.[name='name']/value", String.class);
    JXPathContext conn = JXPathContext.newContext((JSONObject) cda.getValue(type + "/definition/dataaccess", JSONObject.class));
    Element dataAccess = doc.createElement(tagName);
    dataAccess.setAttribute("id", name);
    dataAccess.setAttribute("type", daType);
    if (connectionId != null && !connectionId.equals(""))
    {
      dataAccess.setAttribute("connection", connectionId);
    }


    Iterator<Pointer> params = conn.iteratePointers("*");
    while (params.hasNext())
    {
      Pointer pointer = params.next();
      String paramName = pointer.asPath().replaceAll(".*name='(.*?)'.*", "$1");
      String placement = ((String) conn.getValue(pointer.asPath() + "/placement", String.class)).toLowerCase();

      if (placement.equals("attrib"))
      {
        if (paramName.equals("id") || paramName.equals("connection"))
        {
          continue;
        }
        else
        {
          String value = (String) context.getValue("properties/.[name='" + paramName + "']/value", String.class);
          dataAccess.setAttribute(paramName, value);
        }
      }
      else if (paramName.equals("parameters"))
      {
        Parameters parameters = new Parameters();
        parameters.setDefinition((JSONObject) context.getValue("properties/.[name='" + paramName + "']"));
        parameters.renderInto(dataAccess);
      }
      else if (paramName.equals("output"))
      {
        Output output = new Output();
        output.setContext(context);
        output.setDefinition((JSONObject) context.getValue("properties/.[name='" + paramName + "']"));
        output.renderInto(dataAccess);
      }
      else if (paramName.equals("variables"))
      {
        Variables vars = new Variables();
        vars.setContext(context);
        vars.setDefinition((JSONObject) context.getValue("properties/.[name='" + paramName + "']"));
        vars.renderInto(dataAccess);
      }
      else if (paramName.equals("outputMode"))
      {
        // Skip over outputMode, it's handled by output.
        break;
      }
      else if (paramName.equals("columns"))
      {
        Element cols = dataAccess.getOwnerDocument().createElement("Columns");
        Columns columns = new Columns();
        columns.setDefinition((JSONObject) context.getValue("properties/.[name='cdacolumns']"));
        columns.renderInto(cols);

        CalculatedColumns calcColumns = new CalculatedColumns();
        calcColumns.setDefinition((JSONObject) context.getValue("properties/.[name='cdacalculatedcolumns']"));
        calcColumns.renderInto(cols);
        dataAccess.appendChild(cols);
      }
      else if (paramName.equals("top") || paramName.equals("bottom")
              || paramName.equals("left") || paramName.equals("right"))
      {
        Element compoundElem = dataAccess.getOwnerDocument().createElement(capitalize(paramName));
        CompoundComponent cmp = new CompoundComponent();
        cmp.setDefinition((JSONObject) context.getValue("properties/.[name='" + paramName + "']"));
        cmp.renderInto(compoundElem);
        dataAccess.appendChild(compoundElem);
        if (paramName.equals("left"))
        {
          Keys keys = new Keys();
          keys.setDefinition((JSONObject) context.getValue("properties/.[name='leftkeys']"));
          keys.renderInto(compoundElem);
        }
        else if (paramName.equals("right"))
        {
          Keys keys = new Keys();
          keys.setDefinition((JSONObject) context.getValue("properties/.[name='rightkeys']"));
          keys.renderInto(compoundElem);
        }
      }
      else
      {
        String value = (String) context.getValue("properties/.[name='" + paramName + "']/value", String.class);
        if (paramName.equals("query"))
        {
          value = value.trim();
        }
        Element child = doc.createElement(capitalize(paramName));
        child.appendChild(doc.createTextNode(value));
        dataAccess.appendChild(child);
      }
    }
    return dataAccess;
  }

  public void setContext(String string)
  {
    // JSONSerializer doesn't like newlines at the head of the file
    JSON json = JSONSerializer.toJSON(string.replaceAll("^\n*", ""));
    setContext(JXPathContext.newContext(json));
  }


  private static String capitalize(String s)
  {
    if (s.length() == 0)
    {
      return s;
    }
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }
}
