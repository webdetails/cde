/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

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
