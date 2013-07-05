
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

  protected static final String NEWLINE = System.getProperty("line.separator");

  protected static void addCommaAndLineSep(StringBuilder out)
  {
    out.append(",");
    out.append(NEWLINE);
  }
  
  protected static void addVar(StringBuilder out, String name, String value)
  {
    out.append("var ");
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
