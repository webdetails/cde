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


package pt.webdetails.cdf.dd.model.inst.validation;

public final class DashboardDuplicateComponentError extends ComponentError {
  private final String _componentName;

  public DashboardDuplicateComponentError( String componentName, String dashboardName )
      throws IllegalArgumentException {
    super( dashboardName, "Dashboard" );

    if ( componentName == null ) {
      throw new IllegalArgumentException( "componentName" );
    }

    this._componentName = componentName;
  }

  public String getComponentName() {
    return this._componentName;
  }

  @Override
  public String toString() {
    return "Dashboard of id '" + this._componentId + " has a duplicate component with name '" + this._componentName
      + "'.";
  }
}
