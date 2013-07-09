/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.reader.cdfdejs;

import java.util.Iterator;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.IThingReader;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.LayoutComponent;
import pt.webdetails.cdf.dd.model.meta.DashboardType;

/**
 * @author dcleao
 */
public class CdfdeJsDashboardReader implements IThingReader
{
  public Dashboard.Builder read(IThingReadContext context, Object source, String sourcePath) 
          throws ThingReadException
  {
    Dashboard.Builder builder = new Dashboard.Builder();
    this.read(builder, context, source, sourcePath);
    return builder;
  }

  public void read(Thing.Builder builder, IThingReadContext context, Object source, String sourcePath) 
          throws ThingReadException
  {
    this.read((Dashboard.Builder)builder, (CdfdeJsReadContext)context, (JXPathContext)source, sourcePath);
  }
  
  public void read(
          Dashboard.Builder builder,
          CdfdeJsReadContext context, 
          JXPathContext source, 
          String sourcePath)
          throws ThingReadException
  {
    builder.setMeta(DashboardType.getInstance());
    
    // 0. File path (sourcePath and filename should be the same - they may differ on non-canonicalization or solution relative vs system absolute?)
    builder.setSourcePath(sourcePath);//XPathUtils.getStringValue(source, "/filename"));

    // 1. WCDF
    builder.setWcdf(context.getWcdf());

    // 2. REGULAR
    readKind(
        builder,
        KnownThingKind.Component, 
        source, 
        source.iteratePointers("/components/rows"),
        context, 
        sourcePath);
    
    // 3. DATASOURCE
    readKind(
        builder,
        KnownThingKind.Component, 
        source, 
        source.iteratePointers("/datasources/rows"),
        context, 
        sourcePath);
    
    // 4. LAYOUT
    //JXPathContext layoutXP = source.getRelativeContext(source.getPointer("/layout"));
    {
      // HACK: 'layout' key for getting the reader
      IThingReader reader;
      try
      {
        reader = context.getFactory().getReader(KnownThingKind.Component, "layout", null);
      }
      catch(UnsupportedThingException ex)
      {
        throw new ThingReadException(ex);
      }
      
      LayoutComponent.Builder compBuilder = 
              (LayoutComponent.Builder)reader.read(context, source, sourcePath);
      
      builder.addComponent(compBuilder);
    }
  }
  
  private void readKind(
          Dashboard.Builder builder,
          String thingKind, 
          JXPathContext source, 
          Iterator<Pointer> componentPointers, 
          CdfdeJsReadContext context, 
          String sourcePath) throws ThingReadException
  {
    while(componentPointers.hasNext())
    {
      Pointer componentPointer = componentPointers.next();
      JXPathContext compXP = source.getRelativeContext(componentPointer);
      
      String className = (String)compXP.getValue("type");
      
      // Ignore label components (it's OK for current needs)
      if(className == null || !className.equalsIgnoreCase("label"))
      {
        IThingReader reader;
        try
        {
          reader = context.getFactory().getReader(thingKind, className, null);
        }
        catch (UnsupportedThingException ex)
        {
          throw new ThingReadException(ex);
        }

        Component.Builder compBuilder = 
                (Component.Builder)reader.read(context, compXP, sourcePath);

        builder.addComponent(compBuilder);
      }
    }
  }
}