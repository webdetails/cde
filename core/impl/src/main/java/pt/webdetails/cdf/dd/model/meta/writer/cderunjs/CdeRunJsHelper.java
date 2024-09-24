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

package pt.webdetails.cdf.dd.model.meta.writer.cderunjs;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.Attribute;
import pt.webdetails.cdf.dd.model.meta.ComponentType;

public final class CdeRunJsHelper {
  private CdeRunJsHelper() { }

  public static String getComponentTypeModelPrefix( ComponentType comp ) {
    Attribute cdeModelPrefixAttr = comp.tryGetAttribute( "cdeModelPrefix" );
    String modelPrefix = cdeModelPrefixAttr != null ? cdeModelPrefixAttr.getValue() : null;
    return StringUtils.defaultIfEmpty( modelPrefix, "Components" );
  }

  public static String getComponentTypeModelId( ComponentType comp ) {
    return getComponentTypeModelId( comp, getComponentTypeModelPrefix( comp ) );
  }

  public static String getComponentTypeModelId( ComponentType comp, String modelPrefix ) {
    return modelPrefix + comp.getName();
  }
}
