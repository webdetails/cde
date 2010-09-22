package pt.webdetails.cdf.dd.render.components;

import org.apache.commons.jxpath.JXPathContext;

import pt.webdetails.cdf.dd.util.XPathUtils;

public class FunctionComponent extends BaseComponent {

    public FunctionComponent(JXPathContext context) {
        super(context);
    }

    public void setNode(JXPathContext node) {
        clearProperties();
        super.setNode(node);
        this.id = XPathUtils.getStringValue(getNode(),"properties/value[../name='name']").replace(" ","_");
    }
    public FunctionComponent() {
        super();
    }

    public String render() {
    	return  XPathUtils.getStringValue(getNode(),"properties/value[../name='javaScript']") + newLine;
     }
  public String render(JXPathContext context) {
return  XPathUtils.getStringValue(context,"properties/value[../name='javaScript']") + newLine;
     }
    public String getName() {
        return "Function";
    }
}
