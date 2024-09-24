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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

public class DashboardType extends MetaObject {
  private static final Log _logger = LogFactory.getLog( DashboardType.class );

  private static final DashboardType _instance;

  static {
    DashboardType instance;
    try {
      instance = new DashboardType.Builder().build();
    } catch ( ValidationException ex ) {
      // Should never happen (Would be FATAL!)
      _logger.error( "Error creating DashboardType instance", ex );
      instance = null;
    }

    _instance = instance;
  }

  public static DashboardType getInstance() {
    return _instance;
  }

  protected DashboardType( Builder builder ) throws ValidationException {
    super( builder );
  }

  @Override
  public String getKind() {
    return KnownThingKind.DashboardType;
  }

  /**
   * Class to create and modify DashboardType instances.
   */
  public static class Builder extends MetaObject.Builder {
    public DashboardType build() throws ValidationException {
      return new DashboardType( this );
    }
  }
}
