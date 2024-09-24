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

package pt.webdetails.cdf.dd.model.meta.reader.cdexml.fs;

import java.util.HashMap;
import java.util.Map;

import pt.webdetails.cdf.dd.model.meta.CodeComponentType;
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.meta.CustomComponentType;
import pt.webdetails.cdf.dd.model.meta.DataSourceComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaObject;
import pt.webdetails.cdf.dd.model.meta.ParameterComponentType;
import pt.webdetails.cdf.dd.model.meta.PrimitiveComponentType;
import pt.webdetails.cdf.dd.model.meta.WidgetComponentType;
import pt.webdetails.cdf.dd.model.meta.reader.cdexml.XmlAdhocComponentTypeReader;
import pt.webdetails.cdf.dd.model.meta.reader.cdexml.XmlPropertyTypeReader;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;

/**
 * Factory for XmlFsPluginModelReader, XmlPropertyTypeReader and ComponentType readers. (and DashboardType?)
 */
public class XmlFsPluginThingReaderFactory {
  private Map<String, XmlAdhocComponentTypeReader<? extends ComponentType.Builder>> _metaReadersByClassName;

  private IContentAccessFactory contentAccessFactory;

  public XmlFsPluginThingReaderFactory( IContentAccessFactory factory ) {
    contentAccessFactory = factory;
    this._metaReadersByClassName = new HashMap<String, XmlAdhocComponentTypeReader<? extends ComponentType.Builder>>();

    this.register( PrimitiveComponentType.class, PrimitiveComponentType.Builder.class );
    this.register( CustomComponentType.class, CustomComponentType.Builder.class );
    this.register( WidgetComponentType.class, WidgetComponentType.Builder.class );

    //    //XXX this is only called for ComponentType; is DashboardType a ComponentType? should use thingyKind
    // .DashboardType...
    //    this.register(DashboardType.class,          DashboardType.Builder.class);

    this.register( CodeComponentType.class, CodeComponentType.Builder.class );
    this.register( ParameterComponentType.class, ParameterComponentType.Builder.class );
    this.register( DataSourceComponentType.class, DataSourceComponentType.Builder.class );
  }

  private <TR extends MetaObject, TB extends ComponentType.Builder> void register( Class<TR> klass,
                                                                                   Class<TB> readerClass ) {
    this._metaReadersByClassName.put(
      klass.getSimpleName(),
      new XmlAdhocComponentTypeReader<TB>( readerClass, this ) );
  }

  public XmlFsPluginModelReader getMetaModelReader() {
    return new XmlFsPluginModelReader( contentAccessFactory, false );
  }

  public XmlPropertyTypeReader getPropertyTypeReader() {
    return new XmlPropertyTypeReader();
  }

  //TODO: use new iface
  public XmlAdhocComponentTypeReader<? extends ComponentType.Builder> getComponentTypeReader( String className ) {
    return this._metaReadersByClassName.get( className );
  }
}
