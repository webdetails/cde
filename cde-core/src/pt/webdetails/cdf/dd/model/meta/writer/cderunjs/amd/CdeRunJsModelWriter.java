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

package pt.webdetails.cdf.dd.model.meta.writer.cderunjs.amd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.PropertyType;

public class CdeRunJsModelWriter extends JsWriterAbstract implements IThingWriter {
  protected static final Log _logger = LogFactory.getLog( CdeRunJsModelWriter.class );

  public void write( Object output, IThingWriteContext context, Thing t )
    throws ThingWriteException {
    MetaModel model = (MetaModel) t;
    StringBuilder out = (StringBuilder) output;

    IThingWriterFactory factory = context.getFactory();
    assert factory != null;

    // GLOBAL PROPERTIES
    for ( PropertyType prop : model.getPropertyTypes() ) {
      IThingWriter propWriter;
      try {
        propWriter = factory.getWriter( prop );
        assert propWriter != null;
      } catch ( UnsupportedThingException ex ) {
        ThingWriteException ex2 = new ThingWriteException( ex );
        if ( context.getBreakOnError() ) {
          throw ex2;
        }
        _logger.error( ex2 );
        continue;
      }

      propWriter.write( out, context, prop );
    }

    // COMPONENTS
    // Components output their own properties
    for ( ComponentType comp : model.getComponentTypes() ) {
      // check if component can be used in AMD dashboards
      if ( !comp.supportsAMD() ) {
        continue;
      }
      IThingWriter compWriter;
      try {
        compWriter = factory.getWriter( comp );
        assert compWriter != null;
      } catch ( UnsupportedThingException ex ) {
        ThingWriteException ex2 = new ThingWriteException( ex );
        if ( context.getBreakOnError() ) {
          throw ex2;
        }
        _logger.error( ex2 );
        continue;
      }

      compWriter.write( out, context, comp );
    }
  }
}
