/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.validation.RequiredAttributeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

/**
 * @author dcleao
 */
public final class Resource
{
  private final String _name;
  private final String _app;
  private final String _version;
  private final String _source;
  private final Type   _type;

  private Resource(Builder builder) throws ValidationException
  {
    assert builder != null;

    if(StringUtils.isEmpty(builder._source))
    {
      throw new ValidationException(new RequiredAttributeError("Source"));
    }

    if(builder._type == null)
    {
      throw new ValidationException(new RequiredAttributeError("Type"));
    }

    this._name    = StringUtils.defaultIfEmpty(builder._name, builder._source);
    this._app     = StringUtils.defaultIfEmpty(builder._app, "");
    this._source  = builder._source;
    this._version = StringUtils.defaultIfEmpty(builder._version, "1.0");
    // TODO: validate version format
    
    this._type    = builder._type;
  }

  // -------------
  // Properties
  public static String buildKey(Type type, String name)
  {
    return type + "|" + (name == null ? "" : name);
  }

  public String getKey()
  {
    return buildKey(this._type, this._name);
  }

  public String getName()
  {
    return this._name;
  }

  public String getVersion()
  {
    return this._version;
  }

  public String getSource()
  {
    return this._source;
  }

  public Type getType()
  {
    return this._type;
  }

  public String getApp()
  {
    return this._app;
  }

  // ------------

  public enum Type
  {
    SCRIPT,
    RAW, // Raw code
    STYLE
  }

  public final static class Builder
  {
    private String _name;
    private String _version;
    private String _source;
    private Type   _type;
    private String _app;

    // ----------
    // Properties

    public String getName()
    {
      return this._name;
    }

    public Builder setName(String name)
    {
      this._name = name;
      return this;
    }

    public String getVersion()
    {
      return this._version;
    }

    public Builder setVersion(String version)
    {
      this._version = version;
      return this;
    }

    public String getSource()
    {
      return this._source;
    }

    public Builder setSource(String source)
    {
      this._source = source;
      return this;
    }

    public Type getType()
    {
      return this._type;
    }

    public Builder setType(Type type)
    {
      this._type = type;
      return this;
    }

    public String getApp()
    {
      return this._app;
    }

    public Builder setApp(String app)
    {
      this._app = app;
      return this;
    }

    public Resource build() throws ValidationException
    {
      return new Resource(this);
    }
  }
}
