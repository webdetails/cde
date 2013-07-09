/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

/**
 * @author dcleao
 */
public class DashboardType extends MetaObject
{
  private static final Log _logger = LogFactory.getLog(DashboardType.class);
  
  private static final DashboardType _instance;
  
  static
  {
    DashboardType instance;
    try
    {
      instance = new DashboardType.Builder().build();
    }
    catch(ValidationException ex)
    {
      // Should never happen
      _logger.error("Error creating DashboardType instance", ex);
      instance = null;
    }
    
    _instance = instance;
  }
  
  public static DashboardType getInstance()
  {
    return _instance;
  }
  
  protected DashboardType(Builder builder) throws ValidationException
  {
    super(builder);
  }

  @Override
  public String getKind()
  {
    return KnownThingKind.DashboardType;
  }
  
  /**
   * Class to create and modify DashboardType instances.
   */
  public static class Builder extends MetaObject.Builder
  {
    public DashboardType build() throws ValidationException
    {
      return new DashboardType(this);
    }
  }
}
