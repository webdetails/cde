package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.util.PropertyBag;
import pt.webdetails.cdf.dd.util.XPathUtils;

@SuppressWarnings("unchecked")
public abstract class Render {

    private JXPathContext node;
    protected static final Log logger = LogFactory.getLog(Render.class);
    private PropertyBag propertyBag;

    public Render(JXPathContext node) {
        this.propertyBag = new PropertyBag();
        this.node = node;
    }

    public JXPathContext getNode() {
        return node;
    }

    public void setNode(JXPathContext node) {
        this.node = node;
    }

    public String getPropertyBagString() {

        return propertyBag.getPropertiesString();

    }

    protected boolean hasProperty(String property) {

        return XPathUtils.exists(getNode(), "properties/value[../name='" + property + "']");

    }

    protected String getPropertyString(String property) {

        return XPathUtils.getStringValue(getNode(), "properties/value[../name='" + property + "']");

    }

    protected Boolean getPropertyBoolean(String property) {

        return XPathUtils.getBooleanValue(getNode(), "properties/value[../name='" + property + "']");

    }

    public PropertyBag getPropertyBag() {

        return propertyBag;
    }

    public abstract void processProperties();

    public abstract String renderStart();

    public abstract String renderClose();
}
