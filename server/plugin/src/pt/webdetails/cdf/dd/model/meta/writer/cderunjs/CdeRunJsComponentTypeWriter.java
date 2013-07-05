
package pt.webdetails.cdf.dd.model.meta.writer.cderunjs;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.meta.DataSourceComponentType;
import pt.webdetails.cdf.dd.model.core.Attribute;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.meta.PropertyTypeUsage;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.util.JsonUtils;

/**
 * @author dcleao
 */
public class CdeRunJsComponentTypeWriter extends JsWriterAbstract implements IThingWriter
{
  public void write(Object output, IThingWriteContext context, Thing t) throws ThingWriteException
  {
    ComponentType comp = (ComponentType)t;
    StringBuilder out  = (StringBuilder)output;
  
    Attribute cdeModelIgnoreAttr = comp.tryGetAttribute("cdeModelIgnore");
    
    if(cdeModelIgnoreAttr != null && "true".equals(cdeModelIgnoreAttr.getValue())) { return; }
    
    String name = comp.getName();
    
    // the name in cdefdejs/components/rows/type
    Attribute cdeModelPrefixAttr = comp.tryGetAttribute("cdeModelPrefix");
    String modelPrefix = cdeModelPrefixAttr != null ? cdeModelPrefixAttr.getValue() : null;
    if(StringUtils.isEmpty(modelPrefix)) { modelPrefix = "Components"; }
    
    String modelName = modelPrefix + name + "Model";
    String modelId   = modelPrefix + name;
    
    String label = comp.getLabel();
    String jsTooltip = JsonUtils.toJsString(comp.getTooltip());

    // --------------
    // ENTRY
    if(comp.getVisible()) 
    {
      String entryName = name + "Entry";
      String entryId   = name.toUpperCase() + "_ENTRY";
      String baseEntryType = comp.tryGetAttributeValue("cdePalleteType", "PalleteEntry");
      out.append(NEWLINE);
      out.append("var ");
      out.append(entryName);
      out.append(" = ");
      out.append(baseEntryType);
      out.append(".extend({");
      out.append(NEWLINE);

      addJsProperty(out, "id",           JsonUtils.toJsString(entryId), INDENT1, true);
      addJsProperty(out, "name",         JsonUtils.toJsString(label  ), INDENT1, false);
      addJsProperty(out, "description",  jsTooltip, INDENT1, false);
      addJsProperty(out, "category",     JsonUtils.toJsString(comp.getCategory()), INDENT1, false);
      addJsProperty(out, "categoryDesc", JsonUtils.toJsString(comp.getCategoryLabel()), INDENT1, false);
      addCommaAndLineSep(out);
      out.append(INDENT1);
      out.append("getStub: function() {");
      out.append(NEWLINE);
      out.append(INDENT2);
      out.append("return ");
      out.append(modelName);
      out.append(".getStub();");
      out.append(NEWLINE);
      out.append(INDENT1);
      out.append("}");
      out.append(NEWLINE);
      out.append("});");
      out.append(NEWLINE);
      
      // TODO: maybe asbtract this into some explicit ComponentType «Class» concept/field or something?
      String collectionName = (comp instanceof DataSourceComponentType) ?
              "CDFDDDatasourcesArray" : 
              "CDFDDComponentsArray";

      out.append(collectionName);
      out.append(".push(new ");
      out.append(entryName);
      out.append("());");
      out.append(NEWLINE);
    }
    
    // --------------
    // OWN PROPERTIES
    if(comp.getPropertyUsageCount() > 0)
    {
      IThingWriterFactory factory = context.getFactory();
      for(PropertyTypeUsage propUsage : comp.getPropertyUsages())
      {
        if(propUsage.isOwned())
        {
          PropertyType prop = propUsage.getProperty();
          IThingWriter writer;
          try
          {
            writer = factory.getWriter(prop);
          }
          catch (UnsupportedThingException ex)
          {
            throw new ThingWriteException(ex);
          }

          writer.write(out, context, prop);
        }
      }
    }
    
    // --------------
    // MODEL
    out.append(NEWLINE);
    out.append("var "); out.append(modelName); out.append(" = BaseModel.extend({}, {"); out.append(NEWLINE);
    out.append(INDENT1);
    out.append("MODEL: ");
    out.append(JsonUtils.toJsString(modelId));
    addCommaAndLineSep(out);
    out.append(INDENT1); out.append("getStub: function() {"); out.append(NEWLINE);
    out.append(INDENT2); out.append("return {"); out.append(NEWLINE);
    
    addJsProperty(out, "id", "TableManager.generateGUID()", INDENT3, true);
    addJsProperty(out, "type", modelName + ".MODEL", INDENT3, false);
    addJsProperty(out, "typeDesc", jsTooltip, INDENT3, false);

    for(Attribute attribute : comp.getAttributes())
    {
      if(!"cdeModelIgnore".equals(attribute.getName()))
      {
        String jsAttrName = attribute.getName();
        if(StringUtils.isEmpty(jsAttrName))
        {
          jsAttrName = "meta";
        }
        else
        {
          jsAttrName = "meta_" + jsAttrName;
        }
        
        addJsProperty(
            out,
            JsonUtils.toJsString(jsAttrName),
            JsonUtils.toJsString(attribute.getValue()),
            INDENT3,
            false);
      }
    }
    
    addJsProperty(out, "parent", "IndexManager.ROOTID", INDENT3, false);

    // TODO: shouldn't components instead receive a list of aliased props?
    addJsProperty(out, "properties", "[", INDENT3, false);
    if(comp.getPropertyUsageCount() > 0)
    {
      boolean isFirst = true;
      for(PropertyTypeUsage propUsage : comp.getPropertyUsages())
      {
        if(isFirst) { isFirst = false; }
        else        { out.append(","); }
        out.append(NEWLINE);
        out.append(INDENT4);
        out.append(JsonUtils.toJsString(propUsage.getProperty().getCamelName()));
      }
      
      out.append(NEWLINE);
    }
    
    out.append(INDENT3); out.append("].map(PropertiesManager.getProperty, PropertiesManager)");  out.append(NEWLINE);
    out.append(INDENT2); out.append("};"); out.append(NEWLINE); // return {
    out.append(INDENT1); out.append("}");  out.append(NEWLINE);  // getStub
    out.append("});"); out.append(NEWLINE); // .extend({

    out.append("BaseModel.registerModel(");
    out.append(modelName);
    out.append(");"); out.append(NEWLINE);
  }
}
