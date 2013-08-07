/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta.reader.cdexml.fs;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.reader.IThingReader;
import pt.webdetails.cdf.dd.model.core.reader.IThingReaderFactory;
import pt.webdetails.cdf.dd.model.meta.*;
import pt.webdetails.cdf.dd.model.meta.reader.cdexml.XmlAdhocComponentTypeReader;
import pt.webdetails.cdf.dd.model.meta.reader.cdexml.XmlPropertyTypeReader;

/**
 * @author dcleao
 */
public class XmlFsPluginThingReaderFactory implements IThingReaderFactory
{
  private Map<String, IThingReader> _metaReadersByClassName;
  
  public XmlFsPluginThingReaderFactory()
  {
    this._metaReadersByClassName = new HashMap<String, IThingReader>();
    
    this.register(PrimitiveComponentType.class, PrimitiveComponentType.Builder.class);
    this.register(CustomComponentType.class,    CustomComponentType.Builder.class);
    this.register(WidgetComponentType.class,    WidgetComponentType.Builder.class);
    this.register(DashboardType.class,          DashboardType.Builder.class);
    this.register(CodeComponentType.class,      CodeComponentType.Builder.class);
    this.register(ParameterComponentType.class, ParameterComponentType.Builder.class);
    this.register(DataSourceComponentType.class, DataSourceComponentType.Builder.class);
  }
  
  private <TR extends MetaObject, TB extends MetaObject.Builder> 
          void register(Class<TR> klass, Class<TB> readerClass)
  {
    this._metaReadersByClassName.put(
            klass.getSimpleName(), 
            new XmlAdhocComponentTypeReader(readerClass));
  }
  
  public IThingReader getReader(String kind, String className, String name) throws UnsupportedThingException
  {
    if(KnownThingKind.ComponentType.equals(kind))
    {
      IThingReader reader = this._metaReadersByClassName.get(className);
      if(reader != null)
      {
        return reader;
      }
    }
    else if(KnownThingKind.PropertyType.equals(kind))
    {
      if(StringUtils.isEmpty(className) ||
         "PropertyType".equals(className)) { return new XmlPropertyTypeReader(); }
    }
    else if(KnownThingKind.MetaModel.equals(kind))
    {
      return new XmlFsPluginModelReader(false);
    }

    throw new UnsupportedThingException(kind, className);
  }
}