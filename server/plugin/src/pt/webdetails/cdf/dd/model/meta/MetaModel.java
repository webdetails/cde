/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.meta.validation.DuplicateComponentTypeError;
import pt.webdetails.cdf.dd.model.meta.validation.DuplicatePropertyTypeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

/**
 * @author dcleao
 */
public class MetaModel extends MetaObject
{
  // NOTE: The fact that there are some legacy components
  // whose name differs only by case...causes us to recognize the difference.
  // (See: base/components/datasources/XactionResultSetRender.xml and 
  //  base/components/others/XActionRender.xml)
  private final Map<String, ComponentType> _componentTypesByName;
  
  private final Map<String, PropertyType>  _propertyTypesByLowerName;

  protected MetaModel(Builder builder) throws ValidationException
  {
    super(builder);

    this._componentTypesByName = new LinkedHashMap<String, ComponentType>();
    this._propertyTypesByLowerName  = new LinkedHashMap<String, PropertyType>();
    
    for(PropertyType.Builder propBuilder : builder._propertyTypes)
    {
      PropertyType prop = propBuilder.build();
      String key = prop.getName().toLowerCase();
      if(this._propertyTypesByLowerName.containsKey(key))
      {
        throw new ValidationException(new DuplicatePropertyTypeError(prop));
      }

      this._propertyTypesByLowerName.put(key, prop);
    }

    // Create a PropertyType source to support building of ComponentType.s
    
    // A sub-type of IObectBuildContext
    IPropertyTypeSource propSource = this.getPropertyTypeSource();
    
    for(ComponentType.Builder compBuilder : builder._componentTypes)
    {
      ComponentType comp = compBuilder.build(propSource);
      String key = comp.getName();//.toLowerCase();
      if(this._componentTypesByName.containsKey(key))
      {
        throw new ValidationException(new DuplicateComponentTypeError(comp));
      }

      this._componentTypesByName.put(key, comp);
    }
  }

  public final IPropertyTypeSource getPropertyTypeSource()
  {
    final MetaModel me = this;
    return new IPropertyTypeSource() {
      public PropertyType getProperty(String name)
      {
        String lkey = name != null ? name.toLowerCase() : "";
        return me._propertyTypesByLowerName.get(lkey);
      }
    };
  }

  @Override
  public String getKind()
  {
    return KnownThingKind.MetaModel;
  }
  
  public ComponentType getComponentType(String name)
  {
    ComponentType comp = this.tryGetComponentType(name);
    if(comp == null)
    {
      throw new IllegalArgumentException("There is no component type with name '" + name + "'.");
    }

    return comp;
  }

  public ComponentType tryGetComponentType(String name)
  {
    if(StringUtils.isEmpty(name)) { throw new IllegalArgumentException("name"); }

    return this._componentTypesByName.get(name/*.toLowerCase()*/);
  }

  public PropertyType  getPropertyType(String name)
  {
    PropertyType prop = this.tryGetPropertyType(name);
    if(prop == null)
    {
      throw new IllegalArgumentException("There is no property type with name '" + name + "'.");
    }

    return prop;
  }

  public PropertyType tryGetPropertyType(String name)
  {
    if(StringUtils.isEmpty(name)) { throw new IllegalArgumentException("name"); }

    return this._propertyTypesByLowerName.get(name.toLowerCase());
  }

  public Iterable<ComponentType> getComponentTypes()
  {
    return this._componentTypesByName.values();
  }

  public int getComponentTypeCount()
  {
    return this._componentTypesByName.size();
  }

  public Iterable<PropertyType>  getPropertyTypes()
  {
    return this._propertyTypesByLowerName.values();
  }

  public int getPropertyTypeCount()
  {
    return this._propertyTypesByLowerName.size();
  }

  public static class Builder extends MetaObject.Builder
  {
    private final List<ComponentType.Builder> _componentTypes;
    private final List<PropertyType.Builder>  _propertyTypes;

    public Builder()
    {
      super();
      
      this._componentTypes = new ArrayList<ComponentType.Builder>();
      this._propertyTypes  = new ArrayList<PropertyType.Builder>();
    }

    public Builder addComponent(ComponentType.Builder comp)
    {
      if(comp == null) { throw new IllegalArgumentException("comp"); }

      this._componentTypes.add(comp);
      return this;
    }

    public Builder addProperty(PropertyType.Builder prop)
    {
      if(prop == null) { throw new IllegalArgumentException("prop"); }

      this._propertyTypes.add(prop);
      return this;
    }

    public MetaModel build() throws ValidationException
    {
      return new MetaModel(this);
    }
  }
}
