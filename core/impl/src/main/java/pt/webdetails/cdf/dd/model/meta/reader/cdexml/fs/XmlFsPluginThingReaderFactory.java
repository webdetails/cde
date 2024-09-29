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
