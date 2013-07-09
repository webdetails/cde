/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;

public class HtmlRender extends Render {

    private boolean hasProperties = false;

    public HtmlRender(JXPathContext context) {
        super(context);
    }

    @Override
    public void processProperties() {

        getPropertyBag().addStyle("color", getPropertyString("color"));
        getPropertyBag().addClass(getPropertyString("cssClass"));
        String fontSize = getPropertyString("fontSize");
        if (fontSize.length() > 0) {
            getPropertyBag().addStyle("font-size", fontSize + "px");
        }

        if (getPropertyBagString().length() > 0) {
            hasProperties = true;
        }
        
        getPropertyBag().addClass(getPropertyString("cssClass"));

    }

    public String renderStart() {

        String out = "";
        if (hasProperties) {
            out += "<span " + getPropertyBagString() + ">";
        }

        out += getPropertyString("html");
        return out;

    }

    public String renderClose() {

        if (hasProperties) {
            return "</span>";
        } else {
            return "";
        }
    }
}
