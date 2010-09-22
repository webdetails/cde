package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;

@SuppressWarnings("unchecked")
public class ColumnRender extends DivRender {

    public ColumnRender(JXPathContext context) {
        super(context);
    }

    @Override
    public void processProperties() {

        super.processProperties();

        getPropertyBag().addColClass("span-", getPropertyString("columnSpan"));
        getPropertyBag().addColClass("append-", getPropertyString("columnAppend"));
        getPropertyBag().addColClass("prepend-", getPropertyString("columnPrepend"));
        getPropertyBag().addColClass(".prepend-top", getPropertyBoolean("columnPrependTop"));
        getPropertyBag().addColClass(".append-bottom", getPropertyBoolean("columnAppendBottom"));
        getPropertyBag().addColClass("border", getPropertyBoolean("columnBorder"));
        getPropertyBag().addColClass("colborder", getPropertyBoolean("columnBigBorder"));

    }



  @Override
    public String renderStart() {

        
        String div = "<div ";
        
        if (lastColumn()) {
            getPropertyBag().addClass("last");
        }
        div += getPropertyBagString() + ">";

        return div;
    }



    private boolean lastColumn() {

        String parentId = (String) getNode().getValue("parent");
        return ((Boolean) getNode().getValue("not(following-sibling::*[parent='" + parentId + "'][type='LayoutColumn'])")).booleanValue();

    }

    @Override
    public String renderClose() {
        return "</div>";
    }
}
