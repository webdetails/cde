/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;

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
        String height = getPropertyString("height");
        if(StringUtils.isNotEmpty(height)){
          getPropertyBag().addStyle("height", height + "px");
        }
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
