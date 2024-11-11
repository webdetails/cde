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


package pt.webdetails.cdf.dd.model.inst.writer.cggrunjs;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.GenericComponent;
import pt.webdetails.cdf.dd.model.inst.WidgetComponent;
import pt.webdetails.cpf.repository.api.IRWAccess;

public class CggRunJsDashboardWriter implements IThingWriter {
  public void write( Object output, IThingWriteContext context, Thing t ) throws ThingWriteException {
    this.write( (IRWAccess) output, (CggRunJsDashboardWriteContext) context, (Dashboard) t );
  }

  public void write( IRWAccess access, CggRunJsDashboardWriteContext context, Dashboard dash )
    throws ThingWriteException {
    assert context.getDashboard() == dash;

    IThingWriterFactory factory = context.getFactory();
    Iterable<Component> comps = dash.getRegulars();
    for ( Component comp : comps ) {
      if ( StringUtils.isNotEmpty( comp.getName() )
          && ( comp instanceof GenericComponent )
          && !( comp instanceof WidgetComponent ) ) {
        GenericComponent genComp = (GenericComponent) comp;
        if ( genComp.getMeta().tryGetAttributeValue( "cdwSupport", "false" ).equalsIgnoreCase( "true" )
            && genComp.tryGetAttributeValue( "cdwRender", "false" ).equalsIgnoreCase( "true" ) ) {
          IThingWriter writer;
          try {
            writer = factory.getWriter( genComp );
          } catch ( UnsupportedThingException ex ) {
            throw new ThingWriteException( ex );
          }

          writer.write( access, context, comp );
        }
      }
    }
  }
}
