/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy;

import org.apache.commons.lang3.StringUtils;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;

public class PentahoCdfRunJsDashboardWriteContextForTesting extends PentahoCdfRunJsDashboardWriteContext {
  public PentahoCdfRunJsDashboardWriteContextForTesting( IThingWriterFactory factory, String indent,
                                                         boolean bypassCacheRead, Dashboard dash,
                                                         CdfRunJsDashboardWriteOptions options ) {
    super( factory, indent, bypassCacheRead, dash, options );
  }

  @Override
  protected String getRoot() {
    final CdfRunJsDashboardWriteOptions options = this.getOptions();

    final String schemeRoot = options.isAbsolute() && StringUtils.isNotEmpty( options.getAbsRoot() )
      ? options.getSchemedRoot() : "";

    return schemeRoot + "/pentaho/plugin/pentaho-cdf-dd/";
  }

  @Override
  protected String getSystemDir() {
    return "system";
  }

  @Override
  protected String getSystemPluginId() {
    return "mockPlugin";
  }
}
