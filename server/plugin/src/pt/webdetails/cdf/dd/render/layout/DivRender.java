package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;
import pt.webdetails.cdf.dd.util.XPathUtils;

public class DivRender extends Render {


    public DivRender(JXPathContext context) {
        super(context);
    }

    public String renderClose() {
        return "</div>";
    }

    
    @Override
    public void processProperties() {
        
        getPropertyBag().addId(getId() );
    	getPropertyBag().addClass(getPropertyString("roundCorners"));
        getPropertyBag().addClass(getPropertyString("cssClass"));
        getPropertyBag().addStyle("background-color", getPropertyString("backgroundColor"));
        getPropertyBag().addStyle("height", getPropertyString("height"));
        getPropertyBag().addStyle("text-align", getPropertyString("textAlign"));

    }

    @Override
    public String renderStart() {

        String div = "<div ";
        div += getPropertyBagString() + ">";
        return div;
    }

    protected String getId() {
        String id = getPropertyString("name");
        return id.length() > 0 ? id : XPathUtils.getStringValue(getNode(), "id");
    }

    }
