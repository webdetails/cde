package pt.webdetails.cdf.dd.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Class to hold the descriptors for a .wcdf file Date: Dec 23, 2009 Time:
 * 3:45:53 PM To change this template use File | Settings | File Templates.
 */
public class WcdfDescriptor
{

  private String title = "";
  private String author;
  private String description = "";
  private String style;
  private String rendererType;
  private String wcdfFilePath;
  private List<String> widgetParameters;
  private boolean isWidget;

  public WcdfDescriptor()
  {
    widgetParameters = new ArrayList<String>();
  }

  /**
   * Generates a JSONObject representing this descriptor
   *
   * @return
   */
  public JSONObject toJSON()
  {

    JSONObject json = new JSONObject();
    json.put("title", getTitle());
    json.put("author", getAuthor());
    json.put("description", getDescription());
    json.put("style", getStyle());
    json.put("widget", isWidget());
    json.put("rendererType", getRendererType());
    JSONArray arr = new JSONArray();
    StringBuilder sb = new StringBuilder();
    for (String s : widgetParameters)
    {
      arr.add(s);
    }
    json.put("widgetParameters", arr);
    return json;

  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getAuthor()
  {
    return author;
  }

  public void setAuthor(String author)
  {
    this.author = author;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getStyle()
  {
    return style;
  }

  public void setStyle(String style)
  {
    this.style = style;
  }

  /**
   * @return the rendererType
   */
  public String getRendererType()
  {
    return rendererType;
  }

  /**
   * @param rendererType the rendererType to set
   */
  public void setRendererType(String rendererType)
  {
    this.rendererType = rendererType;
  }

  public void setWcdfPath(String wcdfFilePath)
  {
    this.wcdfFilePath = wcdfFilePath;
  }

  public String getWcdfPath()
  {
    return wcdfFilePath;
  }

  public String getStructurePath()
  {
    return wcdfFilePath == null? null : wcdfFilePath.replace(".wcdf", ".cdfde");
  }

  public void setWidget(boolean isWidget)
  {
    this.isWidget = isWidget;
  }

  public boolean isWidget()
  {
    return isWidget;
  }

  void setWidgetParameters(String[] params)
  {
    setWidgetParameters(Arrays.asList(params));
  }

  void setWidgetParameters(List params)
  {
    widgetParameters = params;
  }

  public String[] getWidgetParameters()
  {
    return widgetParameters.toArray(new String[0]);
  }
}
