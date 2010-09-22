package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;


public class ImageRender extends Render {

    public ImageRender(JXPathContext context) {
        super(context);
    }

    @Override
    public void processProperties() {

        getPropertyBag().addClass(getPropertyString("cssClass"));

    }

    public String renderStart() {

        String image = "<image src='" + getPropertyString("url") + "'";
        image += getPropertyBagString() + ">";
        return image;
    }

    public String renderClose() {
        return "</image>";
    }

}
