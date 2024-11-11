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
