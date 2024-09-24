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
