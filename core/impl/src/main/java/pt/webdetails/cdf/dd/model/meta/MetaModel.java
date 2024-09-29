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


package pt.webdetails.cdf.dd.model.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.meta.validation.DuplicatePropertyTypeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

public class MetaModel extends MetaObject {
  private static final Log _logger = LogFactory.getLog( MetaModel.class );

  // NOTE: The fact that there are some legacy components
  // whose name differs only by case...causes us to need to recognize the difference.
  // (See: base/components/datasources/XactionResultSetRender.xml and 
  //  base/components/others/XActionRender.xml)
  private final Map<String, ComponentType> _componentTypesByName;
  private final Map<String, ComponentType> _componentTypesByLegacyName;

  private final Map<String, PropertyType> _propertyTypesByLowerName;

  protected MetaModel( Builder builder ) throws ValidationException {
    super( builder );

    this._componentTypesByName = new LinkedHashMap<String, ComponentType>();

    // Don't need two «keep order» implementations.
    this._componentTypesByLegacyName = new HashMap<String, ComponentType>();

    this._propertyTypesByLowerName = new LinkedHashMap<String, PropertyType>();

    for ( PropertyType.Builder propBuilder : builder._propertyTypes ) {
      PropertyType prop;
      try {
        prop = propBuilder.build();
      } catch ( ValidationException ex ) {
        // Ignore PropertyType, log warning and continue.
        _logger.warn( ex.getError() );
        continue;
      }

      String key = prop.getName().toLowerCase();
      if ( !this._propertyTypesByLowerName.containsKey( key ) ) {
        this._propertyTypesByLowerName.put( key, prop );
      } else {
        // Ignore PropertyType, log warning and continue.
        _logger.warn( new DuplicatePropertyTypeError( prop ) );
        continue;
      }
    }

    // Create a PropertyType source to support building of ComponentType.s

    // A sub-type of IObectBuildContext
    IPropertyTypeSource propSource = this.getPropertyTypeSource();

    for ( ComponentType.Builder compBuilder : builder._componentTypes ) {
      ComponentType comp;
      try {
        comp = compBuilder.build( propSource );
      } catch ( ValidationException ex ) {
        // Ignore ComponentType, log warning and continue.
        _logger.warn( ex.getError() );
        continue;
      }

      String key = comp.getName();

      // Detect Component Type Override.
      ComponentType oldComp = this._componentTypesByName.get( key );

      // Add new component
      this._componentTypesByName.put( key, comp );
      for ( String legacyName : comp.getLegacyNames() ) {
        this._componentTypesByLegacyName.put( legacyName, comp );
      }

      // Check if oldComp was registered under additional legacy names.
      // If a component is overriden more than once, we accumulate all legacyNames "so far".
      // This thus overrides all mappings to the previous definition.
      if ( oldComp != null ) {
        _logger.info( "ComponentType '" + oldComp.getLabel() + "' was overriden." );

        List<String> alternateNames = null;
        for ( Map.Entry<String, ComponentType> entry : this._componentTypesByLegacyName.entrySet() ) {
          if ( entry.getValue() == oldComp ) {
            if ( alternateNames == null ) {
              alternateNames = new ArrayList<String>();
            }
            alternateNames.add( entry.getKey() );
          }
        }

        if ( alternateNames != null ) {
          for ( String altName : alternateNames ) {
            // Replace oldComp by comp
            this._componentTypesByLegacyName.put( altName, comp );
          }
        }
      }
    }
  }

  public final IPropertyTypeSource getPropertyTypeSource() {
    final MetaModel me = this;
    return new IPropertyTypeSource() {
      public PropertyType getProperty( String name ) {
        String lkey = name != null ? name.toLowerCase() : "";
        return me._propertyTypesByLowerName.get( lkey );
      }
    };
  }

  @Override
  public String getKind() {
    return KnownThingKind.MetaModel;
  }

  public ComponentType getComponentType( String name ) {
    ComponentType comp = this.tryGetComponentType( name );
    if ( comp == null ) {
      throw new IllegalArgumentException( "There is no component type with name '" + name + "'." );
    }

    return comp;
  }

  public ComponentType tryGetComponentType( String name ) {
    if ( StringUtils.isEmpty( name ) ) {
      throw new IllegalArgumentException( "name" );
    }

    ComponentType compType = this._componentTypesByName.get( name );
    return compType != null ? compType : this._componentTypesByLegacyName.get( name );
  }

  public PropertyType getPropertyType( String name ) {
    PropertyType prop = this.tryGetPropertyType( name );
    if ( prop == null ) {
      throw new IllegalArgumentException( "There is no property type with name '" + name + "'." );
    }

    return prop;
  }

  public PropertyType tryGetPropertyType( String name ) {
    if ( StringUtils.isEmpty( name ) ) {
      throw new IllegalArgumentException( "name" );
    }

    return this._propertyTypesByLowerName.get( name.toLowerCase() );
  }

  public Iterable<ComponentType> getComponentTypes() {
    return this._componentTypesByName.values();
  }

  public int getComponentTypeCount() {
    return this._componentTypesByName.size();
  }

  public Iterable<PropertyType> getPropertyTypes() {
    return this._propertyTypesByLowerName.values();
  }

  public int getPropertyTypeCount() {
    return this._propertyTypesByLowerName.size();
  }

  public static class Builder extends MetaObject.Builder {
    private final List<ComponentType.Builder> _componentTypes;
    private final List<PropertyType.Builder> _propertyTypes;

    public Builder() {
      super();

      this._componentTypes = new ArrayList<ComponentType.Builder>();
      this._propertyTypes = new ArrayList<PropertyType.Builder>();
    }

    public Builder addComponent( ComponentType.Builder comp ) {
      if ( comp == null ) {
        throw new IllegalArgumentException( "comp" );
      }

      this._componentTypes.add( comp );
      return this;
    }

    public Builder addProperty( PropertyType.Builder prop ) {
      if ( prop == null ) {
        throw new IllegalArgumentException( "prop" );
      }

      this._propertyTypes.add( prop );
      return this;
    }

    public MetaModel build() throws ValidationException {
      return new MetaModel( this );
    }
  }
}
