/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components;

import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.util.JsonUtils;

/**
 * @author dcleao
 */
public class CdfRunJsExpressionParameterComponentWriter extends CdfRunJsParameterComponentWriter
{
  @Override
  public void write(StringBuilder out, CdfRunJsDashboardWriteContext context, ParameterComponent comp) throws ThingWriteException
  {
    String name  = JsonUtils.toJsString( context.getId(comp));
    String value = sanitizeExpression( comp.tryGetPropertyValue("javaScript", "") );
    Boolean isBookmarkable = "true".equalsIgnoreCase( comp.tryGetPropertyValue("bookmarkable", null) );

    addSetParameterAssignment(out, name, value);
    if (isBookmarkable){
      addBookmarkable(out, name);
    }
  }

  protected static String sanitizeExpression(String expr){
    return expr.replaceAll("[;\\s]+$", "");
  }
}
