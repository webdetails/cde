package pt.webdetails.cdf.dd.render.components;

import org.apache.commons.jxpath.JXPathContext;

import pt.webdetails.cdf.dd.util.XPathUtils;

public class BaseComponent extends Component implements IComponent {

  public BaseComponent(JXPathContext context) {
    super(context);
    this.id = "render_" + XPathUtils.getStringValue(getNode(), "properties/value[../name='name']").replace(" ", "_");
  }

  public BaseComponent() {
  }

  @Override
  public String getProperties() {
    addProperty("name", this.id, true);
    return super.getProperties();
  }

  public String render() {
    return "";
  }

  public String render(JXPathContext context) {
    return "";
  }

  @Override
  public void setNode(JXPathContext node) {
    clearProperties();
    super.setNode(node);
    this.id = "render_" + XPathUtils.getStringValue(getNode(), "properties/value[../name='name']").replace(" ", "_");
  }

  public String getEntry() {
    return "";
  }

  public String getModel() {
    return "";
  }

  public String getName() {
    return "";
  }
}
