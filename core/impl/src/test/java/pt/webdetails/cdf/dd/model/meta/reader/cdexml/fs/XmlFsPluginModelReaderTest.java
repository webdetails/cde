/*!
 * Copyright 2002 - 2024 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.model.meta.reader.cdexml.fs;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import pt.webdetails.cdf.dd.CdeEngineForTests;
import pt.webdetails.cdf.dd.ICdeEnvironment;
import pt.webdetails.cdf.dd.IPluginResourceLocationManager;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.packager.origin.PathOrigin;
import pt.webdetails.cpf.packager.origin.StaticSystemOrigin;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XmlFsPluginModelReaderTest {

  private static XmlFsPluginModelReader modelReader;
  private static XmlFsPluginThingReaderFactory factory;
  private static IReadAccess mockedReadAccess;

  private static final String TEST_DIR = Utils.joinPath( System.getProperty( "user.dir" ), "src/test" );
  private static final String RESOURCE_DIR = Utils.joinPath( TEST_DIR, "resources" );
  private static final String DUMMY_XML =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><DesignerComponent></DesignerComponent>";
  private static final String PROPERTIES_MATCHER = "properties";

  @Before
  public void setUp() throws Exception {
    mockedReadAccess = mock( IReadAccess.class );
    when( mockedReadAccess.listFiles( any(), Mockito.<IBasicFileFilter>any(), anyInt() ) ).thenAnswer(
      new Answer<Object>() {
        @Override
        public Object answer( InvocationOnMock invocation ) throws Throwable {
          return buildFileList();
        }
      } );

    IContentAccessFactory mockedContentAccessFactory = mock( IContentAccessFactory.class );
    when( mockedContentAccessFactory.getPluginSystemReader( any() ) ).thenAnswer( new Answer<Object>() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        String path = (String) invocation.getArguments()[ 0 ];

        if ( path.equals( XmlFsPluginModelReader.COMPONENTS_DIR ) ) {
          return buildReadAccessMock( Utils.joinPath( RESOURCE_DIR, XmlFsPluginModelReader.COMPONENTS_DIR ) );
        } else if ( path.equals( XmlFsPluginModelReader.PROPERTIES_DIR ) ) {
          return buildReadAccessMock( Utils.joinPath( RESOURCE_DIR, XmlFsPluginModelReader.PROPERTIES_DIR ) );
        } else {
          return mockedReadAccess;
        }
      }
    } );
    when( mockedContentAccessFactory.getPluginRepositoryReader( any() ) ).thenReturn( mockedReadAccess );

    IPluginResourceLocationManager mockedPluginResourceLocationManager = mock( IPluginResourceLocationManager.class );
    List<PathOrigin> pathOrigins = new ArrayList<>();
    pathOrigins.add( new StaticSystemOrigin( Utils.joinPath( RESOURCE_DIR, XmlFsPluginModelReader.COMPONENTS_DIR ) ) );
    when( mockedPluginResourceLocationManager.getCustomComponentsLocations() ).thenReturn( pathOrigins );

    ICdeEnvironment mockedEnvironment = mock( ICdeEnvironment.class );
    when( mockedEnvironment.getContentAccessFactory() ).thenReturn( mockedContentAccessFactory );
    when( mockedEnvironment.getPluginResourceLocationManager() ).thenReturn( mockedPluginResourceLocationManager );

    new CdeEngineForTests( mockedEnvironment );
    factory = new XmlFsPluginThingReaderFactory( mockedEnvironment.getContentAccessFactory() );
    modelReader = new XmlFsPluginModelReader( mockedContentAccessFactory, false );
  }

  @Test
  public void testReadOrder() {
    try {
      MetaModel.Builder builder = modelReader.read( factory );
      MetaModel model = builder.build();
      List<List<ComponentType>> splitComponentTypes = buildComponentTypes( model.getComponentTypes() );

      if ( model.getComponentTypeCount() == 0 ) {
        fail( "Couldn't read ComponentTypes" );
      }
      if ( model.getPropertyTypeCount() == 0 ) {
        fail( "Couldn't read PropertyTypes" );
      }

      for ( List<ComponentType> compTypes : splitComponentTypes ) {
        String oldSourcePath = "";
        for ( ComponentType comp : compTypes ) {
          String currentPath = comp.getSourcePath().toLowerCase();
          assertTrue( currentPath.compareTo( oldSourcePath ) >= 0 );
          oldSourcePath = currentPath;
        }
      }
      List<List<PropertyType>> splitPropertyTypes = buildPropertyTypes( model.getPropertyTypes() );

      for ( List<PropertyType> propTypes : splitPropertyTypes ) {
        String oldSourcePath = "";
        for ( PropertyType prop : propTypes ) {
          String currentPath = prop.getSourcePath().toLowerCase();
          assertTrue( currentPath.compareTo( oldSourcePath ) >= 0 );
          oldSourcePath = currentPath;
        }
      }

    } catch ( ThingReadException e ) {
      e.printStackTrace();
    } catch ( ValidationException e ) {
      e.printStackTrace();
    }
  }

  private List<List<PropertyType>> buildPropertyTypes( Iterable<PropertyType> propertyTypes ) {
    List<List<PropertyType>> splitPropertyTypes = new ArrayList<List<PropertyType>>();
    List<PropertyType> properties = new ArrayList<PropertyType>();

    for ( PropertyType prop : propertyTypes ) {
      if ( prop.getSourcePath().contains( PROPERTIES_MATCHER ) ) {
        properties.add( prop );
      }
    }
    splitPropertyTypes.add( properties );
    return splitPropertyTypes;
  }

  private List<List<ComponentType>> buildComponentTypes( Iterable<ComponentType> componentTypes ) {
    List<List<ComponentType>> splitComponentTypes = new ArrayList<>();
    List<String> pathOrigins = new ArrayList<>();

    for ( ComponentType comp : componentTypes ) {
      if ( !pathOrigins.contains( comp.getOrigin().toString() ) ) {
        pathOrigins.add( comp.getOrigin().toString() );
      }
    }
    for ( String origin : pathOrigins ) {
      List<ComponentType> compTypes = new ArrayList<>();
      for ( ComponentType comp : componentTypes ) {
        if ( origin.equals( comp.getOrigin().toString() ) ) {
          compTypes.add( comp );
        }
      }
      splitComponentTypes.add( compTypes );
    }
    return splitComponentTypes;
  }

  private InputStream buildFileInputStream() {
    return new ByteArrayInputStream( DUMMY_XML.getBytes() );
  }

  private List<IBasicFile> buildFileList() throws IOException {
    IBasicFile foo = mock( IBasicFile.class );
    when( foo.getContents() ).thenReturn( buildFileInputStream() );
    when( foo.getFullPath() ).thenReturn( "path/to/foo" );
    List<IBasicFile> fileList = new ArrayList<>();
    fileList.add( foo );
    return fileList;
  }

  private IReadAccess buildReadAccessMock( String path ) {
    IReadAccess mocked = mock( IReadAccess.class );
    List<IBasicFile> fileList = listFromFileSystem( path );
    when( mocked.listFiles( any(), Mockito.<IBasicFileFilter>any(), anyInt() ) ).thenReturn( fileList );
    when( mocked.listFiles( any(), Mockito.<IBasicFileFilter>any(), anyInt(), anyBoolean(), anyBoolean() ) )
      .thenReturn( fileList );
    return mocked;
  }

  private List<IBasicFile> listFromFileSystem( String path ) {
    List<IBasicFile> fileList = new ArrayList<IBasicFile>();
    File file = new File( path );
    if ( file.exists() && file.isDirectory() ) {
      basicFileListFromFileArray( file.listFiles(), fileList );
    }
    return fileList;
  }

  private void basicFileListFromFileArray( File[] files, List<IBasicFile> fileList ) {
    for ( File file : files ) {
      if ( !file.isDirectory() ) {
        fileList.add( basicFileFromFile( file ) );
      } else {
        basicFileListFromFileArray( file.listFiles(), fileList );
      }
    }
  }

  private IBasicFile basicFileFromFile( final File file ) {
    return new IBasicFile() {
      @Override
      public InputStream getContents() throws IOException {
        return new FileInputStream( file );
      }

      @Override
      public String getName() {
        return file.getName();
      }

      @Override
      public String getFullPath() {
        return file.getAbsolutePath();
      }

      @Override
      public String getPath() {
        return file.getPath();
      }

      @Override
      public String getExtension() {
        return FilenameUtils.getExtension( getName() );
      }

      @Override
      public boolean isDirectory() {
        return file.isDirectory();
      }
    };
  }
}
