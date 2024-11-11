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
    packages = new HashMap<>();
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
    manager.registerPackage( StdPackages.COMPONENT_SNIPPETS, createSnippetPackage( factory, urlProvider ) );
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

      final PathOrigin origin = new StaticSystemOrigin( "" );

      manager.registerPackage( StdPackages.EDITOR_JS_INCLUDES, PackageType.JS );
      if ( props.containsKey( StdPackages.EDITOR_JS_INCLUDES ) ) {
        final DependenciesPackage scripts = manager.getPackage( StdPackages.EDITOR_JS_INCLUDES );

        registerProperties( scripts, origin, props.get( StdPackages.EDITOR_JS_INCLUDES ) );
      }

      manager.registerPackage( StdPackages.EDITOR_CSS_INCLUDES, PackageType.CSS );
      if ( props.containsKey( StdPackages.EDITOR_CSS_INCLUDES ) ) {
        final DependenciesPackage styles = manager.getPackage( StdPackages.EDITOR_CSS_INCLUDES );

        registerProperties( styles, origin, props.get( StdPackages.EDITOR_CSS_INCLUDES ) );
      }
    } catch ( IOException e ) {
      logger.error( "Error attempting to read " + INCLUDES_PROP, e );
    }

    return manager;
  }

  private static DependenciesPackage createSnippetPackage( IContentAccessFactory factory, IUrlProvider urlProvider ) {
    return new DependenciesPackage( StdPackages.COMPONENT_SNIPPETS, PackageType.JS, factory, urlProvider );
  }

  /**
   * instantiate and register resources from MetaModel
   */
  private static DependenciesManager createDependencyManager( MetaModel metaModel ) {
    long start = System.currentTimeMillis();

    DependenciesManager depManager = createInstance();

    for ( ComponentType compType : metaModel.getComponentTypes() ) {
      // Custom components that support legacy dashboards must register resources.
      if ( compType instanceof CustomComponentType && !compType.supportsLegacy() ) {
        continue;
      }

      // General Resources
      for ( Resource resource : compType.getResources() ) {
        final String name = resource.getName();

        final DependenciesPackage pack = getResourceDependencyPackage( depManager, resource );
        if ( pack != null ) {
          final String version = resource.getVersion();
          final String source = resource.getSource();

          final boolean isRawType = resource.getType() == Resource.Type.RAW;
          if ( isRawType ) {
            try {
              pack.registerRawDependency( name, version, source );
            } catch ( Exception ex ) {
              logger.error( "Failed to register code fragment '" + source + "'" );
            }
          } else {
            try {
              pack.registerFileDependency( name, version, resource.getOrigin(), source );
            } catch ( Exception ex ) {
              logger.error( "Failed to register dependency '" + source + "'" );
            }
          }
        }
      }

      // Implementation
      final String compImplementation = compType.getImplementationPath();
      if ( StringUtils.isNotEmpty( compImplementation ) ) {
        final String compName = compType.getName();
        final String compVersion = compType.getName();
        final PathOrigin compOrigin = compType.getOrigin();

        try {
          final DependenciesPackage componentScripts = depManager.getPackage( StdPackages.COMPONENT_DEF_SCRIPTS );

          componentScripts.registerFileDependency( compName, compVersion, compOrigin, compImplementation );
        } catch ( Exception e ) {
          logger.error( "Failed to register dependency '" + compImplementation + "'" );
        }
      }
    }

    // read resources/include.properties
    Properties extraProps = new Properties();
    try {
      InputStream in = null;
      try {
        in = CdeEnvironment.getPluginSystemReader().getFileInputStream( EXTRA_INCLUDES_PROP );

        extraProps.load( in );
      } finally {
        IOUtils.closeQuietly( in );
      }

      final PathOrigin origin = new StaticSystemOrigin( "" );

      if ( extraProps.containsKey( StdPackages.EDITOR_JS_INCLUDES ) ) {
        final DependenciesPackage scripts = depManager.getPackage( StdPackages.COMPONENT_DEF_SCRIPTS );

        registerProperties( scripts, origin, extraProps.get( StdPackages.EDITOR_JS_INCLUDES ) );
      }

      if ( extraProps.containsKey( StdPackages.EDITOR_CSS_INCLUDES ) ) {
        final DependenciesPackage styles = depManager.getPackage( StdPackages.COMPONENT_STYLES );

        registerProperties( styles, origin, extraProps.get( StdPackages.EDITOR_CSS_INCLUDES ) );
      }
    } catch ( IOException e ) {
      logger.error( "Error attempting to read " + EXTRA_INCLUDES_PROP, e );
    }

    if ( logger.isDebugEnabled() ) {
      logger.debug( String.format( "Registered meta model dependencies in %ss", Utils.ellapsedSeconds( start ) ) );
    }

    return depManager;
  }

  private static DependenciesPackage getResourceDependencyPackage( DependenciesManager manager, Resource resource ) {
    final Resource.Type type = resource.getType();
    if ( type == Resource.Type.RAW ) {
      return manager.getPackage( StdPackages.COMPONENT_SNIPPETS );
    }

    if ( type == Resource.Type.SCRIPT ) {
      final String app = resource.getApp();

      final boolean isComponentScripts = StdPackages.COMPONENT_DEF_SCRIPTS.equals( app );
      if ( StringUtils.isEmpty( app ) || isComponentScripts ) {
        return manager.getPackage( StdPackages.COMPONENT_DEF_SCRIPTS );
      }

      if ( StdPackages.CDFDD.equals( app ) ) {
        return manager.getPackage( StdPackages.CDFDD );
      }
    }

    if ( type == Resource.Type.STYLE ) {
      return manager.getPackage( StdPackages.COMPONENT_STYLES );
    }

    return null;
  }

  private static void registerProperties( DependenciesPackage depPackage, PathOrigin origin, Object properties ) {
    for ( String path : properties.toString().split( "," ) ) {
      if ( !path.isEmpty() ) {
        depPackage.registerFileDependency( path, null, origin, path );
      }
    }
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
    final IContentAccessFactory cdeContentFactory = CdeEnvironment.getContentAccessFactory();
    final IUrlProvider urlProvider = CdeEngine.getEnv().getUrlProvider();

    return registerPackage( id, new DependenciesPackage( id, type, cdeContentFactory, urlProvider ) );
  }
}
