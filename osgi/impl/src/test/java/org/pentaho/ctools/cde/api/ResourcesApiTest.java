/*!
 * Copyright 2018 - 2024 Webdetails, a Hitachi Vantara company. All rights reserved.
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
package org.pentaho.ctools.cde.api;

import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cpf.repository.api.IBasicFile;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourcesApiTest {
  private String maxAge;

  private ResourcesApi resourcesApi;

  @Before
  public void setUp() {
    this.resourcesApi = spy( new ResourcesApi() );
    this.resourcesApi.setResourceMaxAge( this.maxAge );
  }

  @Test
  public void testResource_successResponse() throws Exception {
    final String filename = "foo.txt";
    final String extension = "txt";
    final String mimeType = "text/plain";

    setMaxAge( "1337" );
    doReturn( createFileMock( filename, extension ) ).when( this.resourcesApi ).getFile( any() );

    Response actual = this.resourcesApi.resource( filename );
    assertSuccessResponse( actual, filename, mimeType );
  }

  @Test
  public void testResource_successResponseNoCacheControl() throws Exception {
    final String filename = "foo.css";
    final String extension = "css";
    final String mimeType = "text/css";

    setMaxAge( null );
    doReturn( createFileMock( filename, extension ) ).when( this.resourcesApi ).getFile( any() );

    Response actual = this.resourcesApi.resource( filename );
    assertSuccessResponse( actual, filename, mimeType );
  }

  @Test
  public void testResource_errorResponse() throws Exception {
    doReturn( null ).when( this.resourcesApi ).getFile( any() );

    Response actual = this.resourcesApi.resource( "" );

    assertErrorResponse( actual );
  }

  // -----

  private void assertSuccessResponse( Response actual, String filename, String mimeType ) {
    Status actualStatus = Status.fromStatusCode( actual.getStatus() );
    assertEquals( Status.OK, actualStatus );

    assertEquals( mimeType, actual.getHeaderString( "Content-Type" ) );

    String expectedCacheControl = this.maxAge != null ? "max-age=" + this.maxAge : null;
    assertEquals( expectedCacheControl, actual.getHeaderString( "Cache-Control" ) );

    String expectedContentDisposition = "inline; filename=\"" + filename + "\"";
    assertEquals( expectedContentDisposition, actual.getHeaderString( "content-disposition" ) );
  }

  private void assertErrorResponse( Response actual ) {
    Status actualStatus = Status.fromStatusCode( actual.getStatus() );

    assertEquals( actualStatus, Status.INTERNAL_SERVER_ERROR );
  }

  // -----

  private void setMaxAge( String maxAge ) {
    this.maxAge = maxAge;
    this.resourcesApi.setResourceMaxAge( maxAge );
  }

  private IBasicFile createFileMock( String filename, String extension ) throws IOException {
    IBasicFile file = mock( IBasicFile.class );

    when( file.getName() ).thenReturn( filename );
    when( file.getExtension() ).thenReturn( extension );

    when( file.getContents() ).thenReturn( null );

    return file;
  }
}
