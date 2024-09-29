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

import java.util.Iterator;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.IThingReader;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.LayoutComponent;
import pt.webdetails.cdf.dd.model.meta.DashboardType;

public class CdfdeJsDashboardReader implements IThingReader {
  private static final Log logger = LogFactory.getLog( CdfdeJsDashboardReader.class );

  public Dashboard.Builder read( IThingReadContext context, Object source, String sourcePath )
    throws ThingReadException {
    Dashboard.Builder builder = new Dashboard.Builder();
    this.read( builder, context, source, sourcePath );

    return builder;
  }

  public void read( Thing.Builder builder, IThingReadContext context, Object source, String sourcePath )
    throws ThingReadException {
    this.read( (Dashboard.Builder) builder, (CdfdeJsReadContext) context, (JXPathContext) source, sourcePath );
  }

  public void read( Dashboard.Builder builder, CdfdeJsReadContext context, JXPathContext source, String sourcePath )
    throws ThingReadException {
    builder.setMeta( DashboardType.getInstance() );

    // 0. File path (sourcePath and filename should be the same - they may differ on non-canonicalization or solution
    // relative vs system absolute?)
    builder.setSourcePath( sourcePath ); //XPathUtils.getStringValue(source, "/filename"));

    // 1. WCDF
    builder.setWcdf( context.getWcdf() );

    // 2. REGULARS
    readRegularComponents( builder, source, context );

    // 3. DATASOURCES
    readDataSourceComponents( builder, source, context );

    // 4. LAYOUT
    readLayoutComponent( builder, source, context );
  }

  private void readKind( Dashboard.Builder builder, String thingKind, JXPathContext source,
                         Iterator<Pointer> componentPointers, CdfdeJsReadContext context ) throws ThingReadException {
    while ( componentPointers.hasNext() ) {
      Pointer componentPointer = componentPointers.next();
      JXPathContext compXP = source.getRelativeContext( componentPointer );

      String className = (String) compXP.getValue( "type" );

      // Ignore label components (it's OK for current needs)
      if ( className == null || !className.equalsIgnoreCase( "label" ) ) {
        IThingReader reader;
        try {
          reader = context.getFactory().getReader( thingKind, className, null );
        } catch ( UnsupportedThingException ex ) {
          logger.error( "While rendering dashboard. " + ex );
          continue;
        }

        String sourcePath = builder.getSourcePath();
        Component.Builder compBuilder = (Component.Builder) reader.read( context, compXP, sourcePath );

        builder.addComponent( compBuilder );
      }
    }
  }

  private void readRegularComponents( Dashboard.Builder builder, JXPathContext source,
                                      CdfdeJsReadContext context ) throws ThingReadException {
    Iterator<Pointer> regularComponents = source.iteratePointers( "/components/rows" );

    readKind( builder, KnownThingKind.Component, source, regularComponents, context );
  }

  private void readDataSourceComponents( Dashboard.Builder builder, JXPathContext source,
                                         CdfdeJsReadContext context ) throws ThingReadException {
    Iterator<Pointer> datasourceComponents = source.iteratePointers( "/datasources/rows" );

    readKind( builder, KnownThingKind.Component, source, datasourceComponents, context );

  }

  private void readLayoutComponent( Dashboard.Builder builder, JXPathContext source,
                                    CdfdeJsReadContext context ) throws ThingReadException {
    //JXPathContext layoutXP = source.getRelativeContext(source.getPointer("/layout"));
    // HACK: 'layout' key for getting the reader
    IThingReader reader;
    try {
      reader = context.getFactory().getReader( KnownThingKind.Component, "layout", null );

      // TODO: HACK: Until layout is handled the right way, we need to detect a null reader,
      // returned when there is an error building the layout inside the factory :-(
      if ( reader == null ) {
        return;
      }
    } catch ( UnsupportedThingException ex ) {
      logger.error( "While rendering dashboard. " + ex );
      return;
    }

    String sourcePath = builder.getSourcePath();
    LayoutComponent.Builder compBuilder = (LayoutComponent.Builder) reader.read( context, source, sourcePath );

    builder.addComponent( compBuilder );
  }
}
