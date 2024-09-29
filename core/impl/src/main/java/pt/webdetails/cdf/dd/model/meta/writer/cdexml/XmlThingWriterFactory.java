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


package pt.webdetails.cdf.dd.model.meta.writer.cdexml;

import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;

public class XmlThingWriterFactory implements IThingWriterFactory {
  public IThingWriter getWriter( Thing t ) throws UnsupportedThingException {
    if ( t == null ) {
      throw new IllegalArgumentException( "t" );
    }

    String kind = t.getKind();

    if ( KnownThingKind.ComponentType.equals( kind ) ) {
      return new XmlComponentTypeWriter();
    } else if ( KnownThingKind.PropertyType.equals( kind ) ) {
      return new XmlPropertyTypeWriter();
    }
    //    else if(KnownThingKind.MetaModel.equals(kind))
    //    {
    //      return new JsModelWriter();
    //    }

    throw new UnsupportedThingException( kind, t.getId() );
  }
}
