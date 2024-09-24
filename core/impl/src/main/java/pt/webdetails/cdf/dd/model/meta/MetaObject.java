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

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.Entity;
import pt.webdetails.cdf.dd.model.core.validation.RequiredAttributeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.util.Utils;

public abstract class MetaObject extends Entity {
  public static final String DEF_VERSION = "1.0";
  public static final String DEF_CATEGORY = "OTHERCOMPONENTS";
  public static final String DEF_CATEGORY_LABEL = "Others";

  private final String _name;
  private final String _camelName;
  private final String _label;
  private final String _tooltip;
  private final String _category;
  private final String _categoryLabel;
  private final String _sourcePath;
  private final String _version;
  private final boolean _visible;

  protected MetaObject( Builder builder ) throws ValidationException {
    super( builder );

    String name = this.initName( builder );
    assert name != null;

    this._version = StringUtils.isEmpty( builder._version ) ? DEF_VERSION : builder._version;

    String cat = builder._category;
    String catDesc = builder._categoryLabel;
    if ( StringUtils.isEmpty( cat ) ) {
      if ( StringUtils.isNotEmpty( catDesc ) ) {
        throw new ValidationException( new RequiredAttributeError( "Category" ) );
      }

      cat = DEF_CATEGORY;
      catDesc = DEF_CATEGORY_LABEL;
    }

    this._name = name;
    this._camelName = Utils.toFirstLowerCase( name );
    this._visible = builder._visible == null || builder._visible.booleanValue();
    this._label = StringUtils.defaultIfEmpty( builder._label, "" );
    this._tooltip = StringUtils.defaultIfEmpty( builder._tooltip, "" );
    this._category = StringUtils.defaultIfEmpty( cat, "" );
    this._categoryLabel = StringUtils.defaultIfEmpty( catDesc, this._category );
    this._sourcePath = StringUtils.defaultIfEmpty( builder._sourcePath, "" );
  }

  private String initName( Builder builder ) {
    // Although a name can be empty, there can only be one empty named component per kind...
    String name = builder._name;
    return name == null ? "" : name.trim();
  }

  @Override
  public final String getId() {
    return this._name;
  }

  // ---------------
  // Simple Properties

  public final String getName() {
    return this._name;
  }

  public final String getCamelName() {
    return this._camelName;
  }

  public String getVersion() {
    return this._version;
  }

  public final String getLabel() {
    return this._label;
  }

  public final String getTooltip() {
    return this._tooltip;
  }

  public final String getCategory() {
    return this._category;
  }

  public final String getCategoryLabel() {
    return this._categoryLabel;
  }

  public final String getSourcePath() {
    return this._sourcePath;
  }

  public final Boolean getVisible() {
    return this._visible;
  }

  /**
   * Class to create and modify MetaObject instances.
   */
  public abstract static class Builder extends Entity.Builder {
    private String _name;
    private String _label;
    private String _category;
    private String _categoryLabel;
    private String _tooltip;
    private String _sourcePath;
    private String _version;
    private Boolean _visible;

    // -----------
    // Atomic properties
    public String getName() {
      return this._name;
    }

    public Builder setName( String name ) {
      this._name = name;
      return this;
    }

    public String getVersion() {
      return this._version;
    }

    public Builder setVersion( String version ) {
      this._version = version;
      return this;
    }

    public String getLabel() {
      return this._label;
    }

    public Builder setLabel( String label ) {
      this._label = label;
      return this;
    }

    public String getTooltip() {
      return this._tooltip;
    }

    public Builder setTooltip( String tooltip ) {
      this._tooltip = tooltip;
      return this;
    }

    public String getCategory() {
      return this._category;
    }

    public Builder setCategory( String category ) {
      this._category = category;
      return this;
    }

    public String getCategoryLabel() {
      return this._categoryLabel;
    }

    public Builder setCategoryLabel( String label ) {
      this._categoryLabel = label;
      return this;
    }

    public String getSourcePath() {
      return this._sourcePath;
    }

    public Builder setSourcePath( String sourcePath ) {
      this._sourcePath = sourcePath;
      return this;
    }

    public Boolean getVisible() {
      return this._visible;
    }

    public Builder setVisible( Boolean visible ) {
      this._visible = visible;
      return this;
    }
  }
}
