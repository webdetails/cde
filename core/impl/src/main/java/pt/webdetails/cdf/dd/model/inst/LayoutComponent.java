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

package pt.webdetails.cdf.dd.model.inst;

import org.apache.commons.jxpath.JXPathContext;
import pt.webdetails.cdf.dd.model.core.validation.RequiredAttributeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.LayoutComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;

/**
 * // TODO: HACK:
 * Temporary implementation that stores all the layout rows
 * of a cdfde file...
 * Until RenderLayout can be transformed to this model, if anytime.
 */
public class LayoutComponent extends VisualComponent<LayoutComponentType> {
  private final JXPathContext _layoutXP;

  private LayoutComponent( Builder builder, MetaModel metaModel ) throws ValidationException {
    super( builder, metaModel );

    if ( builder._layoutXP == null ) {
      throw new ValidationException( new RequiredAttributeError( "LayoutXPContext" ) );
    }

    this._layoutXP = builder._layoutXP;
  }

  @Override
  public LayoutComponentType getMeta() {
    return super.getMeta();
  }

  public JXPathContext getLayoutXPContext() {
    return this._layoutXP;
  }

  /**
   * Class to create and modify LayoutComponent instances.
   */
  public static final class Builder extends VisualComponent.Builder {
    private JXPathContext _layoutXP;

    public Builder() {
    }

    public JXPathContext getLayoutXPContext() {
      return this._layoutXP;
    }

    public Builder setLayoutXPContext( JXPathContext layoutXP ) {
      this._layoutXP = layoutXP;
      return this;
    }

    @Override
    public LayoutComponent build( MetaModel metaModel ) throws ValidationException {
      if ( metaModel == null ) {
        throw new IllegalArgumentException( "metaModel" );
      }

      return new LayoutComponent( this, metaModel );
    }
  }
}
