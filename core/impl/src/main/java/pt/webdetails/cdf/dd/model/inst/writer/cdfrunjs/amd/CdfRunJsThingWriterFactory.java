/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.amd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.CodeComponent;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.GenericComponent;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;
import pt.webdetails.cdf.dd.model.inst.WidgetComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.CdfRunJsCodeComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd.CdfRunJsDataSourceComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd.CdfRunJsDateParameterComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd.CdfRunJsExpressionParameterComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd.CdfRunJsGenericComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd.CdfRunJsParameterComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.amd.CdfRunJsDashboardModuleWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.amd.CdfRunJsDashboardWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties.CdfRunJsGenericPropertyBindingWriter;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor.DashboardRendererType;

public class CdfRunJsThingWriterFactory implements IThingWriterFactory {
  protected static final Log logger = LogFactory.getLog( CdfRunJsThingWriterFactory.class );

  @Override
  public IThingWriter getWriter( Thing t ) throws UnsupportedThingException {
    if ( t == null ) {
      throw new IllegalArgumentException( "t" );
    }

    String kind = t.getKind();
    if ( KnownThingKind.Component.equals( kind ) ) {
      return getComponentWriter( t );

    } else if ( KnownThingKind.PropertyBinding.equals( kind ) ) {
      return new CdfRunJsGenericPropertyBindingWriter();

    } else if ( KnownThingKind.Dashboard.equals( kind ) ) { // shouldn't get here anymore
      return getDashboardWriter( ( (Dashboard) t ) );

    }

    throw new UnsupportedThingException( kind, t.getId() );
  }

  /**
   * @param dashboard the dashboard
   * @return an instance of a dashboard writer of the same render type as the provided dashboard
   */
  public CdfRunJsDashboardWriter getDashboardWriter( Dashboard dashboard ) {
    DashboardWcdfDescriptor wcdf = dashboard.getWcdf();
    DashboardRendererType rendererType = wcdf.getParsedRendererType();
    return new CdfRunJsDashboardWriter( rendererType );
  }

  public CdfRunJsDashboardModuleWriter getDashboardModuleWriter( Dashboard dashboard ) {
    DashboardWcdfDescriptor wcdf = dashboard.getWcdf();
    DashboardRendererType rendererType = wcdf.getParsedRendererType();
    return new CdfRunJsDashboardModuleWriter( rendererType );
  }

  private IThingWriter getComponentWriter( Thing t ) throws UnsupportedThingException {
    Class compClass = t.getClass();

    if ( GenericComponent.class.isAssignableFrom( compClass ) ) {
      if ( WidgetComponent.class.isAssignableFrom( compClass ) ) {
        logger.error( "Widget component is no longer supported" );
        throw new UnsupportedThingException( t.getKind(), t.getId() );
      }

      return new CdfRunJsGenericComponentWriter();
    }

    if ( ParameterComponent.class.isAssignableFrom( compClass ) ) {
      return getParameterWriter( t );
    }

    if ( CodeComponent.class.isAssignableFrom( compClass ) ) {
      return new CdfRunJsCodeComponentWriter();
    }

    if ( DataSourceComponent.class.isAssignableFrom( compClass ) ) {
      return new CdfRunJsDataSourceComponentWriter();
    }

    return null;
  }

  private IThingWriter getParameterWriter( Thing t ) {
    ParameterComponent paramComp = (ParameterComponent) t;
    String typeName = paramComp.getMeta().getName().toLowerCase();

    IThingWriter parameterWriter;
    switch ( typeName ) {
      case SIMPLE_PARAMETER:
      case OLAP_PARAMETER:
        parameterWriter = new CdfRunJsParameterComponentWriter();
        break;
      case DATE_PARAMETER:
        parameterWriter = new CdfRunJsDateParameterComponentWriter();
        break;
      case JS_EXPRESSION_PARAMETER:
        parameterWriter = new CdfRunJsExpressionParameterComponentWriter();
        break;
      default:
        parameterWriter = null;
    }

    return parameterWriter;
  }
}
