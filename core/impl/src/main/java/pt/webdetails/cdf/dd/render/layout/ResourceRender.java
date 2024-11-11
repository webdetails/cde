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
import org.apache.commons.lang.StringUtils;
import org.owasp.encoder.Encode;
import pt.webdetails.cdf.dd.CdeConstants;

public class ResourceRender extends Render {


  public ResourceRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public void processProperties() {
  }

  @Override
  public String renderStart() {
    // render legacy LayoutResource type
    final String resourceType = getPropertyString( CdeConstants.RESOURCE_TYPE );
    String value;
    if ( !StringUtils.isEmpty( value = getPropertyString( CdeConstants.RESOURCE_FILE ) ) ) {
      // render RESOURCE_FILE
      if ( resourceType.equals( CdeConstants.CSS ) ) {
        return MessageFormat.format( CdeConstants.LINK, Encode.forHtmlAttribute( value ) );
      } else if ( resourceType.equals( CdeConstants.JAVASCRIPT ) ) {
        return MessageFormat.format( CdeConstants.SCRIPT_FILE, Encode.forHtmlAttribute( value ) );
      }
    }
    if ( !StringUtils.isEmpty( value = getPropertyString( CdeConstants.RESOURCE_CODE ) ) ) {
      // render RESOURCE_CODE
      if ( resourceType.equals( CdeConstants.CSS ) ) {
        return MessageFormat.format( CdeConstants.STYLE, value );
      } else if ( resourceType.equals( CdeConstants.JAVASCRIPT ) ) {
        return MessageFormat.format( CdeConstants.SCRIPT_SOURCE, value );
      }
    }

    logger.error( "Resource not rendered" );
    return "";
  }

  @Override
  public String renderClose() {
    return "";
  }
}
