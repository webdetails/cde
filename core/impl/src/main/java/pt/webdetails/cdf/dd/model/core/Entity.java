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

package pt.webdetails.cdf.dd.model.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.model.core.validation.DuplicateAttributeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

/**
 * A thing with identity,
 * and structure, 
 * as provided by attributes.
 */
public abstract class Entity extends Thing {
  private static final Log _logger = LogFactory.getLog( Entity.class );

  private final Map<String, Attribute> _attributesByName;

  public Entity( Builder builder ) throws ValidationException {
    if ( builder.getAttributeCount() > 0 ) {
      this._attributesByName = new LinkedHashMap<String, Attribute>();

      for ( Attribute.Builder metaBuilder : builder._attributes ) {
        Attribute attribute;
        try {
          attribute = metaBuilder.build();
        } catch ( ValidationException ex ) {
          // Ignore attribute, log warning and continue
          _logger.warn( ex );
          continue;
        }

        if ( this._attributesByName.containsKey( attribute.getName() ) ) {
          // Ignore attribute, log warning and continue
          _logger.warn( new DuplicateAttributeError( attribute.getName() ) );
          continue;
        }

        this._attributesByName.put( attribute.getName(), attribute );
      }
    } else {
      this._attributesByName = null;
    }
  }

  public final Attribute getAttribute( String name ) {
    Attribute attribute = this.tryGetAttribute( name );
    if ( attribute == null ) {
      throw new IllegalArgumentException( "There is no attribute named '" + name + "'." );
    }

    return attribute;
  }

  public final Attribute tryGetAttribute( String name ) {
    if ( name == null ) {
      throw new IllegalArgumentException( "name" );
    }

    return this._attributesByName != null ? this._attributesByName.get( name ) : null;
  }

  public String tryGetAttributeValue( String name, String defaultValue ) {
    Attribute attr = this.tryGetAttribute( name );
    return attr == null ? defaultValue : StringUtils.defaultIfEmpty( attr.getValue(), defaultValue );
  }

  public final Iterable<Attribute> getAttributes() {
    return this._attributesByName != null ? this._attributesByName.values() : Collections.<Attribute>emptyList();
  }

  public final int getAttributeCount() {
    return this._attributesByName != null ? this._attributesByName.size() : 0;
  }

  public abstract static class Builder extends Thing.Builder {
    private List<Attribute.Builder> _attributes;

    public Builder addAttribute( Attribute.Builder attribute ) {
      if ( attribute == null ) {
        throw new IllegalArgumentException( "attribute" );
      }

      if ( this._attributes == null ) {
        this._attributes = new ArrayList<Attribute.Builder>();
      }

      this._attributes.add( attribute );

      return this;
    }

    public Builder addAttribute( String name, String value ) {
      return this.addAttribute( new Attribute.Builder()
                  .setName( name )
                  .setValue( value ) );
    }

    public Iterable<Attribute.Builder> getAttributes() {
      return this._attributes != null ? this._attributes : Collections.<Attribute.Builder>emptyList();
    }

    public int getAttributeCount() {
      return this._attributes != null ? this._attributes.size() : 0;
    }
  }
}
