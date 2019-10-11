/*!
 * Copyright 2002 - 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.packager.origin.PathOrigin;
import pt.webdetails.cpf.packager.origin.PluginRepositoryOrigin;
import pt.webdetails.cpf.packager.origin.StaticSystemOrigin;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.PluginSettings;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.CharsetHelper;

public class CdeSettings {

  protected static Log logger = LogFactory.getLog( CdeSettings.class );

  public enum FolderType { STATIC, REPO }

  private CdeSettings() {
  }

  private static CdfDDSettings settings = new CdfDDSettings();

  public static CdfDDSettings getSettings() {
    return settings;
  }

  //<path origin="psystem"><>
  //  public LocationSet getComponentLocations() {
  //
  //  }
  public static Collection<PathOrigin> getCustomComponentLocations() {
    return settings.getComponentLocations();
  }

  /**
   * @deprecated
   */
  public static IReadAccess[] getComponentLocations() {
    ArrayList<IReadAccess> componentAccesses = new ArrayList<>();
    CdfDDSettings settings = getSettings();
    for ( Element element : settings.getComponentLocationElements() ) {
      String path = element.getText();

      if ( path != null ) {
        path = StringUtils.strip( path, "/" );

        //ex: <path>system/pentaho-cdf-dd/resources/custom/components</path>, <path>system/cdc/cdeComponents</path>
        if ( path.startsWith( CdeEnvironment.getSystemDir() + "/" ) ) {
          path = path.replaceFirst( CdeEnvironment.getSystemDir() + "/", "" );

          // ex: <path>system/pentaho-cdf-dd/resources/custom/components</path>
          if ( path.startsWith( CdeEnvironment.getPluginId() + "/" ) ) {
            path = path.replaceFirst( CdeEnvironment.getPluginId() + "/", "" );

            if ( CdeEnvironment.getPluginSystemReader().fileExists( path ) && CdeEnvironment.getPluginSystemReader()
              .fetchFile( path ).isDirectory() ) {
              componentAccesses.add( CdeEnvironment.getPluginSystemReader( path ) );
            }
          } else {
            //XXX this isn't supposed to happen
            // ex: <path>system/cdc/cdeComponents</path>
            String pluginId = path.substring( 0, path.indexOf( "/" ) );
            path = path.replaceFirst( pluginId + "/", "" );

            if ( CdeEnvironment.getOtherPluginSystemReader( pluginId ).fileExists( path ) && CdeEnvironment
              .getOtherPluginSystemReader( pluginId ).fetchFile( path ).isDirectory() ) {
              componentAccesses.add( CdeEnvironment.getOtherPluginSystemReader( pluginId, path ) );
            }
          }
        } else if ( path.startsWith( CdeEnvironment.getPluginRepositoryDir() + "/" ) ) {
          // ex: <path>cde/components</path>
          path = path.replaceFirst( CdeEnvironment.getPluginRepositoryDir() + "/", "" );

          if ( CdeEnvironment.getPluginSystemReader().fileExists( path ) && CdeEnvironment.getPluginSystemReader()
            .fetchFile( path ).isDirectory() ) {
            componentAccesses.add( CdeEnvironment.getPluginRepositoryReader( path ) );
          }
        } else {
          logger.warn( "Components directory '" + element.getText() + "' was not found." );
        }
      }
    }
    return componentAccesses.toArray( new IReadAccess[ componentAccesses.size() ] );
  }


  public static String[] getFilePickerHiddenFolderPaths( FolderType folderType ) {
    List<String> paths = new ArrayList<>();

    // method IBasicFile[] getFilePickerHiddenFolders( folderType ) is already validating if folders exist
    IBasicFile[] files = getFilePickerHiddenFolders( folderType );

    if ( files != null ) {

      for ( IBasicFile file : files ) {
        paths.add( file.getFullPath() );
      }
    }

    return paths.toArray( new String[ files == null? 0:files.length ] );
  }

  public static IBasicFile[] getFilePickerHiddenFolders( FolderType folderType ) {
    String[] paths = getSettings().getFilePickerHiddenFoldersByType( folderType );
    List<IBasicFile> files = new ArrayList<>();

    if ( paths != null ) {
      for ( String path : paths ) {
        IReadAccess access = Utils.getAppropriateReadAccess( path );

        if ( access != null && access.fileExists( path ) && access.fetchFile( path ).isDirectory() ) {
          files.add( access.fetchFile( path ) );
        } else {
          logger.error( "Discarding path '" + path + "': file does not exist or isn't a directory." );
        }
      }
    }

    return files.toArray( new IBasicFile[ files.size() ] );
  }

  public static String getEncoding() {
    return CharsetHelper.getEncoding();
  }

  public static class CdfDDSettings extends PluginSettings {

    public CdfDDSettings( IRWAccess writeAccess ) {
      super( writeAccess ); // useful when unit testing / mocking
    }

    public CdfDDSettings() {
      super( CdeEnvironment.getPluginSystemWriter() );
    }

    List<Element> getComponentLocationElements() {
      return getSettingsXmlSection( "custom-components/path" );
    }

    public List<PathOrigin> getComponentLocations() {
      List<Element> pathElements = getSettingsXmlSection( "custom-components/path" );
      ArrayList<PathOrigin> locations = new ArrayList<PathOrigin>();
      for ( Element pathElement : pathElements ) {
        String path = StringUtils.strip( pathElement.getTextTrim() );
        String origin = pathElement.attributeValue( "origin" );
        if ( !StringUtils.isEmpty( origin ) ) {
          if ( StringUtils.equals( origin, "static" ) ) {
            locations.add( new StaticSystemOrigin( path ) );
          } else if ( StringUtils.equals( origin, "repo" ) ) {
            locations.add( new PluginRepositoryOrigin( CdeEngine.getEnv().getPluginRepositoryDir(), path ) );
          }
        } else {
          logger.error( "Must specify origin (static|repo), location '" + path + " 'ignored." );
        }
      }
      return locations;
    }

    public String[] getFilePickerHiddenFoldersByType( CdeSettings.FolderType folderType ) {
      List<String> hiddenFolders = new ArrayList<String>();
      List<Element> xmlPathElements = getSettingsXmlSection( "file-picker/hidden-folders/path" );

      if ( xmlPathElements != null ) {
        for ( Element xmlPathElement : xmlPathElements ) {
          String path = StringUtils.strip( xmlPathElement.getTextTrim() );
          String origin = xmlPathElement.attributeValue( "origin" );

          if ( StringUtils.isEmpty( path ) || StringUtils.isEmpty( origin ) ) {
            logger.error( "Must specify origin (static|repo) and location '" + path + "." );
            continue;
          }

          if ( folderType == FolderType.valueOf( origin.toUpperCase() ) ) {
            hiddenFolders.add( path );
          }
        }
      }

      return hiddenFolders.toArray( new String[ hiddenFolders.size() ] );
    }
  }
}
