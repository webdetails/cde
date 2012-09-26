/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cdf.dd;

import java.util.Date;
import org.pentaho.platform.api.engine.IParameterProvider;


public interface Dashboard
{

  public String render();
  public String render(IParameterProvider params);
  public String getHeader();

  public String getContent();

  public Date getLoaded();
  
  public String getType();
}
