/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components;

import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.model.inst.ExtensionPropertyBinding;
import pt.webdetails.cdf.dd.model.inst.GenericComponent;
import pt.webdetails.cdf.dd.model.inst.PropertyBinding;
import pt.webdetails.cdf.dd.model.meta.GenericComponentType;
import pt.webdetails.cdf.dd.model.meta.PropertyTypeUsage;
import pt.webdetails.cdf.dd.util.JsonUtils;

/**
 * @author dcleao
 */
public class CdfRunJsGenericComponentWriter extends JsWriterAbstract implements IThingWriter
{
  public void write(Object output, IThingWriteContext context, Thing t) throws ThingWriteException
  {
    this.write((StringBuilder)output, (CdfRunJsDashboardWriteContext)context, (GenericComponent)t);
  }
  
  public void write(StringBuilder out, CdfRunJsDashboardWriteContext context, GenericComponent comp) throws ThingWriteException
  {
    GenericComponentType compType = comp.getMeta();
    
    String id = context.getId(comp);
    
    out.append("var ");
    out.append(id);
    out.append(" = {");
    out.append(NEWLINE);
    addJsProperty(out, "type", JsonUtils.toJsString(compType.getName()), INDENT1, true );
    addJsProperty(out, "name", JsonUtils.toJsString(id                ), INDENT1, false);
    
    // Render definitions
    for(String definitionName : compType.getDefinitionNames())
    {
      addCommaAndLineSep(out);
      this.writeDefinition(definitionName, out, context, comp, compType);
    }
    
    out.append(NEWLINE);
    out.append("};");
    out.append(NEWLINE);
  }
  
  private void writeDefinition(
          String definitionName, 
          StringBuilder out, 
          CdfRunJsDashboardWriteContext context, 
          GenericComponent comp,
          GenericComponentType compType)
          throws ThingWriteException
  {
    String indent = INDENT1;
    
    boolean isDefaultDefinition = StringUtils.isEmpty(definitionName);
    if(!isDefaultDefinition)
    {
      addJsProperty(out, definitionName, " {", INDENT1, true);
      indent = INDENT2;
    }
    
    CdfRunJsDashboardWriteContext childContext = context.withIndent(indent);
    childContext.setIsFirstInList(true);
    
    IThingWriterFactory factory = context.getFactory();
    for(PropertyTypeUsage propUsage : compType.getPropertiesByDefinition(definitionName))
    {
      String propName = propUsage.getName();
      // The 'name' property is handled specially
      if(!(isDefaultDefinition && "name".equalsIgnoreCase(propName)))
      {
        PropertyBinding propBind = comp.tryGetPropertyBindingByName(propName);
        if(propBind != null)
        {
          IThingWriter writer;
          try
          {
            writer = factory.getWriter(propBind);
          }
          catch(UnsupportedThingException ex)
          {
            throw new ThingWriteException(ex);
          }
          
          if(!isDefaultDefinition && childContext.isFirstInList())
          {
            out.append(NEWLINE);
          }
          
          writer.write(out, childContext, propBind);
        }
      }
    }
    
    if(comp.getExtensionPropertyBindingCount() > 0)
    {
      // HACK: CCC V1 properties have to go into the "chartDefinition" definition...
      boolean isCCC = compType.getName().startsWith("ccc");
      if(isCCC ? !isDefaultDefinition : isDefaultDefinition)
      {
        Iterable<ExtensionPropertyBinding> propBinds = comp.getExtensionPropertyBindings();
        for(ExtensionPropertyBinding propBind : propBinds)
        {
          IThingWriter writer;
          try
          {
            writer = factory.getWriter(propBind);
          }
          catch(UnsupportedThingException ex)
          {
            throw new ThingWriteException(ex);
          }

          if(!isDefaultDefinition && childContext.isFirstInList())
          {
            out.append(NEWLINE);
          }
          
          writer.write(out, childContext, propBind);
        }
      }
    }
    
    if(!isDefaultDefinition)
    {
      out.append(NEWLINE);
      out.append(INDENT1);
      out.append("}");
    }
  }
}