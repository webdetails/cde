/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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
import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.model.meta.reader.cdexml.XmlAdhocComponentTypeReader;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.meta.CustomComponentType;
import pt.webdetails.cdf.dd.model.meta.PrimitiveComponentType;
import pt.webdetails.cdf.dd.model.meta.WidgetComponentType;
import pt.webdetails.cpf.packager.origin.PluginRepositoryOrigin;
import pt.webdetails.cpf.packager.origin.RepositoryPathOrigin;
import pt.webdetails.cpf.packager.origin.StaticSystemOrigin;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.GenericBasicFileFilter;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.packager.origin.PathOrigin;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;

/**
 * Loads XML model files, component types and property types, from the file system, of a Pentaho CDE plugin
 * installation.
 */
public final class XmlFsPluginModelReader {

  public static final String RESOURCES_DIR = "resources";

  public static final String DEF_BASE_TYPE = PrimitiveComponentType.class.getSimpleName();

  public static final String BASE_DIR = Utils.joinPath( RESOURCES_DIR, "base" );
  public static final String BASE_PROPS_DIR = Utils.joinPath( BASE_DIR, "properties" );
  public static final String BASE_COMPS_DIR = Utils.joinPath( BASE_DIR, "components" );

  public static final String DEF_CUSTOM_TYPE = CustomComponentType.class.getSimpleName();
  public static final String CUSTOM_DIR = Utils.joinPath( RESOURCES_DIR, "custom" );
  public static final String CUSTOM_PROPS_DIR = Utils.joinPath( CUSTOM_DIR, "properties" );

  public static final String DEF_WIDGET_STUB_TYPE = WidgetComponentType.class.getSimpleName();
  public static final String WIDGETS_DIR = "widgets";

  public static final String CUSTOM_PROPS_FILENAME = "property";
  public static final String COMPONENT_FILENAME = "component";
  // extension for component properties definitions
  public static final String DEFINITION_FILE_EXT = "xml";

  protected static final Log logger = LogFactory.getLog( XmlFsPluginModelReader.class );

  // -----------

  private final Boolean continueOnError;

  private IContentAccessFactory contentAccessFactory;

  public XmlFsPluginModelReader( boolean continueOnError ) { //the real ctor, only usage calls with false
    this.continueOnError = continueOnError;
  }

  public XmlFsPluginModelReader( IContentAccessFactory caf, boolean continueOnError ) {
    this( false );
    contentAccessFactory = caf;
  }

  /**
   * Reads properties, components and widgets
   *
   * @return model with component and property types
   * @throws ThingReadException
   */
  public MetaModel.Builder read( XmlFsPluginThingReaderFactory factory ) throws ThingReadException {
    MetaModel.Builder model = new MetaModel.Builder();

    // Read Properties
    this.readBaseProperties( model, factory );
    this.readCustomProperties( model, factory );

    // Read Components
    logger.info( String.format( "Loading BASE components from: %s", BASE_COMPS_DIR ) );
    this.readBaseComponents( model, factory );
    this.readCustomComponents( model, factory );
    this.readWidgetStubComponents( model, factory );

    return model;
  }

  private void readBaseComponents( MetaModel.Builder model, XmlFsPluginThingReaderFactory factory )
    throws ThingReadException {
    List<IBasicFile> filesList = CdeEnvironment.getPluginSystemReader( BASE_COMPS_DIR ).listFiles( null,
      new GenericBasicFileFilter( null, DEFINITION_FILE_EXT ), IReadAccess.DEPTH_ALL );
    PathOrigin origin = new StaticSystemOrigin( BASE_COMPS_DIR );

    if ( filesList != null ) {
      IBasicFile[] filesArray = filesList.toArray( new IBasicFile[] {} );
      Arrays.sort( filesArray, getFileComparator() );
      for ( IBasicFile file : filesArray ) {
        this.readComponentsFile( model, factory, file, DEF_BASE_TYPE, origin );
      }
    }
  }

  private void readBaseProperties( MetaModel.Builder model, XmlFsPluginThingReaderFactory factory )
    throws ThingReadException {

    logger.info( String.format( "Loading BASE properties from: %s", BASE_PROPS_DIR ) );

    List<IBasicFile> filesList = CdeEnvironment.getPluginSystemReader( BASE_PROPS_DIR ).listFiles( null,
      new GenericBasicFileFilter( null, DEFINITION_FILE_EXT ), IReadAccess.DEPTH_ALL );

    if ( filesList != null ) {
      IBasicFile[] filesArray = filesList.toArray( new IBasicFile[] {} );
      Arrays.sort( filesArray, getFileComparator() );
      for ( IBasicFile file : filesArray ) {
        this.readPropertiesFile( model, factory, file );
      }
    }
  }

  private void readCustomProperties( MetaModel.Builder model, XmlFsPluginThingReaderFactory factory )
    throws ThingReadException {

    logger.info( String.format( "Loading CUSTOM properties from: %s", CUSTOM_PROPS_DIR ) );

    List<IBasicFile> filesList = CdeEnvironment.getPluginSystemReader( CUSTOM_PROPS_DIR ).listFiles( null,
      new GenericBasicFileFilter( CUSTOM_PROPS_FILENAME, DEFINITION_FILE_EXT ), IReadAccess.DEPTH_ALL );

    if ( filesList != null ) {
      IBasicFile[] filesArray = filesList.toArray( new IBasicFile[] {} );
      Arrays.sort( filesArray, getFileComparator() );
      for ( IBasicFile file : filesArray ) {
        this.readPropertiesFile( model, factory, file );
      }
    }
  }

  private void readPropertiesFile( MetaModel.Builder model, XmlFsPluginThingReaderFactory factory, IBasicFile file )
    throws ThingReadException {
    Document doc;
    try {
      doc = Utils.getDocFromFile( file, null );
    } catch ( Exception ex ) {
      ThingReadException ex2 = new ThingReadException( "Cannot read properties file '" + file + "'.", ex );
      if ( !this.continueOnError ) {
        throw ex2;
      }

      // log and move on
      logger.fatal( null, ex2 );
      return;
    }

    // One file can contain multiple definitions.
    @SuppressWarnings( "unchecked" )
    List<Element> propertieElems = Utils.selectElements( doc, "//DesignerProperty" );
    for ( Element propertyElem : propertieElems ) {
      readProperty( model, factory, propertyElem, file );
    }
  }

  private void readProperty(
    MetaModel.Builder modelBuilder,
    XmlFsPluginThingReaderFactory factory,
    Element propertyElem,
    IBasicFile file ) {
    try {
      PropertyType.Builder prop = factory.getPropertyTypeReader().read( propertyElem, file.getPath() );
      modelBuilder.addProperty( prop );
    } catch ( IllegalArgumentException ex ) {
      if ( !this.continueOnError ) {
        throw ex;
      }

      // Just log and move on
      logger.fatal( "Failed to read property ", ex );
    }
  }

  private void readCustomComponents( MetaModel.Builder model, XmlFsPluginThingReaderFactory factory )
    throws ThingReadException {
    for ( PathOrigin origin : CdeEnvironment.getPluginResourceLocationManager().getCustomComponentsLocations() ) {
      readCustomComponentsLocation( model, factory, origin );
    }
  }

  private void readCustomComponentsLocation( MetaModel.Builder model, XmlFsPluginThingReaderFactory factory,
                                             PathOrigin origin ) throws ThingReadException {
    logger.info( "reading custom components from " + origin );

    GenericBasicFileFilter filter = new GenericBasicFileFilter( COMPONENT_FILENAME, DEFINITION_FILE_EXT );
    IReadAccess access = origin.getReader( contentAccessFactory );
    List<IBasicFile> filesList = access.listFiles( null, filter, IReadAccess.DEPTH_ALL, false, true );

    if ( filesList != null ) {
      logger.debug( String.format( "%d sub-folders found", filesList.size() ) );
      IBasicFile[] filesArray = filesList.toArray( new IBasicFile[] {} );
      Arrays.sort( filesArray, getFileComparator() );
      for ( IBasicFile file : filesArray ) {
        this.readComponentsFile( model, factory, file, DEF_CUSTOM_TYPE, origin );
      }
    }
  }

  private void readWidgetStubComponents( MetaModel.Builder model, XmlFsPluginThingReaderFactory factory )
    throws ThingReadException {
    Document doc = null;
    IBasicFile cdeXml = CdeEngine.getInstance().getEnvironment().getCdeXml();
    try {
      if ( cdeXml != null ) {
        doc = Utils.getDocFromFile( cdeXml, null );
      }
    } catch ( Exception e ) {
      String msg = "Cannot read components file 'cde.xml'.";

      if ( !this.continueOnError ) {
        throw new ThingReadException( msg, e );
      }
      // log and move on
      logger.fatal( msg, e );
      return;
    }
    if ( doc != null ) {
      List<Element> widgetLocations = Utils.selectElements( doc, "//widgetsLocations//location" );
      List<List<IBasicFile>> widgetsLists = new ArrayList<List<IBasicFile>>();
      String locations = "";
      for ( Element location : widgetLocations ) {
        try {
          List<IBasicFile> filesList;
          String path = location.getText().toLowerCase().replaceFirst( "/", "" );

          if ( path.startsWith( CdeEnvironment.getSystemDir() + "/" ) ) {
            filesList = CdeEnvironment.getPluginSystemReader().listFiles( location.getText(),
              new GenericBasicFileFilter( COMPONENT_FILENAME, DEFINITION_FILE_EXT ), IReadAccess.DEPTH_ALL, false,
              true );
          } else {
            filesList = CdeEnvironment.getUserContentAccess().listFiles( location.getText(),
              new GenericBasicFileFilter( COMPONENT_FILENAME, DEFINITION_FILE_EXT ), IReadAccess.DEPTH_ALL, false,
              true );
          }

          if ( filesList != null ) {
            widgetsLists.add( filesList );
          }
          locations = locations + location.getText() + ", ";
        } catch ( Exception e ) {
          logger.fatal( "Couldn't load widgets from: " + location.getText(), e );
        }
      }
      logger.info( String.format( "Loading WIDGET components from: %s", locations ) );
      List<String> files = new ArrayList<String>();
      if ( widgetsLists.size() > 0 ) {
        for ( List<IBasicFile> filesList : widgetsLists ) {
          for ( IBasicFile file : filesList ) {
            if ( !files.contains( file.getName() ) ) {
              files.add( file.getName() );
              fixWidgetMeta( file );
              this.readComponentsFile( model, factory, file, DEF_WIDGET_STUB_TYPE,
                new RepositoryPathOrigin(
                  FilenameUtils.getPath( file.getPath() ) ) );
            } else {
              logger.debug( "Duplicate widget, ignoring " + file.getPath() );
            }
          }
        }
      }
      return;
    }

    logger.info( String.format( "Loading WIDGET components from: %s", WIDGETS_DIR ) );

    List<IBasicFile> filesList = CdeEnvironment.getPluginRepositoryReader( WIDGETS_DIR ).listFiles( null,
      new GenericBasicFileFilter( COMPONENT_FILENAME, DEFINITION_FILE_EXT ), IReadAccess.DEPTH_ALL, false, true );
    PathOrigin widgetsOrigin = new PluginRepositoryOrigin( CdeEngine.getEnv().getPluginRepositoryDir(), WIDGETS_DIR );

    if ( filesList != null ) {
      logger.debug( String.format( "%s widget components found", filesList.size() ) );
      IBasicFile[] filesArray = filesList.toArray( new IBasicFile[] {} );
      Arrays.sort( filesArray, getFileComparator() );
      for ( IBasicFile file : filesArray ) {
        this.readComponentsFile( model, factory, file, DEF_WIDGET_STUB_TYPE, widgetsOrigin );
      }
    }

  }

  private void fixWidgetMeta( IBasicFile componentXml ) {
    Document doc = null;

    try {
      if ( CdeEnvironment.getUserContentAccess().fileExists( componentXml.getPath() ) ) {
        doc =
          Utils.getDocFromFile( CdeEnvironment.getUserContentAccess().fetchFile( componentXml.getPath() ), null );
      } else if ( CdeEnvironment.getPluginSystemReader().fileExists( componentXml.getPath() ) ) {
        doc = Utils
          .getDocFromFile( CdeEnvironment.getPluginSystemReader().fetchFile( componentXml.getPath() ), null );
      }
    } catch ( Exception e ) {
      logger.error( "Unable to check meta for " + componentXml.getPath() + ", moving on" );
      return;
    }
    List<Element> wcdfMeta = Utils.selectElements( doc, "//meta[@name='wcdf']" );
    String wcdfName = componentXml.getName().replace( ".component.xml", ".wcdf" );
    String wcdfPath = FilenameUtils.getPath( componentXml.getPath() ) + wcdfName;

    for ( Element meta : wcdfMeta ) {
      String metaText = meta.getText();

      if ( CdeEnvironment.getPluginSystemWriter().fileExists( metaText ) ) {
        wcdfPath = metaText;
      }

      if ( metaText.startsWith( "/" ) && !wcdfPath.startsWith( "/" ) ) {
        wcdfPath = "/" + wcdfPath;
      }

      if ( metaText.equals( wcdfPath ) ) {
        logger.debug( "No need to fix current wcdf meta ( " + metaText + " ) " );
      } else {
        logger.debug( "Fixing wcdf meta, was " + metaText + ", setting " + wcdfPath );
        meta.setText( wcdfPath );
        try {
          if ( CdeEnvironment.getUserContentAccess().fileExists( componentXml.getPath() ) ) {
            CdeEnvironment.getUserContentAccess()
              .saveFile( componentXml.getPath(), new ByteArrayInputStream( doc.asXML().getBytes() ) );
          } else if ( CdeEnvironment.getPluginSystemWriter().fileExists( componentXml.getPath() ) ) {
            CdeEnvironment.getPluginSystemWriter()
              .saveFile( componentXml.getPath(), new ByteArrayInputStream( doc.asXML().getBytes() ) );
          }
        } catch ( Exception e ) {
          logger.error( "Unable to fix meta for " + componentXml.getName() + ", moving on" );
        }
      }
    }
  }


  private Comparator<IBasicFile> getFileComparator() {
    return new Comparator<IBasicFile>() {
      @Override
      public int compare( IBasicFile file1, IBasicFile file2 ) {
        return file1.getFullPath().toLowerCase().compareTo( file2.getFullPath().toLowerCase() );
      }
    };
  }


  private void readComponentsFile( MetaModel.Builder model, XmlFsPluginThingReaderFactory factory, IBasicFile file,
                                   String defaultClassName, PathOrigin origin )
    throws ThingReadException {
    Document doc;
    try {
      doc = Utils.getDocFromFile( file, null );
    } catch ( Exception ex ) {
      String msg = "Cannot read components file '" + file.getFullPath() + "'.";

      if ( !this.continueOnError ) {
        throw new ThingReadException( msg, ex );
      }
      // log and move on
      logger.fatal( msg, ex );
      return;
    }

    // One file can contain multiple definitions.
    @SuppressWarnings( "unchecked" )
    List<Element> componentElems = Utils.selectElements( doc, "//DesignerComponent" );

    if ( logger.isDebugEnabled() && componentElems.size() > 0 ) {
      logger.debug( String.format( "\t%s [%s]", file.getPath(), componentElems.size() ) );
    }

    for ( Element componentElem : componentElems ) {
      readComponent( model, factory, componentElem, file.getPath(), defaultClassName, origin );
    }
  }

  private void readComponent( MetaModel.Builder model, XmlFsPluginThingReaderFactory factory, Element componentElem,
                              String sourcePath, String defaultClassName, PathOrigin origin )
    throws ThingReadException {
    String className = Utils.getNodeText( "Header/Override", componentElem );

    if ( StringUtils.isEmpty( className ) ) {
      className = defaultClassName;
    }

    String name = Utils.getNodeText( "Header/Name", componentElem );

    XmlAdhocComponentTypeReader<? extends ComponentType.Builder> reader = factory.getComponentTypeReader( className );
    if ( reader == null ) {
      String msg = "Failed to read component of class '" + className + "' and name " + name;
      if ( !this.continueOnError ) {
        throw new ThingReadException( msg );
      }
      // Just log and move on
      logger.fatal( msg );
    }

    ComponentType.Builder comp = reader.read( componentElem, origin, sourcePath );
    comp.setOrigin( origin );
    model.addComponent( comp );
  }

}
