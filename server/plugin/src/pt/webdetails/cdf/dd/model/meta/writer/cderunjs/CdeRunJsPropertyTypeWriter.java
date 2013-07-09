/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta.writer.cderunjs;

import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.meta.LabeledValue;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;

import pt.webdetails.cdf.dd.util.JsonUtils;

/**
 * @author dcleao
 */
public class CdeRunJsPropertyTypeWriter extends JsWriterAbstract implements IThingWriter
{
  public void write(java.lang.Object output, IThingWriteContext context, Thing t) throws ThingWriteException
  {
    PropertyType prop = (PropertyType)t;
    StringBuilder out = (StringBuilder)output;
    
    String camelName = prop.getCamelName();
    String jsStrCamelName = JsonUtils.toJsString(camelName);
    
    // NOTE: property type name cannot have spaces
    // or other special chars, but this is not enforced anywhere.
    String propName = prop.getName() + "Property";

    // Write Select renderer when it is a list
    String valuesSource = prop.getPossibleValuesSource();
    if(valuesSource != null || prop.getPossibleValueCount() > 0)
    {
      String rendererName = prop.getInputType() + "Renderer";

      out.append(NEWLINE);
      out.append("var ");
      out.append(rendererName);
      out.append(" = ");
      out.append(prop.getAttribute("BaseRenderer").getValue());
      out.append(".extend({"); out.append(NEWLINE);
      out.append(INDENT1); out.append("selectData: ");

      if(valuesSource != null)
      {
        out.append(NEWLINE);
        out.append(INDENT2); out.append(valuesSource); out.append(NEWLINE);
      }
      else
      {
        out.append("{"); out.append(NEWLINE);

        boolean isFirst = true;
        for(LabeledValue labeledValue : prop.getPossibleValues())
        {
          if(isFirst) { isFirst = false; }
          else        { addCommaAndLineSep(out); }
          out.append(INDENT2);
          out.append(JsonUtils.toJsString(labeledValue.getValue()));
          out.append(": ");
          out.append(JsonUtils.toJsString(labeledValue.getLabel()));
        }
        out.append(NEWLINE);
        out.append(INDENT1); out.append("}"); out.append(NEWLINE);
      }

      out.append("});");
      out.append(NEWLINE);
    }

    // TODO: Currently, the Property/Base property is not being taken
    // into account in the generated JS....
    out.append(NEWLINE);
    out.append("var ");
    out.append(propName);
    out.append(" = BasePropertyType.extend({");
    out.append(NEWLINE);

    addJsProperty(out, "type", jsStrCamelName, INDENT1, true);
    addCommaAndLineSep(out);
    out.append(INDENT1); out.append("stub: {"); out.append(NEWLINE);
    addJsProperty(out, "name",        jsStrCamelName, INDENT2, true);
    addJsProperty(out, "value",       prop.getDefaultValue(), INDENT2, false);
    addJsProperty(out, "description", JsonUtils.toJsString(prop.getLabel()), INDENT2, false);
    addJsProperty(out, "tooltip",     JsonUtils.toJsString(prop.getTooltip()), INDENT2, false);
    addJsProperty(out, "type",        JsonUtils.toJsString(prop.getInputType()), INDENT2, false);
    addJsProperty(out, "order",       String.valueOf(prop.getOrder()), INDENT2, false);
    
    // TODO: CDE editor only supports "simple" and "advanced" classTypes.
    String cat = prop.getCategory();
    if(!PropertyType.CAT_ADVANCED.equals(cat))
    {
      cat = "";
    }
    
    addJsProperty(out, "classType", JsonUtils.toJsString(cat == null ? "" : cat), INDENT2, false);
    out.append(NEWLINE);
    out.append(INDENT1); out.append("}"); out.append(NEWLINE);
    out.append("});"); out.append(NEWLINE);

    // TODO: this registers properties in a way that private properties
    // may shadow/replace global properties...
    // It's not the full name that is being used.
    out.append("PropertiesManager.register(new "); out.append(propName); out.append("());");
    out.append(NEWLINE);
  }
}