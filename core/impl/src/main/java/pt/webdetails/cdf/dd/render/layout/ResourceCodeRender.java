/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cdf.dd.render.layout;

import java.text.MessageFormat;
import org.apache.commons.jxpath.JXPathContext;
import pt.webdetails.cdf.dd.CdeConstants;

public class ResourceCodeRender extends ResourceRender {

  public ResourceCodeRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public String renderStart() {
    final String resourceType = getPropertyString( CdeConstants.RESOURCE_TYPE );

    if ( resourceType.equals( CdeConstants.CSS ) ) {
      return MessageFormat.format( CdeConstants.STYLE, getPropertyString( CdeConstants.RESOURCE_CODE ) );
    } else if ( resourceType.equals( CdeConstants.JAVASCRIPT ) ) {
      return MessageFormat.format( CdeConstants.SCRIPT_SOURCE, getPropertyString( CdeConstants.RESOURCE_CODE ) );
    }

    logger.error( "Resource not rendered" );

    return "";
  }
}
