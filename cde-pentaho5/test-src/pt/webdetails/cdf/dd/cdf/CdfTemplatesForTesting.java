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

package pt.webdetails.cdf.dd.cdf;

import pt.webdetails.cdf.dd.structure.DashboardStructureException;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

import java.io.IOException;

public class CdfTemplatesForTesting extends CdfTemplates {

  private String mockResourceEndpoint;
  private DashboardWcdfDescriptor mockDashboardWcdfDescriptor;

  public CdfTemplatesForTesting( String mockResourceEndpoint ) {
    super( mockResourceEndpoint );
    this.mockResourceEndpoint = mockResourceEndpoint;
  }

  public CdfTemplatesForTesting( String mockResourceEndpoint, DashboardWcdfDescriptor mockDashboardWcdfDescriptor ) {
    super( mockResourceEndpoint );
    this.mockResourceEndpoint = mockResourceEndpoint;
    this.mockDashboardWcdfDescriptor = mockDashboardWcdfDescriptor;
  }

  @Override
  protected String getResourceUrl( String resourceEndpoint ) {
    return mockResourceEndpoint;
  }

  @Override
  protected DashboardWcdfDescriptor loadWcdfDescriptor( String wcdfFile ) throws IOException {
    return mockDashboardWcdfDescriptor;
  }

  @Override
  public String addDashboardStyleAndRendererTypeToTemplate( String origStructure ) throws DashboardStructureException {
    return super.addDashboardStyleAndRendererTypeToTemplate( origStructure );
  }

  public DashboardWcdfDescriptor getMockDashboardWcdfDescriptor() {
    return mockDashboardWcdfDescriptor;
  }

  public void setMockDashboardWcdfDescriptor( DashboardWcdfDescriptor mockDashboardWcdfDescriptor ) {
    this.mockDashboardWcdfDescriptor = mockDashboardWcdfDescriptor;
  }
}
