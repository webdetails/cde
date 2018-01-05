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
