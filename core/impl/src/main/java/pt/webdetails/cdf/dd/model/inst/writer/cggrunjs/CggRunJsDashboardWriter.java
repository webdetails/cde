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
