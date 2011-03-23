package pt.webdetails.cdf.dd.structure;

import net.sf.json.JSONObject;

/**
 * Class to hold the descriptors for a .wcdf file
 * Date: Dec 23, 2009
 * Time: 3:45:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class WcdfDescriptor {


  private String title = "";
  private String author;
  private String description = "";
  private String style;
  private String rendererType;

  public WcdfDescriptor() {
  }


  /**
   * Generates a JSONObject representing this descriptor
   * @return
   */
  public JSONObject toJSON(){

    JSONObject json = new JSONObject();
    json.put("title",getTitle());
    json.put("author",getAuthor());
    json.put("description",getDescription());
    json.put("style",getStyle());
    json.put("rendererType",getRendererType());

    return json;

  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getStyle() {
    return style;
  }

  public void setStyle(String style) {
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

}
