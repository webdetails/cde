/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta.writer.cderunjs;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.Attribute;
import pt.webdetails.cdf.dd.model.meta.ComponentType;

/**
 * @author dcleao
 */
class CdeRunJsHelper 
{
  private CdeRunJsHelper() {}
  
  public static String getComponentTypeModelPrefix(ComponentType comp)
  {
    Attribute cdeModelPrefixAttr = comp.tryGetAttribute("cdeModelPrefix");
    String modelPrefix = cdeModelPrefixAttr != null ? cdeModelPrefixAttr.getValue() : null;
    return StringUtils.defaultIfEmpty(modelPrefix, "Components");
  }
  
  public static String getComponentTypeModelId(ComponentType comp)
  {
    return getComponentTypeModelId(comp, getComponentTypeModelPrefix(comp));
  }
  
  public static String getComponentTypeModelId(ComponentType comp, String modelPrefix)
  {
    return modelPrefix + comp.getName();
  }
}
