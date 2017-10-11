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

package pt.webdetails.cdf.dd.cdf;

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import pt.webdetails.cdf.dd.CdeEngineForTests;
import pt.webdetails.cdf.dd.CdeEnvironmentForTests;
import pt.webdetails.cdf.dd.structure.DashboardStructureException;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class CdfTemplatesTest {
  private static final String RESOURCE_ENDPOINT = "resource_endpoint/";

  private static String[] templates = { "Template1", "Template2", "Template3" };
  private static List<String> templatesList = Arrays.asList( templates );

  private static String savedTemplate = "";


  @Before
  public void setUp() {
    CdeEnvironmentForTests cdeEnvironmentForTests = new CdeEnvironmentForTests();

    List<IBasicFile> mockedFilesList = mockIBasicFiles( templatesList );

    // mocking IReadAccess
    IReadAccess mockedReadAccess = mock( IReadAccess.class );
    when( mockedReadAccess.listFiles( anyString(), any( IBasicFileFilter.class ), anyInt() ) )
      .thenReturn( mockedFilesList );
    cdeEnvironmentForTests.setMockedReadAccess( mockedReadAccess );

    // mocking IRWAccess
    IRWAccess mockedRWAccess = mock( IRWAccess.class );
    when( mockedRWAccess.fileExists( anyString() ) ).thenReturn( true );
    when( mockedRWAccess.saveFile( anyString(), any( InputStream.class ) ) ).thenAnswer( new Answer<Boolean>() {
      @Override
      public Boolean answer( InvocationOnMock invocation ) throws Throwable {
        InputStream is = (InputStream) invocation.getArguments()[1];
        StringWriter wr = new StringWriter();
        IOUtils.copy( is, wr );
        savedTemplate = wr.toString();
        return true;
      }
    } );

    cdeEnvironmentForTests.setMockedRWAccess( mockedRWAccess );

    // injecting our test environment
    new CdeEngineForTests( cdeEnvironmentForTests );
  }

  @Test
  public void testTemplateLoad() throws JSONException {
    CdfTemplates cdfTemplates = new CdfTemplatesForTesting( RESOURCE_ENDPOINT );
    JSONObject result = new JSONObject();
    result.put( "result", cdfTemplates.load() );
    Object obj = result.get( "result" );
    if ( obj instanceof JSONArray ) {
      verifyResult( (JSONArray) obj );
    } else {
      Assert.fail( "Template loading should have built a JSONArray" );
    }
  }

  @Test
  public void testTemplateSave() throws IOException, DashboardStructureException {
    CdfTemplates cdfTemplates = new CdfTemplatesForTesting( RESOURCE_ENDPOINT );
    String fileName = "fileName";

    String fileStructure = "{components: [], rows: [], layout:{}, title:\"title\"}";
    cdfTemplates.save( fileName, fileStructure );
    Assert.assertEquals( fileStructure, savedTemplate );

    fileStructure = "foo bar";
    cdfTemplates.save( fileName, fileStructure );
    Assert.assertEquals( fileStructure, savedTemplate );
  }

  private void verifyResult( JSONArray arr ) throws JSONException {
    Assert.assertTrue( "There should twice as much templates as were mocked (read from system and repository)",
        arr.length() == ( templatesList.size() * 2 ) );
    for ( int i = 0; i < arr.length(); i++ ) {
      Object obj = arr.get( i );
      if ( obj instanceof JSONObject ) {
        JSONObject structure = ( (JSONObject) obj ).getJSONObject( "structure" );
        String img =  ( (JSONObject) obj ).getString( "img" );
        Assert.assertTrue( "img property should start with the resource endpoint",
            img.startsWith( RESOURCE_ENDPOINT ) );
        Assert.assertTrue( "Expecting fileName to be present in the templatesList (the array is ordered)",
            structure.getString( "fileName" ).equals( templatesList.get( i % templatesList.size() ) ) );
      } else {
        Assert.fail( "Only expecting JSONObject in the result array" );
      }
    }
  }

  private static List<IBasicFile> mockIBasicFiles( List<String> files ) {
    List<IBasicFile> iBasicFiles = new ArrayList<IBasicFile>();
    for ( String file : files ) {
      IBasicFile iBasicFile = mock( IBasicFile.class );
      when( iBasicFile.getName() ).thenReturn( file );
      when( iBasicFile.getFullPath() ).thenReturn( file );
      iBasicFiles.add( iBasicFile );
    }
    return iBasicFiles;
  }


  @After
  public void tearDown() {

  }

}
