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


package pt.webdetails.cdf.dd.render.layout;

import java.text.MessageFormat;
import org.apache.commons.jxpath.JXPathContext;
import org.owasp.encoder.Encode;
import pt.webdetails.cdf.dd.CdeConstants;

public class ResourceFileRender extends ResourceRender {

  public ResourceFileRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public String renderStart() {
    final String resourceType = getPropertyString( CdeConstants.RESOURCE_TYPE );

    if ( resourceType.equals( CdeConstants.CSS ) ) {
      return MessageFormat.format(
              CdeConstants.LINK,
              Encode.forHtmlAttribute( getPropertyString( CdeConstants.RESOURCE_FILE ) ) );
    } else if ( resourceType.equals( CdeConstants.JAVASCRIPT ) ) {
      return MessageFormat.format(
              CdeConstants.SCRIPT_FILE,
              Encode.forHtmlAttribute( getPropertyString( CdeConstants.RESOURCE_FILE ) ) );
    }

    logger.error( "Resource not rendered" );

    return "";
  }
}
