/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.reader.cdfdejs;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;

import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.IThingReader;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.UnresolvedPropertyBinding;

/**
 * Reads the CDFDE JavaScript format of a component.
 * 
 * @author dcleao
 */
public abstract class CdfdeJsComponentReader<TM extends Component.Builder> implements IThingReader
{
  public abstract TM read(IThingReadContext context, java.lang.Object source, String sourcePath)
          throws ThingReadException;
//  {
//    Component.Builder builder = new Component.Builder();
//    read(builder, context, (JXPathContext)source, sourcePath);
//    return builder;
//  }
  
  public void read(Thing.Builder builder, IThingReadContext context, java.lang.Object source, String sourcePath)
    throws ThingReadException {
    read((TM)builder, context, (JXPathContext)source, sourcePath);
  }

  public void read(TM builder, IThingReadContext context, JXPathContext source, String sourcePath) {
    // TODO: Other CDFDE component properties are not relevant now, 
    // but revisit this in the future.
            
    // Property Bindings
    Iterator<Map<String, Object>> mapIterator = source.iterate("properties");
    while(mapIterator.hasNext())
    {
      Map<String, Object> map = mapIterator.next();
      Object name = map.get( "name" );
      Object type = map.get( "type" );
      Object value = map.get( "value" );

      builder.addPropertyBinding(
        new UnresolvedPropertyBinding.Builder()
          .setAlias( name != null ? name.toString() : "" ) // matches prop/name
          .setInputType( type != null ? type.toString() : "" )
          .setValue( value != null ? value.toString() : "" ));
    }
    
    // Attributes
    Map<String, Object> jsComp = (Map<String, Object>)source.getContextBean();
    Set<String> keys = jsComp.keySet();
    for ( String key : keys ) {
      if( key.startsWith("meta_") ) {
        String name = key.substring("meta_".length());
        builder.addAttribute(name, jsComp.get( key ).toString() );
      }
    }
  }
}
