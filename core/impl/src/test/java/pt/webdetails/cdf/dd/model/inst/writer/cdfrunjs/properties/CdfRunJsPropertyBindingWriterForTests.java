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
