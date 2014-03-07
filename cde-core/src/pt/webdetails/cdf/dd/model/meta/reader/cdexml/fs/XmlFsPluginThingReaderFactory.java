/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta.reader.cdexml.fs;

import java.util.HashMap;
import java.util.Map;
import pt.webdetails.cdf.dd.model.meta.*;
import pt.webdetails.cdf.dd.model.meta.reader.cdexml.XmlAdhocComponentTypeReader;
import pt.webdetails.cdf.dd.model.meta.reader.cdexml.XmlPropertyTypeReader;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;

/**
 * Factory for XmlFsPluginModelReader, XmlPropertyTypeReader and ComponentType readers. (and DashboardType?)
 * @author dcleao
 */
public class XmlFsPluginThingReaderFactory
{
  private Map<String, XmlAdhocComponentTypeReader<? extends ComponentType.Builder>> _metaReadersByClassName;
  
  private IContentAccessFactory contentAccessFactory;
  
  public XmlFsPluginThingReaderFactory(IContentAccessFactory factory)
  {
    contentAccessFactory = factory;
    this._metaReadersByClassName = new HashMap<String, XmlAdhocComponentTypeReader<? extends ComponentType.Builder>>();
    
    this.register(PrimitiveComponentType.class, PrimitiveComponentType.Builder.class);
    this.register(CustomComponentType.class,    CustomComponentType.Builder.class);
    this.register(WidgetComponentType.class,    WidgetComponentType.Builder.class);

//    //XXX this is only called for ComponentType; is DashboardType a ComponentType? should use thingyKind.DashboardType...
//    this.register(DashboardType.class,          DashboardType.Builder.class);

    this.register(CodeComponentType.class,      CodeComponentType.Builder.class);
    this.register(ParameterComponentType.class, ParameterComponentType.Builder.class);
    this.register(DataSourceComponentType.class, DataSourceComponentType.Builder.class);
  }
  
  private <TR extends MetaObject, TB extends ComponentType.Builder>
          void register(Class<TR> klass, Class<TB> readerClass)
  {
    this._metaReadersByClassName.put(
            klass.getSimpleName(), 
            new XmlAdhocComponentTypeReader<TB>(readerClass, this));
  }
  
  public XmlFsPluginModelReader getMetaModelReader() {
    return new XmlFsPluginModelReader(contentAccessFactory, false);
  }
  public XmlPropertyTypeReader getPropertyTypeReader() {
    return new XmlPropertyTypeReader();
  }

  //TODO: use new iface
  public XmlAdhocComponentTypeReader<? extends ComponentType.Builder> getComponentTypeReader (String className) {
    return this._metaReadersByClassName.get(className);
  }

}