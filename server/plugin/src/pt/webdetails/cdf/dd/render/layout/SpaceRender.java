package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;

public class SpaceRender extends Render {

    public SpaceRender(JXPathContext context) {
        super(context);
    }

    @Override
    public void processProperties() {

        getPropertyBag().addStyle("background-color", getPropertyString("backgroundColor"));
        getPropertyBag().addClass(getPropertyString("cssClass"));
        getPropertyBag().addClass("space");
        getPropertyBag().addStyle("height", getPropertyString("height"));

    }


    public String renderStart() {
       
        String div = "<hr ";
        div += getPropertyBagString() + ">";
        return div;
    }

    public String renderClose() {
        return "</hr>";
    }
}
