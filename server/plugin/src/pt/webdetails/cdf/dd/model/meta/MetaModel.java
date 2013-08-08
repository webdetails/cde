/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.meta.validation.DuplicatePropertyTypeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

/**
 * @author dcleao
 */
public class MetaModel extends MetaObject
{
  private static final Logger logger = LoggerFactory.getLogger(MetaModel.class);
  
  // NOTE: The fact that there are some legacy components
  // whose name differs only by case...causes us to need to recognize the difference.
  // (See: base/components/datasources/XactionResultSetRender.xml and 
  //  base/components/others/XActionRender.xml)
  // On the other hand, some components need to be detected independently of casing...
  // So, we first lookup with normal case and, when not found,
  //  lookup by lower case...
  // Of course, this requires us to store the mapping twice... :-/
  private final Map<String, ComponentType> _componentTypesByName;
  private final Map<String, ComponentType> _componentTypesByLowerName;
  
  private final Map<String, PropertyType>  _propertyTypesByLowerName;

  protected MetaModel(Builder builder) throws ValidationException
  {
    super(builder);

    this._componentTypesByName = new LinkedHashMap<String, ComponentType>();
    // Don't need two «keep order» implementations.
    this._componentTypesByLowerName = new HashMap<String, ComponentType>();
    
    this._propertyTypesByLowerName  = new LinkedHashMap<String, PropertyType>();
    
    for(PropertyType.Builder propBuilder : builder._propertyTypes)
    {
      PropertyType prop = propBuilder.build();
      String key = prop.getName().toLowerCase();
      if(this._propertyTypesByLowerName.containsKey(key))
      {
        logger.warn(
            "While building the meta-model. Ignoring property type definition.",
            new DuplicatePropertyTypeError(prop));
      } else {
        this._propertyTypesByLowerName.put(key, prop);
      }
    }

    // Create a PropertyType source to support building of ComponentType.s
    
    // A sub-type of IObectBuildContext
    IPropertyTypeSource propSource = this.getPropertyTypeSource();
    
    for(ComponentType.Builder compBuilder : builder._componentTypes)
    {
      ComponentType comp = compBuilder.build(propSource);
      
      // Overriding is detected by normal case.
      String key = comp.getName();
      if(this._componentTypesByName.containsKey(key))
      {
        // This is expected. By definition, we allow component overriding.
        logger.info("ComponentType '" + comp.getLabel() + "' was overriden.");
      }

      this._componentTypesByName.put(key, comp);
      // The following may replace another component, 
      // in case two != exist with different casing.
      // So, access by lower case name to the other component is shadowed.
      // This is highly non-linear and undesirable: legacy inheritage.
      // Example of undesirable behavior:
      // the addition of a component type may break
      // previously working access to another different case one...
      this._componentTypesByLowerName.put(key.toLowerCase(), comp);
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

    ComponentType compType = this._componentTypesByName.get(name);
    return compType != null ?
           compType :
           this._componentTypesByLowerName.get(name.toLowerCase());
  }

  public PropertyType getPropertyType(String name)
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
