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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cdf.dd.datasources.IDataSourceProvider;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.meta.reader.datasources.DataSourcesModelReader;
import pt.webdetails.cdf.dd.model.meta.reader.datasources.DataSourcesObjectReaderFactory;
import pt.webdetails.cdf.dd.model.meta.reader.cdexml.fs.XmlFsPluginModelReader;
import pt.webdetails.cdf.dd.model.meta.reader.cdexml.fs.XmlFsPluginThingReaderFactory;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.core.writer.DefaultThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.writer.cderunjs.legacy.CdeRunJsThingWriterFactory;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.Utils;

public final class MetaModelManager {
  protected static final Log logger = LogFactory.getLog( MetaModelManager.class );

  private static final MetaModelManager instance = new MetaModelManager();

  public static MetaModelManager getInstance() {
    return instance;
  }

  private final Object lock = new Object();
  private MetaModel model;
  private String jsDefinition;
  private String amdJsDefinition;

  private MetaModelManager() {
    long start = System.currentTimeMillis();
    logger.info( "CDE Starting Load MetaModelManager" );

    this.model = readModel();

    logger.info( "CDE Finished Load MetaModelManager: " + Utils.ellapsedSeconds( start ) + "s" );
  }

  public MetaModel getModel() {
    synchronized ( lock ) {
      return this.model;
    }
  }

  public String getJsDefinition() {
    synchronized ( lock ) {
      if ( this.jsDefinition == null && this.model != null ) {
        this.jsDefinition = writeJsDefinition( this.model );
      }
      return this.jsDefinition;
    }
  }

  public String getAmdJsDefinition() {
    synchronized ( lock ) {
      if ( this.amdJsDefinition == null && this.model != null ) {
        this.amdJsDefinition = writeAmdJsDefinition( this.model );
      }
      return this.amdJsDefinition;
    }
  }

  public void refresh() {
    this.refresh( true );
  }

  public void refresh( boolean refreshDatasources ) {
    long start = System.currentTimeMillis();
    logger.info( "CDE Starting Reload MetaModelManager" );

    if ( refreshDatasources ) {
      CdeEnvironment.getDataSourceManager().refresh();
    }

    MetaModel model = this.readModel();
    if ( model != null ) {

      // Switch current model.
      synchronized ( lock ) {
        this.model = model;
        this.jsDefinition = null;
        this.amdJsDefinition = null;
      }
    }

    logger.info( "CDE Finished Reload MetaModelManager: " + Utils.ellapsedSeconds( start ) + "s" );
  }

  private MetaModel readModel() {
    // Read Components from the FS
    XmlFsPluginThingReaderFactory factory =
        new XmlFsPluginThingReaderFactory( CdeEnvironment.getContentAccessFactory() );
    XmlFsPluginModelReader metaModelReader = factory.getMetaModelReader();
    try {
      // read component and property definitions
      MetaModel.Builder builder = metaModelReader.read( factory );
      // read data source definitions
      readDataSourceComponents( builder );
      return builder.build();
    } catch ( ThingReadException ex ) {
      logger.error( "Error while reading model from file system.", ex );
    } catch ( ValidationException ex ) {
      logger.error( "Error while building model.", ex );
    }
    return null;
  }

  private void readDataSourceComponents( MetaModel.Builder builder ) {
    // Read DataSource Components from each DataSourceProvider
    DataSourcesObjectReaderFactory dsFactory = new DataSourcesObjectReaderFactory();

    DataSourcesModelReader dsModelReader = dsFactory.getModelReader();
    IDataSourceManager dataSourceManager = CdeEnvironment.getDataSourceManager();
    for ( IDataSourceProvider dsProvider : dataSourceManager.getProviders() ) {
      String providerId = dsProvider.getId();
      JSONObject jsDef = dataSourceManager.getProviderJsDefinition( providerId );
      try {
        // id is apparently a source
        dsModelReader.read( builder, jsDef, providerId );
      } catch ( ThingReadException ex ) {
        logger.error( "Error while reading model from data source definitions in '" + providerId + "'.", ex );
      }
    }
  }

  private String writeJsDefinition( MetaModel model ) {
    IThingWriterFactory factory = new CdeRunJsThingWriterFactory();
    IThingWriter writer;

    try {
      writer = factory.getWriter( model );
    } catch ( UnsupportedThingException ex ) {
      logger.error( "Error while obtaining the model writer from the factory.", ex );
      return null;
    }

    StringBuilder out = new StringBuilder();
    IThingWriteContext context = new DefaultThingWriteContext( factory, false );
    try {
      writer.write( out, context, model );
    } catch ( ThingWriteException ex ) {
      logger.error( "Error while writing the model to JS.", ex );
      return null;
    }

    return out.toString();
  }

  private String writeAmdJsDefinition( MetaModel model ) {
    IThingWriterFactory factory =
        new pt.webdetails.cdf.dd.model.meta.writer.cderunjs.amd.CdeRunJsThingWriterFactory();
    IThingWriter writer;

    try {
      writer = factory.getWriter( model );
    } catch ( UnsupportedThingException ex ) {
      logger.error( "Error while obtaining the model writer from the factory.", ex );
      return null;
    }

    StringBuilder out = new StringBuilder();
    IThingWriteContext context = new DefaultThingWriteContext( factory, false );
    try {
      writer.write( out, context, model );
    } catch ( ThingWriteException ex ) {
      logger.error( "Error while writing the model to JS.", ex );
      return null;
    }

    return out.toString();
  }
}
