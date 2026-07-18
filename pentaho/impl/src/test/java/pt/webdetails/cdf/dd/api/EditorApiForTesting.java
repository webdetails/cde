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



package pt.webdetails.cdf.dd.api;

import pt.webdetails.cdf.dd.reader.factory.IResourceLoader;
import pt.webdetails.cdf.dd.testUtils.MockResourceLoader;


public class EditorApiForTesting extends EditorApi {

  private MockResourceLoader mockResourceLoader;

  public void initMockResourceLoader() {
    this.mockResourceLoader = new MockResourceLoader();
  }

  @Override
  protected IResourceLoader getResourceLoader( String path ) {
    return this.mockResourceLoader;
  }

  public void setHasAccess( boolean hasAccess ) {
    mockResourceLoader.setHasAccess( hasAccess );
  }

  public void setSavedFile( boolean savedFile ) {
    mockResourceLoader.setSavedFile( savedFile );
  }
}
