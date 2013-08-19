/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.reader.cdfdejs;

import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.reader.IThingReader;
import pt.webdetails.cdf.dd.model.core.reader.IThingReaderFactory;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

import pt.webdetails.cdf.dd.model.inst.*;
import pt.webdetails.cdf.dd.model.meta.*;

/**
 * @author dcleao
 */
public class CdfdeJsThingReaderFactory implements IThingReaderFactory
{
  private static final Logger logger = LoggerFactory.getLogger(CdfdeJsThingReaderFactory.class);
  
  private static final Pattern _modelIdToNamePattern = 
          Pattern.compile("^(?:Components|Layout|DataSources)?(.*?)(?:Model)?$");
  
  private final MetaModel _metaModel;
  
  public CdfdeJsThingReaderFactory(MetaModel metaModel)
  {
    if(metaModel== null) { throw new IllegalArgumentException("metaModel"); }
    
    this._metaModel = metaModel;
  }
  
  public IThingReader getReader(String kind, String className, String name)
      throws UnsupportedThingException
  {
    // Dashboard / Widget
    if(KnownThingKind.Dashboard.equals(kind))
    {
      return new CdfdeJsDashboardReader();
    }
    
    if(KnownThingKind.Component.equals(kind))
    {
      // 1. Argument className is taken from:
      //
      //     "components/rows/type"  or 
      //     "datasources/rows/type" or
      //     "layout/rows/type"
      //
      //    For "components" and "datasources", 
      //    this is the value of the corresponding component type's:
      //       modelId = "Components" + comp.getName()
      //       (see model.meta.writer.cderunjs.CdeRunJsComponentTypeWriter)
      //
      //    For "layout", this is by construction.
      
      if(StringUtils.isEmpty(className)) { throw new IllegalArgumentException("className"); }
      
      // TODO: HACK: until Layout classes can be handled like others,
      // falling in its corresponding «if», below.
      // (see CdfdeJsDashboardReader#read)
      if(className.equals("layout")) 
      {
        try
        {
          return new CdfdeJsLayoutComponentReader(
                  new LayoutComponentType.Builder()
                        .build(_metaModel.getPropertyTypeSource()));
        }
        catch(ValidationException ex)
        {
          logger.error("Error building dashboard layout.", ex);
          return null;
        }
      }

      // 2. Extract comp.getName() from modelId.
      String compTypeName = _modelIdToNamePattern.matcher(className).replaceFirst("$1");
      
      // 3. Lookup the ComponentType, by name, in the MetaModel.
      ComponentType compType;
      try
      {
        compType = this._metaModel.getComponentType(compTypeName);
        
        assert compType != null;
      }
      catch(IllegalArgumentException ex)
      {
        throw new UnsupportedThingException(kind, className);
      }
      
      // 4. Find the corresponding appropriate base class,
      //    that is a subclass of Component.
      Class compTypeClass = compType.getClass();
      
      Class<? extends Component.Builder> compBuilderClass = null;
      if(VisualComponentType.class.isAssignableFrom(compTypeClass))
      {
        if(GenericComponentType.class.isAssignableFrom(compTypeClass))
        {
          if(PrimitiveComponentType.class.isAssignableFrom(compTypeClass))
          {
            compBuilderClass = PrimitiveComponent.Builder.class;
          }
          else if(CustomComponentType.class.isAssignableFrom(compTypeClass))
          {
            compBuilderClass = CustomComponent.Builder.class;
          }
          else if(WidgetComponentType.class.isAssignableFrom(compTypeClass))
          {
            compBuilderClass = WidgetComponent.Builder.class;
          }
        } 
        else if(LayoutComponentType.class.isAssignableFrom(compTypeClass))
        {
          compBuilderClass = LayoutComponent.Builder.class;
        }
      }
      else // NonVisual
      {
        if(DataSourceComponentType.class.isAssignableFrom(compTypeClass))
        {
          compBuilderClass = DataSourceComponent.Builder.class;
        }
        else if(ParameterComponentType.class.isAssignableFrom(compTypeClass))
        {
          compBuilderClass = ParameterComponent.Builder.class;
        }
        else if(CodeComponentType.class.isAssignableFrom(compTypeClass)) 
        {
          compBuilderClass = CodeComponent.Builder.class;
        }
      }
      
      if(compBuilderClass == null)
      {
        throw new UnsupportedThingException(kind, className);
      
      }
      // 5. Return a corresponding IThingReader.
      return new CdfdeJsAdhocComponentReader(compBuilderClass, compType);
    }
    
    throw new UnsupportedThingException(kind, className);
  }
}
