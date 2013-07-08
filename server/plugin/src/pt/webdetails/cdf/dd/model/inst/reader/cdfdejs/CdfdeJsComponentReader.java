
package pt.webdetails.cdf.dd.model.inst.reader.cdfdejs;

import java.util.Iterator;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

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
      throws ThingReadException
  {
    read((TM)builder, context, (JXPathContext)source, sourcePath);
  }

  public void read(TM builder, IThingReadContext context, JXPathContext source, String sourcePath)
  {
    // TODO: Other CDFDE component properties are not relevant now, 
    // but revisit this in the future.
            
    // Property Bindings
    Iterator<JSONObject> jsonIterator = source.iterate("properties");
    while(jsonIterator.hasNext())
    {
      JSONObject jsProp = jsonIterator.next();
      
      builder.addPropertyBinding(
        new UnresolvedPropertyBinding.Builder()
          .setAlias(jsProp.optString("name")) // matches prop/name
          .setInputType(jsProp.optString("type"))
          .setValue(jsProp.optString("value")));
    }
    
    // Attributes
    JSONObject jsComp = (JSONObject)source.getContextBean();
    Iterator<String> keys = jsComp.keys();
    while(keys.hasNext())
    {
      String key = keys.next();
      
      if(key.startsWith("meta_"))
      {
        String name = key.substring("meta_".length());
        builder.addAttribute(name, jsComp.getString(key));
      }
    }
  }
}
