/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core.writer.js;

import org.apache.commons.lang.StringUtils;

/**
 * @author dcleao
 */
public abstract class JsWriterAbstract
{
  protected static final String INDENT1 = "\t";
  protected static final String INDENT2 = "\t\t";
  protected static final String INDENT3 = "\t\t\t";
  protected static final String INDENT4 = "\t\t\t\t";

  protected static final String NEWLINE = System.getProperty("line.separator");//TODO: do we really want this?

  protected static void addCommaAndLineSep(StringBuilder out)
  {
    out.append(",");
    out.append(NEWLINE);
  }
  
  protected static void addVar(StringBuilder out, String name, String value)
  {
    out.append("var ");
    addAssignmentWithOr(out, name, value);
  }

  protected static void addAssignmentWithOr(StringBuilder out, String name, String value)
  {
    out.append(name);
    out.append(" = ");
    out.append(name);
    out.append(" || ");
    out.append(value);
    out.append(";");
    out.append(NEWLINE);
  }
  
    protected static void addAssignment(StringBuilder out, String name, String value)
  {
    out.append(name);
    out.append(" = ");
    out.append(value);
    out.append(";");
    out.append(NEWLINE);
  }
  
  protected static void addJsProperty(StringBuilder out, String name, String jsValue, String indent)
  {
    addJsProperty(out, name, jsValue, indent, true);
  }
  
  protected static void addJsProperty(StringBuilder out, String name, String jsValue, String indent, boolean isFirst)
  {
    if(!isFirst) { addCommaAndLineSep(out); }

    if(StringUtils.isNotEmpty(indent)) { out.append(indent); }

    out.append(name);
    out.append(": ");
    out.append(jsValue);
  }
  
  protected static String wrapJsScriptTags(String code)
  {
    StringBuilder out = new StringBuilder();
    wrapJsScriptTags(out, code);
    return out.toString();
  }
  
  protected static void wrapJsScriptTags(StringBuilder out, String code)
  {
    out.append(NEWLINE);
    out.append("<script language=\"javascript\" type=\"text/javascript\">");
    out.append(NEWLINE);
    out.append(code);
    out.append(NEWLINE);
    out.append("</script>");
    out.append(NEWLINE);
  }
}
