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

package pt.webdetails.cdf.dd.render.cda;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import java.util.Map;

public class CompoundComponent implements CdaElementRenderer {

  private Map<String, Object> definition;
  protected static final Log logger = LogFactory.getLog( CompoundComponent.class );

  public void renderInto( Element compound ) {
    JXPathContext context = JXPathContext.newContext( definition );
    compound.setAttribute( "id", (String) context.getValue( "value", String.class ) );
  }

  public void setDefinition( Map<String, Object> definition ) {
    this.definition = definition;
  }

}
