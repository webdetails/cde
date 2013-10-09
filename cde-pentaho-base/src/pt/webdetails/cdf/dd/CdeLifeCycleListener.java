/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cdf.dd;

//import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.PluginLifecycleException;
//import org.pentaho.platform.repository.hibernate.HibernateUtil;

//import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.SimpleLifeCycleListener;
//import pt.webdetails.cpf.repository.api.IRWAccess;
//import pt.webdetails.cpf.repository.api.IReadAccess;

public class CdeLifeCycleListener extends SimpleLifeCycleListener {

  static Log logger = LogFactory.getLog( CdeLifeCycleListener.class );

  @Override
  public void init() throws PluginLifecycleException {
    logger.debug( "Init for CDE" );
  }

  @Override
  public void loaded() /* throws PluginLifecycleException */{

//    IRWAccess access = CdeEnvironment.getPluginRepositoryWriter();
//
//    String pluginSolutionRepositoryDir = CdeEnvironment.getPluginRepositoryDir(); // "cde"
//
//    if ( !access.fileExists( null ) ) {
//      if ( access.createFolder( null ) ) {
//        HibernateUtil.closeSession(); // solves http://redmine.webdetails.org/issues/2094
//      } else {
//        logger.error( "Error while creating folder " + pluginSolutionRepositoryDir
//            + " for cde plugin. CDE may not work as expected", null );
//      }
//    }
//
//    if ( !access.fileExists( "styles" ) ) {
//      if ( access.createFolder( "styles" ) ) {
//        HibernateUtil.closeSession(); // solves http://redmine.webdetails.org/issues/2094
//      } else {
//        logger.error( "Error while creating folder " + pluginSolutionRepositoryDir
//            + "/styles for cde plugin. CDE may not work as expected", null );
//      }
//    }
//
//    if ( !access.fileExists( "components" ) ) {
//      if ( access.createFolder( "components" ) ) {
//        HibernateUtil.closeSession(); // solves http://redmine.webdetails.org/issues/2094
//      } else {
//        logger.error( "Error while creating folder " + pluginSolutionRepositoryDir
//            + "/components for cde plugin. CDE may not work as expected", null );
//      }
//    }
//
//    if ( !access.fileExists( "templates" ) ) {
//      if ( access.createFolder( "templates" ) ) {
//        HibernateUtil.closeSession(); // solves http://redmine.webdetails.org/issues/2094
//      } else {
//        logger.error( "Error while creating folder " + pluginSolutionRepositoryDir
//            + "/templates for cde plugin. CDE may not work as expected", null );
//      }
//    }
//
//    if ( !access.fileExists( "widgets" ) ) {
//      try {
//        if ( access.createFolder( "widgets" ) ) {
//          HibernateUtil.closeSession(); // solves http://redmine.webdetails.org/issues/2094
//        }
//
//        IReadAccess pluginSystemReader = CdeEnvironment.getPluginSystemReader( "resources/samples/" );
//        IRWAccess pluginRepositoryWriter = CdeEnvironment.getPluginRepositoryWriter( "widgets/" );
//
//        if ( pluginSystemReader.fileExists( "widget.cdfde" ) ) {
//          pluginRepositoryWriter.saveFile( "sample.cdfde", pluginSystemReader.getFileInputStream( "widget.cdfde" ) );
//        }
//
//        if ( pluginSystemReader.fileExists( "widget.wcdf" ) ) {
//          pluginRepositoryWriter.saveFile( "sample.wcdf", pluginSystemReader.getFileInputStream( "widget.wcdf" ) );
//        }
//
//        if ( pluginSystemReader.fileExists( "widget.cda" ) ) {
//          pluginRepositoryWriter.saveFile( "sample.cda", pluginSystemReader.getFileInputStream( "widget.cda" ) );
//        }
//
//        if ( pluginSystemReader.fileExists( "widget.xml" ) ) {
//          pluginRepositoryWriter
//              .saveFile("sample.component.xml", pluginSystemReader.getFileInputStream("widget.xml"));
//        }
//
//      } catch ( IOException ioe ) {
//        logger.error( "Error while creating folder " + pluginSolutionRepositoryDir
//            + "/widgets for cde plugin. CDE may not work as expected", ioe );
//      }
//    }
  }

  @Override
  public void unLoaded() throws PluginLifecycleException {
    logger.debug( "Unload for CDE" );
  }

  @Override
  public PluginEnvironment getEnvironment() {
    return (PluginEnvironment) CdeEngine.getInstance().getEnvironment();
  }
}
