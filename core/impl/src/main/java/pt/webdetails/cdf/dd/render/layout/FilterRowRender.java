/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;
import pt.webdetails.cdf.dd.util.XPathUtils;

public class FilterRowRender extends DivRender {

  public FilterRowRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public String renderClose() {
    return "</span></span>";
  }

  @Override
  public void processProperties() {
    super.processProperties();
    getPropertyBag().addId( getId() );
  }

  @Override
  public String renderStart() {
    String id = getPropertyString( "name" ),
      label = getPropertyString( "label" );
    String div = "<span class='filter'><span class='label'>";
    div += label + ": </span><span id ='" + id + "' class='selector'>";
    return div;
  }

  protected String getId() {
    String id = getPropertyString( "name" );
    return id.length() > 0 ? id : XPathUtils.getStringValue( getNode(), "id" );
  }
}
