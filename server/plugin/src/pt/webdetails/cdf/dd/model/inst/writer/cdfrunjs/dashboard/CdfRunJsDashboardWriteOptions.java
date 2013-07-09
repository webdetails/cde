/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import java.io.Serializable;
import org.apache.commons.lang.StringUtils;

/**
 * @author dcleao
 */
public final class CdfRunJsDashboardWriteOptions implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  private final boolean _absolute, _debug;
  private final String  _absRoot, _scheme, _aliasPrefix;
  
  public CdfRunJsDashboardWriteOptions(
          boolean absolute, 
          boolean debug,
          String absRoot, 
          String scheme)
  {
    this("", absolute, debug, absRoot, scheme);
  }
  
  public CdfRunJsDashboardWriteOptions(
          String  aliasPrefix,
          boolean absolute, 
          boolean debug,
          String absRoot, 
          String scheme)
  {
    this._aliasPrefix = aliasPrefix;
    this._absolute    = absolute;
    this._debug       = debug;
    this._absRoot     = absRoot;
    this._scheme      = scheme;
  }
  
  public CdfRunJsDashboardWriteOptions addAliasPrefix(String aliasPrefix)
  {
    if(StringUtils.isEmpty(aliasPrefix)) { throw new IllegalArgumentException("aliasPrefix"); }
    
    return new CdfRunJsDashboardWriteOptions(
      StringUtils.isEmpty(this._aliasPrefix) ? 
        aliasPrefix : 
        (this._aliasPrefix + "_" + aliasPrefix),
      _absolute, 
      _debug, 
      _absRoot, 
      _scheme);
  }
          
  public String getAliasPrefix()
  {
    return this._aliasPrefix;
  }

  public String getAbsRoot()
  {
    return this._absRoot;
  }

  public String getSchemedRoot()
  {
    return (StringUtils.isEmpty(this._scheme) ? "" : (this._scheme + "://")) + this._absRoot;
  }
  
  public String getScheme()
  {
    return this._scheme;
  }

  public boolean isAbsolute()
  {
    return this._absolute;
  }

  public boolean isDebug()
  {
    return this._debug;
  }
}
