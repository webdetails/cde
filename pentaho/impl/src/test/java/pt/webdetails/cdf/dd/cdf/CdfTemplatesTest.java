/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cdf.dd.cdf;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CdfTemplatesTest {
  private static final String RESOURCE_ENDPOINT = "resource_endpoint/";
  private static final String[] TEMPLATES = { "Template1", "Template2", "Template3" };
  private static final List<String> TEMPLATES_LIST = Arrays.asList( TEMPLATES );

  private static String savedTemplate = StringUtils.EMPTY;


  @Before
  public void setUp() {
    CdeEnvironmentForTests cdeEnvironmentForTests = new CdeEnvironmentForTests();

    List<IBasicFile> mockedFilesList = mockIBasicFiles( TEMPLATES_LIST );

    // mocking IReadAccess
    IReadAccess mockedReadAccess = mock( IReadAccess.class );
    when( mockedReadAccess.listFiles( any(), Mockito.<IBasicFileFilter>any(), anyInt() ) )
      .thenReturn( mockedFilesList );
    cdeEnvironmentForTests.setMockedReadAccess( mockedReadAccess );

    // mocking IRWAccess
    IRWAccess mockedRWAccess = mock( IRWAccess.class );
    when( mockedRWAccess.fileExists( any() ) ).thenReturn( true );
    when( mockedRWAccess.saveFile( any(), Mockito.<InputStream>any() ) ).thenAnswer( new Answer<Boolean>() {
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
      fail( "Template loading should have built a JSONArray" );
    }
  }

  @Test
  public void testTemplateSave() throws IOException, DashboardStructureException {
    CdfTemplates cdfTemplates = new CdfTemplatesForTesting( RESOURCE_ENDPOINT );
    String fileName = "fileName";

    String fileStructure = "{components: [], rows: [], layout:{}, title:\"title\"}";
    cdfTemplates.save( fileName, fileStructure );
    assertEquals( fileStructure, savedTemplate );

    fileStructure = "foo bar";
    cdfTemplates.save( fileName, fileStructure );
    assertEquals( fileStructure, savedTemplate );
  }

  private void verifyResult( JSONArray arr ) throws JSONException {
    assertEquals( "There should twice as much templates as were mocked (read from system and repository)",
      2 * TEMPLATES_LIST.size(), arr.length() );
    for ( int i = 0; i < arr.length(); i++ ) {
      Object obj = arr.get( i );
      if ( obj instanceof JSONObject ) {
        JSONObject structure = ( (JSONObject) obj ).getJSONObject( "structure" );
        String img =  ( (JSONObject) obj ).getString( "img" );
        assertTrue( "img property should start with the resource endpoint",
            img.startsWith( RESOURCE_ENDPOINT ) );
        assertEquals( "Expecting fileName to be present in the templatesList (the array is ordered)",
          TEMPLATES_LIST.get( i % TEMPLATES_LIST.size() ), structure.getString( "fileName" ) );
      } else {
        fail( "Only expecting JSONObject in the result array" );
      }
    }
  }

  private static List<IBasicFile> mockIBasicFiles( List<String> files ) {
    List<IBasicFile> iBasicFiles = new ArrayList<>();
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
