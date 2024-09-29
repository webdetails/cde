/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
