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


package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties;

import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.PropertyBinding;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;

public class CdfRunJsPropertyBindingWriterForTests extends CdfRunJsPropertyBindingWriter {

  public static final int TYPE_STRING = 0;
  public static final int TYPE_NUMBER = 1;
  public static final int TYPE_BOOLEAN = 2;
  public static final int TYPE_LITERAL = 3;
  public static final int TYPE_ARRAY = 4;
  public static final int TYPE_FUNCTION = 5;
  private int type;

  public CdfRunJsPropertyBindingWriterForTests() {
    type = TYPE_STRING;
  }

  @Override
  public void write( StringBuilder out, CdfRunJsDashboardWriteContext context, PropertyBinding propBind )
    throws ThingWriteException {
    String canonicalValue = propBind.getValue();
    switch ( type ) {
      case TYPE_STRING:
        out.append( writeString( canonicalValue ) );
        break;
      case TYPE_NUMBER:
        out.append( writeNumber( canonicalValue ) );
        break;
      case TYPE_BOOLEAN:
        out.append( writeBoolean( canonicalValue ) );
        break;
      case TYPE_LITERAL:
        out.append( writeLiteral( canonicalValue ) );
        break;
      case TYPE_ARRAY:
        out.append( writeArray( canonicalValue ) );
        break;
      case TYPE_FUNCTION:
        out.append( writeFunction( canonicalValue ) );
        break;
    }
  }

  public void setType( int type ) {
    this.type = type;
  }

}
