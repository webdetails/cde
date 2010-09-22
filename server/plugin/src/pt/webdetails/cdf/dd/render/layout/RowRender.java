package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;

public class RowRender extends DivRender {

    public RowRender(JXPathContext context) {
        super(context);
    }

     @Override
    public void processProperties() {

        super.processProperties();

        getPropertyBag().addClass("clearfix");

    }

    @Override
    public String renderStart() {

        String div = "<div ";
        div += getPropertyBagString() + ">";
        return div;
    }

    @Override
    public String renderClose() {
        return "</div>";
    }
}
