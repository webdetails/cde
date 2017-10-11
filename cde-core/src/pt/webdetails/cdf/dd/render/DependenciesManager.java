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

package pt.webdetails.cdf.dd.render;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.MetaModelManager;
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.meta.CustomComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.Resource;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.packager.DependenciesPackage;
import pt.webdetails.cpf.packager.DependenciesPackage.PackageType;
import pt.webdetails.cpf.packager.origin.PathOrigin;
import pt.webdetails.cpf.packager.origin.StaticSystemOrigin;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;

public final class DependenciesManager {
  private static Log logger = LogFactory.getLog( DependenciesManager.class );

  private static final String INCLUDES_PROP = "editor.includes.properties";

  private static final String EXTRA_INCLUDES_PROP = "render.includes.properties";

  private static DependenciesManager manager;
  private final HashMap<String, DependenciesPackage> packages;

  private DependenciesManager() {
    packages = new HashMap<String, DependenciesPackage>();
  }

  public static synchronized DependenciesManager getInstance() {
    if ( manager == null ) {
      manager = createDependencyManager( MetaModelManager.getInstance().getModel() );
    }
    return manager;
  }

  public static final class StdPackages {
    public static final String COMPONENT_DEF_SCRIPTS = "CDF";
    public static final String COMPONENT_STYLES = "CDF-CSS";
    public static final String COMPONENT_SNIPPETS = "CDF-RAW";
    public static final String CDFDD = "CDFDD";

    public static final String EDITOR_JS_INCLUDES = "scripts";
    public static final String EDITOR_CSS_INCLUDES = "styles";
  }

  /**
   * Force re-registration of resources.
   */
  public static synchronized void refresh() {
    manager = null;
  }

  /**
   * instantiation and basic init
   */
  private static DependenciesManager createInstance() {
    DependenciesManager manager = new DependenciesManager();
    IUrlProvider urlProvider = CdeEngine.getEnv().getUrlProvider();
    IContentAccessFactory factory = CdeEnvironment.getContentAccessFactory();
    manager.registerPackage( StdPackages.COMPONENT_STYLES, PackageType.CSS );
    manager.registerPackage( StdPackages.COMPONENT_DEF_SCRIPTS, PackageType.JS );
    manager.registerPackage(
      StdPackages.COMPONENT_SNIPPETS,
      new DependenciesPackage( StdPackages.COMPONENT_SNIPPETS, PackageType.JS, factory, urlProvider ) ); // TODO change
    manager.registerPackage( StdPackages.CDFDD, PackageType.JS );

    //read include.properties
    Properties props = new Properties();
    try {
      InputStream in = null;
      try {
        in = CdeEnvironment.getPluginSystemReader().getFileInputStream( INCLUDES_PROP );
        props.load( in );
      } finally {
        IOUtils.closeQuietly( in );
      }
      PathOrigin origin = new StaticSystemOrigin( "" );

      manager.registerPackage( StdPackages.EDITOR_JS_INCLUDES, PackageType.JS );
      DependenciesPackage scripts = manager.getPackage( StdPackages.EDITOR_JS_INCLUDES );
      if ( props.containsKey( "scripts" ) ) {
        for ( String path : props.get( "scripts" ).toString().split( "," ) ) {
          if ( !path.isEmpty() ) {
            scripts.registerFileDependency( path, null, origin, path );
          }
        }
      }

      manager.registerPackage( StdPackages.EDITOR_CSS_INCLUDES, PackageType.CSS );
      DependenciesPackage styles = manager.getPackage( StdPackages.EDITOR_CSS_INCLUDES );
      if ( props.containsKey( "styles" ) ) {
        for ( String path : props.get( "styles" ).toString().split( "," ) ) {
          if ( !path.isEmpty() ) {
            styles.registerFileDependency( path, null, origin, path );
          }
        }
      }
    } catch ( IOException e ) {
      logger.error( "Error attempting to read " + INCLUDES_PROP, e );
    }

    return manager;
  }

  /**
   * instantiate and register resources from MetaModel
   */
  private static DependenciesManager createDependencyManager( MetaModel metaModel ) {
    long start = System.currentTimeMillis();
    DependenciesManager depMgr = createInstance();

    DependenciesPackage componentScripts = depMgr.getPackage( StdPackages.COMPONENT_DEF_SCRIPTS );
    DependenciesPackage componentSnippets = depMgr.getPackage( StdPackages.COMPONENT_SNIPPETS );
    DependenciesPackage componentStyles = depMgr.getPackage( StdPackages.COMPONENT_STYLES );

    DependenciesPackage ddScripts = depMgr.getPackage( StdPackages.CDFDD );

    for ( ComponentType compType : metaModel.getComponentTypes() ) {
      // Custom components that support legacy dashboards must register resources.
      if ( compType instanceof CustomComponentType && !compType.supportsLegacy() ) {
        continue;
      }
      // General Resources
      for ( Resource res : compType.getResources() ) {
        Resource.Type resType = res.getType();
        if ( resType == Resource.Type.RAW ) {
          try {
            componentSnippets.registerRawDependency( res.getName(), res.getVersion(), res.getSource() );
          } catch ( Exception ex ) {
            logger.error( "Failed to register code fragment '" + res.getSource() + "'" );
          }
        } else {
          DependenciesPackage pack = null;
          if ( resType == Resource.Type.SCRIPT ) {
            String app = res.getApp();
            if ( StringUtils.isEmpty( app ) || app.equals( StdPackages.COMPONENT_DEF_SCRIPTS ) ) {
              pack = componentScripts;
            } else if ( app.equals( StdPackages.CDFDD ) ) {
              pack = ddScripts;
            }
          } else if ( resType == Resource.Type.STYLE ) {
            pack = componentStyles;
          }

          if ( pack != null ) {
            try {
              pack.registerFileDependency( res.getName(), res.getVersion(), res.getOrigin(), res.getSource() );
            } catch ( Exception ex ) {
              logger.error( "Failed to register dependency '" + res.getSource() + "'" );
            }
          }
        }
      }

      // Implementation
      PathOrigin origin = compType.getOrigin();
      String srcImpl = compType.getImplementationPath();
      if ( StringUtils.isNotEmpty( srcImpl ) ) {
        try {
          componentScripts.registerFileDependency( compType.getName(), compType.getVersion(), origin, srcImpl );
        } catch ( Exception e ) {
          logger.error( "Failed to register dependency '" + srcImpl + "'" );
        }
      }
    }

    //read resources/include.properties
    Properties extraProps = new Properties();
    try {
      InputStream in = null;
      try {
        in = CdeEnvironment.getPluginSystemReader().getFileInputStream( EXTRA_INCLUDES_PROP );
        extraProps.load( in );
      } finally {
        IOUtils.closeQuietly( in );
      }
      PathOrigin origin = new StaticSystemOrigin( "" );

      DependenciesPackage scripts = depMgr.getPackage( StdPackages.COMPONENT_DEF_SCRIPTS );
      if ( extraProps.containsKey( "scripts" ) ) {
        for ( String path : extraProps.get( "scripts" ).toString().split( "," ) ) {
          if ( !path.isEmpty() ) {
            scripts.registerFileDependency( path, null, origin, path );
          }
        }
      }

      DependenciesPackage styles = depMgr.getPackage( StdPackages.COMPONENT_STYLES );
      if ( extraProps.containsKey( "styles" ) ) {
        for ( String path : extraProps.get( "styles" ).toString().split( "," ) ) {
          if ( !path.isEmpty() ) {
            styles.registerFileDependency( path, null, origin, path );
          }
        }
      }
    } catch ( IOException e ) {
      logger.error( "Error attempting to read " + EXTRA_INCLUDES_PROP, e );
    }

    if ( logger.isDebugEnabled() ) {
      logger.debug( String.format( "Registered meta model dependencies in %ss", Utils.ellapsedSeconds( start ) ) );
    }
    return depMgr;
  }

  //TODO: unexpose file registration
  public DependenciesPackage getPackage( String id ) {
    return packages.get( id );
  }

  /**
   * @return if there was a package already registered for that id
   */
  public boolean registerPackage( String id, DependenciesPackage pkg ) {
    return packages.put( id, pkg ) != null;
  }

  public boolean registerPackage( String id, PackageType type ) {
    IUrlProvider urlProvider = CdeEngine.getEnv().getUrlProvider();
    return registerPackage( id,
      new DependenciesPackage( id, type, CdeEnvironment.getContentAccessFactory(), urlProvider ) );
  }
}
