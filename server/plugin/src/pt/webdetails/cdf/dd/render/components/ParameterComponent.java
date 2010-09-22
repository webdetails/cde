package pt.webdetails.cdf.dd.render.components;

import org.apache.commons.jxpath.JXPathContext;

import pt.webdetails.cdf.dd.util.XPathUtils;

public class ParameterComponent extends BaseComponent {

    public ParameterComponent(JXPathContext context) {
        super(context);
    }

    public ParameterComponent() {
        super();
    }

  @Override
    public void setNode(JXPathContext node) {
        clearProperties();
        super.setNode(node);
        this.id = XPathUtils.getStringValue(getNode(),"properties/value[../name='name']").replace(" ","_");
    }
  @Override
    public String getName() {
        return "Parameter";
    }
    
  @Override
    public String render() {
    	String name = XPathUtils.getStringValue(getNode(),"properties/value[../name='name']");
    	String value = XPathUtils.getStringValue(getNode(),"properties/value[../name='propertyValue']");
    	return "var " + name + " = \"" + value + "\";" + newLine; 
     }

  @Override
  public String render(JXPathContext context) {
    	String name = XPathUtils.getStringValue(context,"properties/value[../name='name']");
    	String value = XPathUtils.getStringValue(context,"properties/value[../name='propertyValue']");
    	return "var " + name + " = \"" + value + "\";" + newLine;
     }
}
