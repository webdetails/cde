/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.reader.cdfdejs;

import org.apache.commons.jxpath.JXPathContext;
import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.meta.ComponentType;

/**
 * @author dcleao
 */
public class CdfdeJsAdhocComponentReader<TM extends Component.Builder> extends CdfdeJsComponentReader<TM>
{
  private final Class<TM> _class;
  private final ComponentType _compType;
  public CdfdeJsAdhocComponentReader(Class<TM> pclass, ComponentType compType)
  {
    if(pclass == null) { throw new IllegalArgumentException("pclass"); }
    if(compType == null) { throw new IllegalArgumentException("compType"); }
    
    this._class    = pclass;
    this._compType = compType;
  }

  @Override
  public TM read(IThingReadContext context, Object source, String sourcePath) throws ThingReadException
  {
    TM builder = this.createInstance();
    builder.setMeta(this._compType);
    
    this.read(builder, context, (JXPathContext)source, sourcePath);
    
    return builder;
  }
  
  private TM createInstance() throws ThingReadException
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
}