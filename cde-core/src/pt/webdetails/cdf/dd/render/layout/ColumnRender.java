/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;
import pt.webdetails.cdf.dd.util.XPathUtils;

@SuppressWarnings("unchecked")
public class ColumnRender extends DivRender {
	
	private String renderType;

    public ColumnRender(JXPathContext context) {
        super(context);
        renderType = XPathUtils.getStringValue(context, "//rendererType");
    }

    @Override
    public void processProperties() {

        super.processProperties();

        final String spanPrefix = renderType.equals("bootstrap") ? "col-md-" : "span-" ;
        
        getPropertyBag().addColClass(spanPrefix, getPropertyString("columnSpan"));
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
