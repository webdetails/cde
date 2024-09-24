/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cdf.dd.model.inst.writer.cggrunjs;

import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.GenericComponent;
import pt.webdetails.cdf.dd.model.inst.WidgetComponent;

public class CggRunJsThingWriterFactory implements IThingWriterFactory {
  public IThingWriter getWriter( Thing t ) throws UnsupportedThingException {
    if ( t == null ) {
      throw new IllegalArgumentException( "t" );
    }

    String kind = t.getKind();

    if ( KnownThingKind.Component.equals( kind ) ) {
      if ( ( t instanceof GenericComponent ) && !( t instanceof WidgetComponent ) ) {
        GenericComponent comp = (GenericComponent) t;
        if ( comp.getMeta().tryGetAttributeValue( "cdwSupport", "false" ).equalsIgnoreCase( "true" ) ) {
          boolean canWrite = true;
          if ( getId( comp ).equals( "cggDial" ) ) {
            canWrite = false;
          }

          return new CggRunJsGenericComponentWriter( canWrite );
        }
      } else if ( t instanceof DataSourceComponent ) {
        return new CggRunJsDataSourceComponentWriter();
      }
    } else if ( KnownThingKind.Dashboard.equals( kind ) ) {
      return new CggRunJsDashboardWriter();
    }

    throw new UnsupportedThingException( kind, t.getId() );
  }

  protected String getId( GenericComponent comp ) {
    return comp.getMeta().getId();
  }
}
