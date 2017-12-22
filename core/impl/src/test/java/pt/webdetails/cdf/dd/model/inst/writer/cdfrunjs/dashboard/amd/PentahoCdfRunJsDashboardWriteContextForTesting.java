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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.amd;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;

public class PentahoCdfRunJsDashboardWriteContextForTesting extends PentahoCdfRunJsDashboardWriteContext {
  public PentahoCdfRunJsDashboardWriteContextForTesting(
    IThingWriterFactory factory,
    String indent, boolean bypassCacheRead, Dashboard dash,
    CdfRunJsDashboardWriteOptions options ) {
    super( factory, indent, bypassCacheRead, dash, options );
  }

  @Override
  protected String getRoot() {
    return ( this._options.isAbsolute() && !StringUtils.isEmpty( this._options.getAbsRoot() ) )
      ? ( this._options.getSchemedRoot() + "/pentaho/plugin/pentaho-cdf-dd/" )
      : "/pentaho/plugin/pentaho-cdf-dd/";
  }

  @Override
  protected String getSystemDir() {
    return "system";
  }

  @Override
  protected String getPluginId( String path ) {
    return "mockPlugin";
  }
}
