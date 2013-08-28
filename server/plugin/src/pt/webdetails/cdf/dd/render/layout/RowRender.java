/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;

public class RowRender extends DivRender {

    public RowRender(JXPathContext context) {
        super(context);
    }

     @Override
    public void processProperties() {

        super.processProperties();

        getPropertyBag().addClass("row");
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
