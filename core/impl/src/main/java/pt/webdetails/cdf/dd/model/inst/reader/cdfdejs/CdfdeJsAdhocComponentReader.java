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

package pt.webdetails.cdf.dd.model.inst.reader.cdfdejs;

import org.apache.commons.jxpath.JXPathContext;
import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.meta.ComponentType;

public class CdfdeJsAdhocComponentReader<TM extends Component.Builder> extends CdfdeJsComponentReader<TM> {
  private final Class<TM> _class;
  private final ComponentType _compType;
  public CdfdeJsAdhocComponentReader( Class<TM> pclass, ComponentType compType ) {
    if ( pclass == null ) {
      throw new IllegalArgumentException( "pclass" );
    }
    if ( compType == null ) {
      throw new IllegalArgumentException( "compType" );
    }

    this._class    = pclass;
    this._compType = compType;
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public TM read( IThingReadContext context, Object source, String sourcePath ) throws ThingReadException {
    TM builder = this.createInstance();
    builder.setMeta( this._compType );

    this.read( builder, context, (JXPathContext) source, sourcePath );

    return builder;
  }

  private TM createInstance() throws ThingReadException {
    try {
      return _class.newInstance();
    } catch ( InstantiationException ex ) {
      throw new ThingReadException( ex );
    } catch ( IllegalAccessException ex ) {
      throw new ThingReadException( ex );
    }
  }
}
