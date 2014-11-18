/*!
 * Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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

import org.apache.commons.jxpath.JXPathContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunRequireJsDashboardWriter;
import pt.webdetails.cdf.dd.render.RenderMobileLayout;
import pt.webdetails.cdf.dd.render.Renderer;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

import java.io.IOException;

public final class CdfRunRequireJsMobileDashboardWriter extends CdfRunRequireJsDashboardWriter {
  protected static final String TYPE = "mobile";
  protected static final String MOBILE_TEMPLATE = "resources/mobile/index.html";

  public String getType() {
    return TYPE;
  }

  @Override
  protected String readTemplate( DashboardWcdfDescriptor wcdf ) throws IOException {
    return readTemplateFile( MOBILE_TEMPLATE );
  }

  @Override
  protected Renderer getLayoutRenderer( JXPathContext docXP, CdfRunJsDashboardWriteContext context ) {
    return new RenderMobileLayout( docXP, context );
  }
}


