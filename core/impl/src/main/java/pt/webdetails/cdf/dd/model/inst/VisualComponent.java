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

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.VisualComponentType;

public class VisualComponent<TM extends VisualComponentType> extends Component<TM> {
  public static final String DEF_IDPREFIX = "render";

  private final String _priority;

  protected VisualComponent( Builder builder, MetaModel metaModel ) throws ValidationException {
    super( builder, metaModel );

    PropertyBinding bind = this.tryGetPropertyBinding( "priority" );
    this._priority = bind != null ? StringUtils.defaultIfEmpty( bind.getValue(), "" ) : "";
  }

  public final String getPriority() {
    return this._priority;
  }

  @Override
  public TM getMeta() {
    return super.getMeta();
  }

  @Override
  protected String initGetDefaultIdPrefix() {
    return DEF_IDPREFIX;
  }

  /**
   * Class to create and modify VisualComponent instances.
   */
  public static class Builder extends Component.Builder {
    @Override
    public VisualComponent build( MetaModel metaModel ) throws ValidationException {
      if ( metaModel == null ) {
        throw new IllegalArgumentException( "metaModel" );
      }

      return new VisualComponent( this, metaModel );
    }
  }
}
