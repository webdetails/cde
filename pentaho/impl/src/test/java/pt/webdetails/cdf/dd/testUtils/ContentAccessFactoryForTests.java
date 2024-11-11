/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cdf.dd.testUtils;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import pt.webdetails.cdf.dd.api.RenderApiTest;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

public class ContentAccessFactoryForTests implements IContentAccessFactory {
  private IUserContentAccess mockedContentAccess;
  private IReadAccess mockedReadAccess;
  private IRWAccess mockedRWAccess;
  private static final String USER_DIR = System.getProperty( "user.dir" );
  private static final String TEST_RESOURCES = USER_DIR + File.separator + "src" + File.separator + "test" + File.separator + "resources";
  private static final String LIST_COMPONENTS = "resources" + File.separator + "components";

  public ContentAccessFactoryForTests( IUserContentAccess mockedContentAccess, IReadAccess mockedReadAccess ) {
    this.mockedContentAccess = mockedContentAccess;
    this.mockedReadAccess = mockedReadAccess;
  }

  public ContentAccessFactoryForTests( IUserContentAccess mockedContentAccess, IReadAccess mockedReadAccess,
                                       IRWAccess mockedRWAccess ) {
    this.mockedContentAccess = mockedContentAccess;
    this.mockedReadAccess = mockedReadAccess;
    this.mockedRWAccess = mockedRWAccess;
  }

  @Override
  public IUserContentAccess getUserContentAccess( String s ) {
    return mockedContentAccess;
  }

  @Override
  public IReadAccess getPluginRepositoryReader( String s ) {
    return mockedReadAccess;
  }

  @Override
  public IRWAccess getPluginRepositoryWriter( String s ) {
    return mockedRWAccess;
  }

  @Override
  public IReadAccess getPluginSystemReader( String s ) {
    if ( s == null || !s.equals( LIST_COMPONENTS ) ) {
      return mockedReadAccess;
    }
    IReadAccess readAccess = mock( IReadAccess.class );
    when( readAccess.listFiles( any(), Mockito.<IBasicFileFilter>any(), anyInt() ) )
      .thenReturn( listBasicFiles( TEST_RESOURCES + File.separator + s ) );
    try {
      when( readAccess.getFileInputStream( any() ) ).thenAnswer( new Answer<InputStream>() {
        @Override
        public InputStream answer( InvocationOnMock invocationOnMock ) throws Throwable {
          return RenderApiTest.getInputStreamFromFileName( (String) invocationOnMock.getArguments()[ 0 ] );
        }
      } );
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    return readAccess;
  }

  @Override
  public IRWAccess getPluginSystemWriter( String s ) {
    return mockedRWAccess;
  }

  @Override
  public IReadAccess getOtherPluginSystemReader( String s, String s2 ) {
    return mockedReadAccess;
  }

  @Override
  public IRWAccess getOtherPluginSystemWriter( String s, String s2 ) {
    return null;
  }

  private List<IBasicFile> listBasicFiles( String path ) {
    File files = new File( path );
    List<IBasicFile> basicFiles = new ArrayList<IBasicFile>();
    for ( File file : files.listFiles() ) {
      if ( file.isDirectory() ) {
        basicFiles.addAll( listBasicFiles( file.getAbsolutePath() ) );
      } else {
        basicFiles.add( RenderApiTest.getBasicFileFromFile( file ) );
      }
    }
    return basicFiles;
  }

}
