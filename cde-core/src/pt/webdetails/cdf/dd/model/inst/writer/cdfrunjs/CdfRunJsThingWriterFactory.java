/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs;

import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.*;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties.*;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.*;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.CodeComponent;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.GenericComponent;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;
import pt.webdetails.cdf.dd.model.inst.PropertyBinding;
import pt.webdetails.cdf.dd.model.inst.WidgetComponent;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor.DashboardRendererType;

/**
 * @author dcleao
 */
public class CdfRunJsThingWriterFactory implements IThingWriterFactory
{
  
  public CdfRunJsDashboardWriter getDashboardWriter(Dashboard dashboard) {
    DashboardWcdfDescriptor wcdf = dashboard.getWcdf();
    DashboardRendererType rendererType = wcdf.getParsedRendererType();
      
    if(rendererType == DashboardRendererType.MOBILE)
    {
      return new CdfRunJsMobileDashboardWriter();
    }
    
    if(rendererType == DashboardRendererType.BOOTSTRAP)
      {
    	  return wcdf.isWidget() ?
    	             new CdfRunJsBootstrapWidgetWriter() :
    	             new CdfRunJsBootstrapDashboardWriter();
      }    
    

    return wcdf.isWidget() ?
           new CdfRunJsBlueprintWidgetWriter() :
           new CdfRunJsBlueprintDashboardWriter();
  }

  public IThingWriter getWriter(Thing t) throws UnsupportedThingException
  {
    if(t == null) { throw new IllegalArgumentException("t"); }

    String kind = t.getKind();
    
    if(KnownThingKind.Component.equals(kind))
    {
      Class compClass = t.getClass();
      
      if(GenericComponent.class.isAssignableFrom(compClass))
      {
        if(WidgetComponent.class.isAssignableFrom(compClass))
        {
          return new CdfRunJsWidgetComponentWriter();
        }
        
        return new CdfRunJsGenericComponentWriter();
      }
      
      if(ParameterComponent.class.isAssignableFrom(compClass))
      {
        ParameterComponent paramComp = (ParameterComponent)t;
        String typeName = paramComp.getMeta().getName().toLowerCase();
        if(typeName.equals("parameter"          ) ||
           typeName.equals("olapparameter"      )) { return new CdfRunJsParameterComponentWriter();           }
        if(typeName.equals("dateparameter"      )) { return new CdfRunJsDateParameterComponentWriter();       }
        if(typeName.equals("javascriptparameter")) { return new CdfRunJsExpressionParameterComponentWriter(); }
      }
      
      if(CodeComponent.class.isAssignableFrom(compClass))
      {
        return new CdfRunJsCodeComponentWriter();
      }
    } 
    else if(KnownThingKind.PropertyBinding.equals(kind)) 
    {
      PropertyBinding propBind = (PropertyBinding)t;
      String propName = propBind.getName().toLowerCase();
      
      if(propName.equals("datasource"          )) { return new CdfRunJsDataSourcePropertyBindingWriter(); }
      if(propName.equals("cdadatasource"       )) { return new CdfRunJsCdaDataSourcePropertyBindingWriter(); }
      if(propName.equals("jfreechartdatasource")) { return new CdfRunJsJFreeChartDataSourcePropertyBindingWriter(); }
      
      return new CdfRunJsGenericPropertyBindingWriter();
    }
    else if(KnownThingKind.Dashboard.equals(kind))

    { // shouldn't get here anymore
      return getDashboardWriter(((Dashboard)t));
    }

    throw new UnsupportedThingException(kind, t.getId());
  }
}
