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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.legacy;

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
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.CdfRunJsCodeComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.legacy.CdfRunJsDateParameterComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.legacy.CdfRunJsExpressionParameterComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.legacy.CdfRunJsGenericComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.legacy.CdfRunJsParameterComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.legacy.CdfRunJsWidgetComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy.CdfRunJsDashboardWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties.CdfRunJsCdaDataSourcePropertyBindingWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties.CdfRunJsDataSourcePropertyBindingWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties.CdfRunJsGenericPropertyBindingWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties.CdfRunJsJFreeChartDataSourcePropertyBindingWriter;

public class CdfRunJsThingWriterFactory implements IThingWriterFactory {

  public CdfRunJsDashboardWriter getDashboardWriter( Dashboard dashboard ) {
    return new CdfRunJsDashboardWriter( dashboard.getWcdf().getParsedRendererType() );
  }

  public IThingWriter getWriter( Thing t ) throws UnsupportedThingException {
    if ( t == null ) {
      throw new IllegalArgumentException( "t" );
    }

    String kind = t.getKind();

    if ( KnownThingKind.Component.equals( kind ) ) {
      Class compClass = t.getClass();

      if ( GenericComponent.class.isAssignableFrom( compClass ) ) {
        if ( WidgetComponent.class.isAssignableFrom( compClass ) ) {
          return new CdfRunJsWidgetComponentWriter();
        }

        return new CdfRunJsGenericComponentWriter();
      }

      if ( ParameterComponent.class.isAssignableFrom( compClass ) ) {
        ParameterComponent paramComp = (ParameterComponent) t;
        String typeName = paramComp.getMeta().getName().toLowerCase();
        if ( typeName.equals( "parameter" ) || typeName.equals( "olapparameter" ) ) {
          return new CdfRunJsParameterComponentWriter();
        }
        if ( typeName.equals( "dateparameter" ) ) {
          return new CdfRunJsDateParameterComponentWriter();
        }
        if ( typeName.equals( "javascriptparameter" ) ) {
          return new CdfRunJsExpressionParameterComponentWriter();
        }
      }

      if ( CodeComponent.class.isAssignableFrom( compClass ) ) {
        return new CdfRunJsCodeComponentWriter();
      }
    } else if ( KnownThingKind.PropertyBinding.equals( kind ) ) {
      PropertyBinding propBind = (PropertyBinding) t;
      String propName = propBind.getName().toLowerCase();

      if ( propName.equals( "datasource" ) ) {
        return new CdfRunJsDataSourcePropertyBindingWriter();
      }
      if ( propName.equals( "cdadatasource" ) ) {
        return new CdfRunJsCdaDataSourcePropertyBindingWriter();
      }
      if ( propName.equals( "jfreechartdatasource" ) ) {
        return new CdfRunJsJFreeChartDataSourcePropertyBindingWriter();
      }

      return new CdfRunJsGenericPropertyBindingWriter();
    } else if ( KnownThingKind.Dashboard.equals( kind ) ) { // shouldn't get here anymore
      return getDashboardWriter( ( (Dashboard) t ) );
    }

    throw new UnsupportedThingException( kind, t.getId() );
  }
}
