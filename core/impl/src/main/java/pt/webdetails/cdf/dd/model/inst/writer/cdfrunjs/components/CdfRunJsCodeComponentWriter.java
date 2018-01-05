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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components;

import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.model.inst.CodeComponent;

import static pt.webdetails.cdf.dd.CdeConstants.Writer.*;

public class CdfRunJsCodeComponentWriter extends JsWriterAbstract implements IThingWriter {
  public void write( Object output, IThingWriteContext context, Thing t ) throws ThingWriteException {
    this.write( (StringBuilder) output, (IThingWriteContext) context, (CodeComponent) t );
  }

  public void write( StringBuilder out, IThingWriteContext context, CodeComponent comp ) throws ThingWriteException {
    out.append( comp.tryGetPropertyValue( "javaScript", "" ) ).append( NEWLINE );
  }
}
