/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta.writer.xml;

import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;

/**
 * @author dcleao
 */
public class XmlThingWriterFactory implements IThingWriterFactory
{
  public IThingWriter getWriter(Thing t) throws UnsupportedThingException
  {
    if(t == null) { throw new IllegalArgumentException("t"); }

    String kind = t.getKind();

    if(KnownThingKind.ComponentType.equals(kind))
    {
      return new XmlComponentTypeWriter();
    }
    else if(KnownThingKind.PropertyType.equals(kind))
    {
      return new XmlPropertyTypeWriter();
    }
//    else if(KnownThingKind.MetaModel.equals(kind))
//    {
//      return new JsModelWriter();
//    }

    throw new UnsupportedThingException(kind, t.getId());
  }
}
