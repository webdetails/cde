package pt.webdetails.cdf.dd.render.datasources;

import java.util.Collection;
import java.util.Iterator;

import net.sf.json.JSONObject;
import pt.webdetails.cdf.dd.render.components.BaseComponent;

public abstract class BaseDataSource extends BaseComponent {

  protected JSONObject definition;

  protected String description;

  protected String id;

  protected String iname;

  protected String name;

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  @Override
  public String render() throws UnsupportedOperationException {
    throw new UnsupportedOperationException("Data Sources can't be rendered");
  }

  /**
   * @param definition the definition to set
   */
  protected void setDefinition(JSONObject definition) {
    this.definition = definition;
  }

  /**
   * @param description the description to set
   */
  protected void setDescription(String description) {
    this.description = description;
  }

  /**
   * @param id the id to set
   */
  protected void setId(String id) {
    this.id = id;
  }

  /**
   * @param iname the iname to set
   */
  protected void setIname(String iname) {
    this.iname = iname;
  }

  /**
   * @param name the name to set
   */
  protected void setName(String name) {
    this.name = name;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final int maxLen = 10;
    StringBuilder builder = new StringBuilder();
    builder.append("BaseDataSource [");
    if (description != null) {
      builder.append("description=");
      builder.append(description);
      builder.append(", ");
    }
    if (id != null) {
      builder.append("id=");
      builder.append(id);
      builder.append(", ");
    }
    if (name != null) {
      builder.append("name=");
      builder.append(name);
      builder.append(", ");
    }
    if (iname != null) {
      builder.append("iname=");
      builder.append(iname);
      builder.append(", ");
    }
    if (definition != null) {
      builder.append("definition=");
      builder.append(toString(definition.entrySet(), maxLen));
    }
    builder.append("]");
    return builder.toString();
  }
  
  private String toString(Collection<?> collection, int maxLen) {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    int i = 0;
    for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
      if (i > 0)
        builder.append(", ");
      builder.append(iterator.next());
    }
    builder.append("]");
    return builder.toString();
  }

}
