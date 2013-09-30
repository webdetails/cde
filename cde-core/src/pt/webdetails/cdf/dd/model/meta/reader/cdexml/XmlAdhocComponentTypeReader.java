/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta.reader.cdexml;

import org.dom4j.Element;

import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.meta.reader.cdexml.fs.XmlFsPluginThingReaderFactory;
import pt.webdetails.cdf.dd.packager.PathOrigin;

/**
 * Casts arguments to fit XmlComponentTypeReader and instantiates builders with empty ctors
 * @author dcleao
 */
public final class XmlAdhocComponentTypeReader<TB extends ComponentType.Builder> 
    extends XmlComponentTypeReader
{
  private final Class<TB> _class;
  private XmlFsPluginThingReaderFactory factory;
  
  public XmlAdhocComponentTypeReader(Class<TB> pclass, XmlFsPluginThingReaderFactory factory)
  {
    assert pclass != null;
    _class = pclass;
    this.factory = factory;
  }

  private TB createInstance() throws ThingReadException
  {
    try
    {
      return _class.newInstance();
    }
    catch (InstantiationException ex)
    {
      throw new ThingReadException(ex);
    }
    catch (IllegalAccessException ex)
    {
      throw new ThingReadException(ex);
    }
  }

  public TB read(Element source, PathOrigin origin, String sourcePath) throws ThingReadException {
    TB builder = createInstance();
    builder.setOrigin(origin);
    this.read(builder, factory, source, sourcePath);
    return builder;
  }
}
