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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;

public class CdeEngine {

  protected static CdeEngine instance;
  protected static Log logger = LogFactory.getLog( CdeEngine.class );
  protected ICdeEnvironment cdeEnv;

  protected CdeEngine() {
    logger.debug( "Starting ElementEngine" );
  }

  protected CdeEngine( ICdeEnvironment environment ) {
    this();
    this.cdeEnv = environment;
  }

  public static synchronized CdeEngine getInstance() {
    if ( instance == null ) {
      instance = new CdeEngine();
    }
    return instance;
  }

  public ICdeEnvironment getEnvironment() {
    return this.cdeEnv;
  }

  public void setEnvironment( ICdeEnvironment environment ) {
    this.cdeEnv = environment;
  }

  public void ensureBasicDirs() {
    IRWAccess repoBase = getEnvironment().getContentAccessFactory().getPluginRepositoryWriter( null );
    // TODO: better error messages
    if ( !ensureDirExists( repoBase, ".", true ) ) {
      logger.error( "Couldn't find or create CDE base dir." );
    }
    if ( !ensureDirExists( repoBase, CdeConstants.SolutionFolders.COMPONENTS, false ) ) {
      logger.error( "Couldn't find or create CDE components dir." );
    }
    if ( !ensureDirExists( repoBase, CdeConstants.SolutionFolders.STYLES, false ) ) {
      logger.error( "Couldn't find or create CDE styles dir." );
    }
    if ( !ensureDirExists( repoBase, CdeConstants.SolutionFolders.TEMPLATES, false ) ) {
      logger.error( "Couldn't find or create CDE templates dir." );
    } else {
      if ( !ensureDirExists( repoBase, CdeConstants.SolutionFolders.TEMPLATES_BLUEPRINT, false ) ) {
        logger.error( "Couldn't find or create CDE templates/blueprint dir." );
      }
      if ( !ensureDirExists( repoBase, CdeConstants.SolutionFolders.TEMPLATES_BOOTSTRAP, false ) ) {
        logger.error( "Couldn't find or create CDE templates/bootstrap dir." );
      }
    }

    // special case for widgets: copy widget samples into dir if creating dir for the first time
    if ( !repoBase.fileExists( CdeConstants.SolutionFolders.WIDGETS ) ) {
      if ( !ensureDirExists( repoBase, CdeConstants.SolutionFolders.WIDGETS, false ) ) {
        logger.error( "Couldn't find or create CDE widgets dir." );
      } else {
        IReadAccess sysPluginSamples = getEnvironment()
          .getContentAccessFactory()
          .getPluginSystemReader( "resources/samples/" );
        saveAndClose( repoBase, Util.joinPath( CdeConstants.SolutionFolders.WIDGETS, "sample.cdfde" ), sysPluginSamples,
            "widget.cdfde" );
        saveAndClose( repoBase, Util.joinPath( CdeConstants.SolutionFolders.WIDGETS, "sample.wcdf" ), sysPluginSamples,
            "widget.wcdf" );
        saveAndClose( repoBase, Util.joinPath( CdeConstants.SolutionFolders.WIDGETS, "sample.cda" ), sysPluginSamples,
            "widget.cda" );
        saveAndClose( repoBase, Util.joinPath( CdeConstants.SolutionFolders.WIDGETS, "sample.component.xml" ),
            sysPluginSamples, "widget.xml" );
      }
    }

  }

  private boolean saveAndClose( IRWAccess writer, String fileOut, IReadAccess reader, String fileIn ) {
    InputStream input = null;
    try {
      input = reader.getFileInputStream( fileIn );
      return getEnv().getFileHandler().createBasicFileIfNotExists( writer, fileOut, input );
    } catch ( IOException e ) {
      logger.error( "Couldn't read " + fileIn + " in " + reader );
    } finally {
      IOUtils.closeQuietly( input );
    }
    return false;
  }

  private boolean ensureDirExists( IRWAccess access, String relPath, boolean isHidden ) {
    return getEnv().getFileHandler().createBasicDirIfNotExists( access, relPath, isHidden );
  }

  public static ICdeEnvironment getEnv() {
    return getInstance().getEnvironment();
  }
}
