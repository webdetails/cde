/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import java.io.Serializable;
import org.apache.commons.lang.StringUtils;

public class CdfRunJsDashboardWriteOptions implements Serializable {
  private static final long serialVersionUID = 1L;

  private final boolean _absolute, _debug, _amdModule;
  private final String  _absRoot, _scheme, _aliasPrefix;

  public CdfRunJsDashboardWriteOptions(
      boolean absolute,
      boolean debug,
      String absRoot,
      String scheme ) {
    this( "", false, absolute, debug, absRoot, scheme );
  }

  public CdfRunJsDashboardWriteOptions(
      boolean amdModule,
      boolean absolute,
      boolean debug,
      String absRoot,
      String scheme ) {
    this( "", amdModule, absolute, debug, absRoot, scheme );
  }

  public CdfRunJsDashboardWriteOptions(
      String  aliasPrefix,
      boolean amdModule,
      boolean absolute,
      boolean debug,
      String absRoot,
      String scheme ) {
    this._aliasPrefix = aliasPrefix;
    this._amdModule   = amdModule;
    this._absolute    = absolute;
    this._debug       = debug;
    this._absRoot     = absRoot;
    this._scheme      = scheme;
  }

  public CdfRunJsDashboardWriteOptions addAliasPrefix( String aliasPrefix ) {
    if ( StringUtils.isEmpty( aliasPrefix ) ) {
      throw new IllegalArgumentException( "aliasPrefix" );
    }

    return new CdfRunJsDashboardWriteOptions(
        StringUtils.isEmpty( this._aliasPrefix ) ? aliasPrefix : ( this._aliasPrefix + "_" + aliasPrefix ),
        _amdModule,
        _absolute,
        _debug,
        _absRoot,
        _scheme );
  }

  public String getAliasPrefix() {
    return this._aliasPrefix;
  }

  public String getAbsRoot() {
    return this._absRoot;
  }

  public String getSchemedRoot() {
    return ( StringUtils.isEmpty( this._scheme ) ? "" : ( this._scheme + "://" ) ) + this._absRoot;
  }

  public String getScheme() {
    return this._scheme;
  }

  public boolean isAbsolute() {
    return this._absolute;
  }

  public boolean isDebug() {
    return this._debug;
  }

  public boolean isAmdModule() {
    return this._amdModule;
  }
}
