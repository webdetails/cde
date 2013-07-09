/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.validation.DuplicateAttributeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

/**
 * A thing with identity,
 * and structure, 
 * as provided by attributes.
 * 
 * @author dcleao
 */
public abstract class Entity extends Thing
{
  private final Map<String, Attribute> _attributesByName;

  @SuppressWarnings("OverridableMethodCallInConstructor")
  public Entity(Builder builder) throws ValidationException
  {
    if(builder.getAttributeCount() > 0)
    {
      this._attributesByName = new LinkedHashMap<String, Attribute>();

      for(Attribute.Builder metaBuilder : builder._attributes)
      {
        Attribute attribute = metaBuilder.build();
        if(this._attributesByName.containsKey(attribute.getName()))
        {
          throw new ValidationException(new DuplicateAttributeError(attribute.getName()));
        }

        this._attributesByName.put(attribute.getName(), attribute);
      }
    } else {
      this._attributesByName = null;
    }
  }

  public final Attribute getAttribute(String name)
  {
    Attribute attribute = this.tryGetAttribute(name);
    if(attribute == null)
    {
      throw new IllegalArgumentException("There is no attribute named '" + name + "'.");
    }

    return attribute;
  }

  public final Attribute tryGetAttribute(String name)
  {
    if(name == null) { throw new IllegalArgumentException("name"); }

    return this._attributesByName != null ?
            this._attributesByName.get(name) :
            null;
  }

  public String tryGetAttributeValue(String name, String defaultValue)
  {
    Attribute attr = this.tryGetAttribute(name);
    return attr == null ? 
           defaultValue : 
           StringUtils.defaultIfEmpty(attr.getValue(), defaultValue);
  }
  
  public final Iterable<Attribute> getAttributes()
  {
    return this._attributesByName != null ?
           this._attributesByName.values() :
           Collections.<Attribute> emptyList();
  }

  public final int getAttributeCount()
  {
    return this._attributesByName != null ? this._attributesByName.size() : 0;
  }

  public static abstract class Builder extends Thing.Builder
  {
    private List<Attribute.Builder> _attributes;

    public Builder addAttribute(Attribute.Builder attribute)
    {
      if(attribute == null) { throw new IllegalArgumentException("attribute"); }

      if(this._attributes == null)
      {
        this._attributes = new ArrayList<Attribute.Builder>();
      }

      this._attributes.add(attribute);

      return this;
    }

    public Builder addAttribute(String name, String value)
    {
      return this.addAttribute(new Attribute.Builder()
                  .setName(name)
                  .setValue(value));
    }

    public Iterable<Attribute.Builder> getAttributes()
    {
      return this._attributes != null ?
             this._attributes :
             Collections.<Attribute.Builder> emptyList();
    }

    public int getAttributeCount()
    {
      return this._attributes != null ? this._attributes.size() : 0;
    }
  }
}