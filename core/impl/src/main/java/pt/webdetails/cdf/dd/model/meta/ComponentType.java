/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd.model.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.meta.validation.ComponentTypeDuplicatePropertyError;
import pt.webdetails.cdf.dd.model.meta.validation.DuplicatePropertyTypeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.validation.ComponentTypeDuplicateResourceError;
import pt.webdetails.cpf.packager.origin.PathOrigin;

public abstract class ComponentType extends MetaObject {
  private static final Log _logger = LogFactory.getLog( ComponentType.class );

  private final Map<String, PropertyTypeUsage> _propertyUsagesByLowerAlias;
  private final Map<String, PropertyTypeUsage> _propertyUsagesByLowerName;

  private final List<String> _legacyNames;

  private final List<String> _definitionNames;
  private final Map<String, List<PropertyTypeUsage>> _propertyDefinitionsByLowerName;

  private final String implementationPath;
  private final Map<String, Resource> _resourcesByKey;

  private PathOrigin origin;
  private final int order;

  private final boolean supportsLegacy;
  private final boolean supportsAMD;

  protected ComponentType( Builder builder, final IPropertyTypeSource propSource ) throws ValidationException {
    super( builder );

    if ( propSource == null ) {
      throw new IllegalArgumentException( "propSource" );
    }

    if ( builder.getPropertyUsageCount() > 0 ) {
      // Local property definitions dictionary
      final Map<String, PropertyType> propertyTypesByLowerName = new LinkedHashMap<String, PropertyType>();

      if ( builder._propertyTypes != null ) {
        // Build local properties dictionary
        for ( PropertyType.Builder propBuilder : builder._propertyTypes ) {

          PropertyType prop;
          try {
            prop = propBuilder.build( this );
          } catch ( ValidationException ex ) {
            // Ignore PropertyType, log warning and continue.
            _logger.warn( ex.getError() );
            continue;
          }

          String key = prop.getName().toLowerCase();
          if ( propertyTypesByLowerName.containsKey( key ) ) {
            // Ignore PropertyType, log warning and continue.
            _logger.warn( new DuplicatePropertyTypeError( prop ) );
            continue;
          }

          propertyTypesByLowerName.put( key, prop );
        }
      }

      // USAGES
      final IPropertyTypeSource propSourceLocal = new IPropertyTypeSource() {
        public PropertyType getProperty( String name ) {
          // Test existence locally first
          String key = name != null ? name.toLowerCase() : "";
          PropertyType prop = propertyTypesByLowerName.get( key );

          // Only then ask the model for a public property, if there's no local definition.
          return prop != null ? prop : propSource.getProperty( name );
        }
      };

      this._propertyUsagesByLowerAlias = new LinkedHashMap<String, PropertyTypeUsage>();
      this._propertyUsagesByLowerName = new HashMap<String, PropertyTypeUsage>();
      this._propertyDefinitionsByLowerName = new HashMap<String, List<PropertyTypeUsage>>();
      this._definitionNames = new ArrayList<String>();

      for ( PropertyTypeUsage.Builder propUsageBuilder : builder._propertyUsages ) {

        PropertyTypeUsage propUsage;
        try {
          propUsage = propUsageBuilder.build( this, propSourceLocal );
        } catch ( ValidationException ex ) {
          // Ignore PropertyTypeUsage, log warning and continue.
          _logger.warn( ex.getError() );
          continue;
        }

        String aliasKey = propUsage.getAlias().toLowerCase();
        if ( this._propertyUsagesByLowerAlias.containsKey( aliasKey ) ) {
          // Ignore PropertyTypeUsage, log warning and continue.
          _logger.warn( new ComponentTypeDuplicatePropertyError(
              this.getLabel(),
              propUsage.getAlias() ) );
          continue;
        }

        this._propertyUsagesByLowerAlias.put( aliasKey, propUsage );

        // Replaces any previous entries
        String nameKey = propUsage.getProperty().getName().toLowerCase();
        this._propertyUsagesByLowerName.put( nameKey, propUsage );

        // -----

        String definitionName = propUsage.getDefinitionName();
        String definitionKey = definitionName.toLowerCase();
        List<PropertyTypeUsage> props = this._propertyDefinitionsByLowerName.get( definitionKey );
        if ( props == null ) {
          props = new ArrayList<PropertyTypeUsage>();
          this._propertyDefinitionsByLowerName.put( definitionKey, props );
          this._definitionNames.add( definitionName );
        }

        props.add( propUsage );
      }
    } else {
      this._propertyUsagesByLowerAlias = null;
      this._propertyUsagesByLowerName = null;
      this._propertyDefinitionsByLowerName = null;
      this._definitionNames = null;
    }

    if ( builder.getLegacyNameCount() > 0 ) {
      // Don't add duplicates
      Set<String> legacyNameSet = new HashSet<String>();
      List<String> legacyNames = new ArrayList<String>();
      for ( String legacyName : builder.getLegacyNames() ) {
        // Not bothering to warn...
        if ( !legacyNameSet.contains( legacyName ) ) {
          legacyNameSet.add( legacyName );
          legacyNames.add( legacyName );
        }
      }

      this._legacyNames = legacyNames.size() > 0 ? legacyNames : null;
    } else {
      this._legacyNames = null;
    }

    // IMPLEMENTATION
    // TODO: is implementationPath required?
    this.implementationPath = StringUtils.defaultIfEmpty( builder.getImplementationPath(), "" );

    if ( builder.getResourceCount() > 0 ) {
      this._resourcesByKey = new LinkedHashMap<String, Resource>();

      for ( Resource.Builder resBuilder : builder._resources ) {
        Resource resource = resBuilder.build();
        String key = resource.getKey();
        if ( this._resourcesByKey.containsKey( key ) ) {
          // Ignore Resource, log warning and continue.
          _logger.warn( new ComponentTypeDuplicateResourceError(
              this.getLabel(),
              resource.getType(),
              resource.getName() ) );
          continue;
        }

        this._resourcesByKey.put( key, resource );
      }
    } else {
      this._resourcesByKey = null;
    }
    this.origin = builder.origin;
    this.order = builder.order;

    this.supportsLegacy = builder.supportsLegacy;
    this.supportsAMD = builder.supportsAMD;
  }

  @Override
  public String getKind() {
    return KnownThingKind.ComponentType;
  }

  public final String getImplementationPath() {
    return this.implementationPath;
  }

  public PathOrigin getOrigin() {
    return this.origin;
  }

  public int getOrder() {
    return this.order;
  }

  // --------
  // PropertyTypeUsage
  public final PropertyTypeUsage getPropertyUsage( String alias ) {
    PropertyTypeUsage propUsage = this.tryGetPropertyUsage( alias );
    if ( propUsage == null ) {
      throw new IllegalArgumentException( "There is no property with alias '" + alias + "'." );
    }

    return propUsage;
  }

  public final PropertyTypeUsage getPropertyUsageByName( String name ) {
    PropertyTypeUsage propUsage = this.tryGetPropertyUsageByName( name );
    if ( propUsage == null ) {
      throw new IllegalArgumentException( "There is no property with name '" + name + "'." );
    }

    return propUsage;
  }

  public final PropertyTypeUsage tryGetPropertyUsage( String alias ) {
    if ( StringUtils.isEmpty( alias ) ) {
      throw new IllegalArgumentException( "alias" );
    }

    return this._propertyUsagesByLowerAlias != null
      ? this._propertyUsagesByLowerAlias.get( alias.toLowerCase() )
      : null;
  }

  public final PropertyTypeUsage tryGetPropertyUsageByName( String name ) {
    if ( StringUtils.isEmpty( name ) ) {
      throw new IllegalArgumentException( "name" );
    }

    return this._propertyUsagesByLowerName != null
      ? this._propertyUsagesByLowerName.get( name.toLowerCase() ) : null;
  }

  public final Iterable<PropertyTypeUsage> getPropertyUsages() {
    return this._propertyUsagesByLowerAlias != null
      ? this._propertyUsagesByLowerAlias.values()
      : Collections.<PropertyTypeUsage>emptyList();
  }

  public final int getPropertyUsageCount() {
    return this._propertyUsagesByLowerAlias != null ? this._propertyUsagesByLowerAlias.size() : 0;
  }

  // --------
  // Property Definition
  public final Iterable<PropertyTypeUsage> getPropertiesByDefinition( String definitionName ) {
    String key = StringUtils.defaultIfEmpty( definitionName, "" ).toLowerCase();
    return this._propertyDefinitionsByLowerName != null
      ? this._propertyDefinitionsByLowerName.get( key )
      : Collections.<PropertyTypeUsage>emptyList();
  }

  // Includes empty definition
  public final int getDefinitionCount() {
    return this._propertyDefinitionsByLowerName != null
      ? this._propertyDefinitionsByLowerName.size()
      : 0;
  }

  // Hope keySet maintains order.
  public final Iterable<String> getDefinitionNames() {
    return this._definitionNames != null
      ? this._definitionNames
      : Collections.<String>emptyList();
  }

  // --------
  // Legacy Names
  public Iterable<String> getLegacyNames() {
    return this._legacyNames != null
      ? this._legacyNames
      : Collections.<String>emptyList();
  }

  public int getLegacyNameCount() {
    return this._legacyNames != null ? this._legacyNames.size() : 0;
  }

  // --------
  // Resources

  public Resource getResource( Resource.Type type, String name ) {
    if ( type == null ) {
      throw new IllegalArgumentException( "type" );
    }
    if ( StringUtils.isEmpty( name ) ) {
      throw new IllegalArgumentException( "name" );
    }

    String key = Resource.buildKey( type, name );

    Resource resource = this._resourcesByKey != null
        ? this._resourcesByKey.get( key )
        : null;
    if ( resource == null ) {
      throw new IllegalArgumentException( "There is no resource with name '" + name + "' and type '" + type + "'." );
    }

    return resource;
  }

  public Iterable<Resource> getResources() {
    return this._resourcesByKey != null
      ? this._resourcesByKey.values()
      : Collections.<Resource>emptyList();
  }

  public int getResourceCount() {
    return this._resourcesByKey != null ? this._resourcesByKey.size() : 0;
  }

  public boolean supportsLegacy() {
    return this.supportsLegacy;
  }
  public boolean supportsAMD() {
    return this.supportsAMD;
  }

  /**
   * Class to create and modify ComponentType instances.
   */
  public abstract static class Builder extends MetaObject.Builder {
    private List<PropertyTypeUsage.Builder> _propertyUsages = new ArrayList<PropertyTypeUsage.Builder>();
    private List<PropertyType.Builder> _propertyTypes;

    private List<String> _legacyNames;

    private String _implementationPath;
    private List<Resource.Builder> _resources;
    private PathOrigin origin;
    private int order;

    // By default, all components (e.g. DataSourceComponent) should support legacy and AMD dashboards.
    // CustomComponents might only support one kind of dashboard.
    private boolean supportsLegacy = true;
    private boolean supportsAMD = true;

    public Builder() {
      super();
      _propertyUsages.add( new PropertyTypeUsage.Builder().setName( "name" ) );
    }

    public Builder setOrigin( PathOrigin pathOrigin ) {
      this.origin = pathOrigin;
      return this;
    }

    public Builder setOrder( int order ) {
      this.order = order;
      return this;
    }

    public String getImplementationPath() {
      return this._implementationPath;
    }

    public Builder setImplementationPath( String implementationPath ) {
      this._implementationPath = implementationPath;
      return this;
    }

    // --------
    // PropertyTypeUsage
    public Builder useProperty( PropertyTypeUsage.Builder prop ) {
      assert prop != null;

      this._propertyUsages.add( prop );

      return this;
    }

    public Builder useProperty( String alias, String name ) {
      return this.useProperty( new PropertyTypeUsage.Builder()
        .setAlias( alias )
        .setName( name ) );
    }

    public Builder useProperty( String alias, String name, String definitionName ) {
      return this.useProperty( new PropertyTypeUsage.Builder()
        .setAlias( alias )
        .setName( name )
        .setDefinitionName( definitionName ) );
    }

    public Iterable<PropertyTypeUsage.Builder> getPropertyUsages() {
      return this._propertyUsages != null
        ? this._propertyUsages
        : Collections.<PropertyTypeUsage.Builder>emptyList();
    }

    public int getPropertyUsageCount() {
      return this._propertyUsages != null ? this._propertyUsages.size() : 0;
    }

    public Builder addProperty( PropertyType.Builder prop ) {
      if ( prop == null ) {
        throw new IllegalArgumentException( "prop" );
      }

      if ( this._propertyTypes == null ) {
        this._propertyTypes = new ArrayList<PropertyType.Builder>();
      }

      this._propertyTypes.add( prop );

      return this;
    }

    public Iterable<PropertyType.Builder> getProperties() {
      return this._propertyTypes != null
        ? this._propertyTypes
        : Collections.<PropertyType.Builder>emptyList();
    }

    public int getPropertyCount() {
      return this._propertyTypes != null ? this._propertyTypes.size() : 0;
    }

    // --------
    // Legacy Names
    public Builder addLegacyName( String legacyName ) {
      if ( StringUtils.isEmpty( legacyName ) ) {
        throw new IllegalArgumentException( "legacyName" );
      }

      if ( this._legacyNames == null ) {
        this._legacyNames = new ArrayList<String>();
      }

      this._legacyNames.add( legacyName );

      return this;
    }

    public Iterable<String> getLegacyNames() {
      return this._legacyNames != null
        ? this._legacyNames
        : Collections.<String>emptyList();
    }

    public int getLegacyNameCount() {
      return this._legacyNames != null ? this._legacyNames.size() : 0;
    }

    // --------
    // Resources
    public Builder addResource( Resource.Builder res ) {
      assert res != null;

      res.setOrigin( origin );
      if ( this._resources == null ) {
        this._resources = new ArrayList<Resource.Builder>();
      }

      this._resources.add( res );

      return this;
    }

    public Iterable<Resource.Builder> getResources() {
      return this._resources != null
        ? this._resources
        : Collections.<Resource.Builder>emptyList();
    }

    public int getResourceCount() {
      return this._resources != null ? this._resources.size() : 0;
    }

    public void setSupportsLegacy( boolean supportsLegacy ) {
      this.supportsLegacy = supportsLegacy;
    }

    public void setSupportsAMD( boolean supportsAMD ) {
      this.supportsAMD = supportsAMD;
    }

    public boolean isSupportsLegacy() {
      return this.supportsLegacy;
    }

    public boolean isSupportsAMD() {
      return this.supportsAMD;
    }

    public abstract ComponentType build( IPropertyTypeSource propSource )
      throws ValidationException;
  }
}
