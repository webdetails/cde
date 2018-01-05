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
