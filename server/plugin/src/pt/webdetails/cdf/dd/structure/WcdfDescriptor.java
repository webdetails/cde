/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import pt.webdetails.cdf.dd.CdfStyles;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;

/**
 * Class to hold the descriptors for a .wcdf file 
 */
public class WcdfDescriptor
{
  private static final Log _logger = LogFactory.getLog(WcdfDescriptor.class);
  
  public enum DashboardRendererType
  {
    MOBILE, BLUEPRINT, BOOTSTRAP
  }
  
  private String _title = "";
  private String _author;
  private String _description = "";
  private String _style;
  private String _rendererType;
  private String _path;
  private List<String> _widgetParameters;
  private String  _widgetName;
  private boolean _isWidget;

  public WcdfDescriptor()
  {
    _widgetParameters = new ArrayList<String>();
  }

  /**
   * Generates a JSONObject representing this descriptor
   *
   * @return
   */
  public JSONObject toJSON()
  {
    JSONObject json = new JSONObject();
    json.put("title",        getTitle());
    json.put("author",       getAuthor());
    json.put("description",  getDescription());
    json.put("style",        getStyle());
    json.put("widgetName",   getWidgetName());
    json.put("widget",       isWidget());
    json.put("rendererType", getRendererType());
    
    JSONArray aWidgetParams = new JSONArray();
    for(String s : _widgetParameters)
    {
      aWidgetParams.add(s);
    }
    
    json.put("widgetParameters", aWidgetParams);
    return json;
  }

  public static WcdfDescriptor fromXml(Document wcdfDoc)
  {
    WcdfDescriptor wcdf = new WcdfDescriptor();
    
    wcdf.setTitle(XmlDom4JHelper.getNodeText("/cdf/title", wcdfDoc, ""));
    wcdf.setDescription(XmlDom4JHelper.getNodeText("/cdf/description", wcdfDoc, ""));
    wcdf.setWidget(XmlDom4JHelper.getNodeText("/cdf/widget", wcdfDoc, "false").equals("true"));
    wcdf.setWidgetName(XmlDom4JHelper.getNodeText("/cdf/widgetName", wcdfDoc, ""));
    wcdf.setAuthor(XmlDom4JHelper.getNodeText("/cdf/author", wcdfDoc, ""));
    wcdf.setStyle(XmlDom4JHelper.getNodeText("/cdf/style", wcdfDoc, CdfStyles.DEFAULTSTYLE));
    wcdf.setRendererType(XmlDom4JHelper.getNodeText("/cdf/rendererType", wcdfDoc, "blueprint"));
    
    String widgetParams = XmlDom4JHelper.getNodeText("/cdf/widgetParameters", wcdfDoc, "");
    if(StringUtils.isNotEmpty(widgetParams))
    {
      wcdf.setWidgetParameters(widgetParams.split(","));
    }
    
    return wcdf;
  }
  
  public Document toXml()
  {
    Document doc = DocumentHelper.createDocument();
    Element cdfElem = doc.addElement("cdf");
    cdfElem.addElement("title").setText(StringUtils.defaultIfEmpty(this.getTitle(), ""));
    cdfElem.addElement("description").setText(StringUtils.defaultIfEmpty(this.getDescription(), ""));
    cdfElem.addElement("author").setText(StringUtils.defaultIfEmpty(this.getAuthor(), ""));
    cdfElem.addElement("style").setText(StringUtils.defaultIfEmpty(this.getStyle(), ""));
    cdfElem.addElement("rendererType").setText(StringUtils.defaultIfEmpty(this.getRendererType(), ""));
    cdfElem.addElement("widget").setText(this.isWidget() ? "true" : "false");
    cdfElem.addElement("widgetName").setText(StringUtils.defaultIfEmpty(this.getWidgetName(), ""));
    cdfElem.addElement("widgetParameters").setText(StringUtils.join(getWidgetParameters(), ","));
    
    return doc;
  }
  
  public void update(HashMap<String, Object> parameters)
  {
    if(parameters.containsKey("title"))
    {
      setTitle((String)parameters.get("title"));
    }
    if(parameters.containsKey("author"))
    {
      setAuthor((String)parameters.get("author"));
    }
    if(parameters.containsKey("description"))
    {
      setDescription((String)parameters.get("description"));
    }
    if(parameters.containsKey("style"))
    {
      setStyle((String)parameters.get("style"));
    }
    if(parameters.containsKey("rendererType"))
    {
      setRendererType((String)parameters.get("rendererType"));
    }
    if(parameters.containsKey("widgetName"))
    {
      setWidgetName((String)parameters.get("widgetName"));
    }
    if(parameters.containsKey("widget"))
    {
      setWidget("true".equals(parameters.get("widget")));
    }
    if(parameters.containsKey("widgetParameters"))
    {
      Object widgetParams = parameters.get("widgetParameters");
      String widgetParameters[] = null;
      if (widgetParams instanceof String[])
      {
        widgetParameters = (String[]) widgetParams;
      }
      else if(widgetParams != null)
      {
        String widgetParamName = widgetParams.toString();
        if(StringUtils.isNotEmpty(widgetParamName))
        {
          widgetParameters = new String[1];
          widgetParameters[0] = widgetParamName;
        }
        else
        {
          widgetParameters = new String[0];
        }
      }

      setWidgetParameters(widgetParameters);
    }
  }
  
  public String getTitle()
  {
    return _title;
  }

  public void setTitle(String title)
  {
    this._title = title;
  }

  public String getAuthor()
  {
    return _author;
  }

  public void setAuthor(String author)
  {
    this._author = author;
  }

  public String getDescription()
  {
    return _description;
  }

  public void setDescription(String description)
  {
    this._description = description;
  }

  public String getStyle()
  {
    return _style;
  }

  public void setStyle(String style)
  {
    this._style = style;
  }

  /**
   * @return the rendererType
   */
  public String getRendererType()
  {
    return _rendererType;
  }
  
  public DashboardRendererType getParsedRendererType()
  {
    // Until we consider it safe to assume that all dashboards have
    // their renderer type correctly identified, 
    // we'll have to default to assuming they're blueprint-style dashboards.
    return parseRendererType(this._rendererType, DashboardRendererType.BLUEPRINT);
  }
  
  /**
   * @param rendererType the rendererType to set
   */
  public void setRendererType(String rendererType)
  {
    this._rendererType = rendererType;
  }

  public void setPath(String wcdfFilePath)
  {
    this._path = wcdfFilePath;
  }

  public String getPath()
  {
    return _path;
  }

  public String getStructurePath()
  {
    return _path == null? null : _path.replace(".wcdf", ".cdfde");
  }
  
  public static String toStructurePath(String wcdfPath)
  {
    return wcdfPath == null ? wcdfPath : wcdfPath.replace(".wcdf", ".cdfde");
  }
  
  public String getWidgetName()
  {
    return _widgetName;
  }

  public void setWidgetName(String widgetName)
  {
    this._widgetName = widgetName;
  }

  public void setWidget(boolean isWidget)
  {
    this._isWidget = isWidget;
  }

  public boolean isWidget()
  {
    return _isWidget;
  }

  public void setWidgetParameters(String[] params)
  {
    if(params != null) 
    {
      this._widgetParameters = Arrays.asList(params);
    }
    else 
    {
      this._widgetParameters.clear();
    }
  }

  public String[] getWidgetParameters()
  {
    return this._widgetParameters.toArray(new String[0]);
  }
  
  public static WcdfDescriptor load(String wcdfFilePath, IPentahoSession userSession) throws IOException
  {
    IRepositoryAccess repository = PentahoRepositoryAccess.getRepository(userSession);
    if(!repository.resourceExists(wcdfFilePath))
    {
      return null;
    }
    
    Document wcdfDoc = repository.getResourceAsDocument(wcdfFilePath);
    WcdfDescriptor wcdf = WcdfDescriptor.fromXml(wcdfDoc);
    wcdf.setPath(wcdfFilePath);
    return wcdf;
  }
  
  public static DashboardRendererType parseRendererType(String rendererType, DashboardRendererType defaultValue)
  {
    if(!StringUtils.isEmpty(rendererType))
    {
      try
      {
        return DashboardRendererType.valueOf(rendererType.toUpperCase());
      }
      catch (IllegalArgumentException ex)
      {
        _logger.error("Bad renderer type: " + rendererType);
      }
    }
    
    return defaultValue;
  }
}
