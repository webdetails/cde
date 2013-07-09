/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

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
        getPropertyBag().addStyle("height", getPropertyString("height")+"px");

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
