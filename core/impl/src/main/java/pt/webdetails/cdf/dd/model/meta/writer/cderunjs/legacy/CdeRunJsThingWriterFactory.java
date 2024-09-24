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

package pt.webdetails.cdf.dd.model.meta.writer.cderunjs.legacy;

import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.meta.writer.cderunjs.CdeRunJsPropertyTypeWriter;

public class CdeRunJsThingWriterFactory implements IThingWriterFactory {
  public IThingWriter getWriter( Thing t ) throws UnsupportedThingException {
    if ( t == null ) {
      throw new IllegalArgumentException( "t" );
    }

    String kind = t.getKind();

    if ( KnownThingKind.ComponentType.equals( kind ) ) {
      return new CdeRunJsComponentTypeWriter();
    }

    if ( KnownThingKind.PropertyType.equals( kind ) ) {
      return new CdeRunJsPropertyTypeWriter();
    }

    if ( KnownThingKind.MetaModel.equals( kind ) ) {
      return new CdeRunJsModelWriter();
    }

    throw new UnsupportedThingException( kind, t.getId() );
  }
}
