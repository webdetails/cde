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


package pt.webdetails.cdf.dd.model.inst.reader.cdfdejs;

import org.apache.commons.jxpath.JXPathContext;

import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.IThingReader;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.inst.LayoutComponent;
import pt.webdetails.cdf.dd.model.inst.UnresolvedPropertyBinding;
import pt.webdetails.cdf.dd.model.meta.LayoutComponentType;

public class CdfdeJsLayoutComponentReader implements IThingReader {
  private final LayoutComponentType _layoutCompType;

  public CdfdeJsLayoutComponentReader( LayoutComponentType layoutCompType ) {
    if ( layoutCompType == null ) {
      throw new IllegalArgumentException( "layoutCompType" );
    }
    this._layoutCompType = layoutCompType;
  }

  public LayoutComponent.Builder read( IThingReadContext context, java.lang.Object source, String sourcePath )
          throws ThingReadException {
    LayoutComponent.Builder builder = new LayoutComponent.Builder();
    read( builder, context, (JXPathContext) source, sourcePath );
    return builder;
  }

  public void read( Thing.Builder builder, IThingReadContext context, java.lang.Object source, String sourcePath )
      throws ThingReadException {
    read( (LayoutComponent.Builder) builder, context, (JXPathContext) source, sourcePath );
  }

  public void read( LayoutComponent.Builder builder, IThingReadContext context, JXPathContext source,
                    String sourcePath ) {
    builder.setMeta( this._layoutCompType );

    // Add a name
    builder.addPropertyBinding(
              new UnresolvedPropertyBinding.Builder()
                .setAlias( "name" )
                .setValue( "TODO" ) );

    builder.setLayoutXPContext( source );
  }
}
