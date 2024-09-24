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
