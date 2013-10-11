/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cdf.dd;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.bean.factory.CoreBeanFactory;
import pt.webdetails.cdf.dd.bean.factory.ICdeBeanFactory;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;

public class CdeEngine {

  private static CdeEngine instance;
  protected static Log logger = LogFactory.getLog( CdeEngine.class );
  private ICdeEnvironment cdeEnv;

  private CdeEngine() {
    logger.debug( "Starting ElementEngine" );
  }

  private CdeEngine( ICdeEnvironment environment ) {
    this();
    this.cdeEnv = environment;
  }

  public static CdeEngine getInstance() {

    if ( instance == null ) {
      instance = new CdeEngine();
      try {
        initialize();
      } catch ( Exception ex ) {
        logger.fatal( "Error initializing CdeEngine: " + Util.getExceptionDescription( ex ) );
      }
    }

    return instance;
  }

  public ICdeEnvironment getEnvironment() {
    return getInstance().cdeEnv;
  }

  private static void initialize() throws InitializationException {
    if ( instance.cdeEnv == null ) {

      ICdeBeanFactory factory = new CoreBeanFactory();

      // try to get the environment from the configuration
      // will return the DefaultCdaEnvironment by default
      ICdeEnvironment env = instance.getConfiguredEnvironment( factory );

      if ( env != null ) {
        env.init( factory );
      }

      instance.cdeEnv = env;
      // XXX figure out where to put ensureBasicDirs
      instance.ensureBasicDirs();
    }
  }

  private void ensureBasicDirs() {
    IRWAccess repoBase = CdeEnvironment.getPluginRepositoryWriter();
    // TODO: better error messages
    if ( !ensureDirExists( repoBase, CdeConstants.SolutionFolders.COMPONENTS ) ) {
      logger.error( "Couldn't find or create CDE components dir." );
    }
    if ( !ensureDirExists( repoBase, CdeConstants.SolutionFolders.STYLES ) ) {
      logger.error( "Couldn't find or create CDE styles dir." );
    }
    if ( !ensureDirExists( repoBase, CdeConstants.SolutionFolders.TEMPLATES ) ) {
      logger.error( "Couldn't find or create CDE templates dir." );
    }

    // special case for widgets: copy widget samples into dir if creating dir for the first time
    if ( !repoBase.fileExists( CdeConstants.SolutionFolders.WIDGETS ) ) {
      if ( !repoBase.createFolder( CdeConstants.SolutionFolders.WIDGETS ) ) {
        logger.error( "Couldn't find or create CDE widgets dir." );
      } else {
        IReadAccess sysPluginSamples = CdeEnvironment.getPluginSystemReader( "resources/samples/" );
        saveAndClose( repoBase, Util.joinPath( CdeConstants.SolutionFolders.WIDGETS, "sample.cdfde" ),
            sysPluginSamples, "widget.cdfde" );
        saveAndClose( repoBase, "sample.wcdf", sysPluginSamples, "widget.wcdf" );
        saveAndClose( repoBase, "sample.cda", sysPluginSamples, "widget.cda" );
        saveAndClose( repoBase, "sample.component.xml", sysPluginSamples, "widget.xml" );
      }
    }

  }

  private boolean saveAndClose( IRWAccess writer, String fileOut, IReadAccess reader, String fileIn ) {
    InputStream input = null;
    try {
      input = reader.getFileInputStream( fileIn );
      return writer.saveFile( fileOut, input );
    } catch ( IOException e ) {
      logger.error( "Couldn't read " + fileIn + " in " + reader );
    } finally {
      IOUtils.closeQuietly( input );
    }
    return false;
  }

  private boolean ensureDirExists( IRWAccess access, String relPath ) {
    return access.fileExists( relPath ) || access.createFolder( relPath );
  }

  public static ICdeEnvironment getEnv() {
    return getInstance().getEnvironment();
  }

  protected synchronized ICdeEnvironment getConfiguredEnvironment( ICdeBeanFactory factory )
    throws InitializationException {

    Object obj = new CoreBeanFactory().getBean( ICdeEnvironment.class.getSimpleName() );

    if ( obj != null && obj instanceof ICdeEnvironment ) {
      return (ICdeEnvironment) obj;
    } else {
      String msg = "No bean found for ICdeEnvironment!!";
      logger.fatal( msg );
      throw new InitializationException( msg, null );
    }
  }
}
