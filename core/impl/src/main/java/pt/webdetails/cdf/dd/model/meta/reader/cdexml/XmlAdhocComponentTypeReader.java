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

package pt.webdetails.cdf.dd.model.meta.reader.cdexml;

import org.dom4j.Element;

import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.meta.reader.cdexml.fs.XmlFsPluginThingReaderFactory;
import pt.webdetails.cpf.packager.origin.PathOrigin;

/**
 * Casts arguments to fit XmlComponentTypeReader and instantiates builders with empty ctors
 */
public final class XmlAdhocComponentTypeReader<TB extends ComponentType.Builder>
  extends XmlComponentTypeReader {
  private final Class<TB> _class;
  private XmlFsPluginThingReaderFactory factory;

  public XmlAdhocComponentTypeReader( Class<TB> pclass, XmlFsPluginThingReaderFactory factory ) {
    assert pclass != null;
    _class = pclass;
    this.factory = factory;
  }

  private TB createInstance() throws ThingReadException {
    try {
      return _class.newInstance();
    } catch ( InstantiationException ex ) {
      throw new ThingReadException( ex );
    } catch ( IllegalAccessException ex ) {
      throw new ThingReadException( ex );
    }
  }

  public TB read( Element source, PathOrigin origin, String sourcePath ) throws ThingReadException {
    TB builder = createInstance();
    builder.setOrigin( origin );
    this.read( builder, factory, source, sourcePath );
    return builder;
  }
}
