/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import java.util.Date;
import net.sf.json.JSON;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.Resource;
import pt.webdetails.cdf.dd.model.meta.writer.cderunjs.CdeRunJsThingWriterFactory;
import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cdf.dd.render.DependenciesEngine;
import pt.webdetails.cdf.dd.render.DependenciesManager;
import pt.webdetails.cdf.dd.render.DependenciesManager.Engines;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IReadAccess;

/**
 * @author dcleao
 */
public final class MetaModelManager
{
  protected static final Log logger = LogFactory.getLog(MetaModelManager.class);
  
  private static MetaModelManager instance;
  
  public static synchronized MetaModelManager getInstance()
  {
    if(instance == null) { instance = new MetaModelManager(); }
    return instance;
  }

  private final Object lock = new Object();
  private MetaModel model;
  private String jsDefinition;
    
  private MetaModelManager()
  {
    Date dtStart = new Date();
    logger.info("CDE Starting Load MetaModelManager");
    
    this.model = readModel();
    
    logger.info("CDE Finished Load MetaModelManager: " + Utils.ellapsedSeconds(dtStart) + "s");
  }
  
  public MetaModel getModel()
  {
      synchronized(lock)
      {
        return this.model;
      }
  }

  public String getJsDefinition()
  {
      synchronized(lock)
      {
        if(this.jsDefinition == null && this.model != null)
        {
          this.jsDefinition = writeJsDefinition(this.model);
        }
        return this.jsDefinition;
      }
  }
  
  public void refresh()
  {
   this.refresh(true);
  }
  
  public void refresh(boolean refreshDatasources)
  {
    Date dtStart = new Date();
    logger.info("CDE Starting Reload MetaModelManager");
    
    if(refreshDatasources) { CdeEnvironment.getDataSourceManager().refresh(); }
    
    MetaModel model = this.readModel();
    if(model != null)
    {
      DependenciesManager.refresh();
      this.createDependencyManager(model);
      
      // Switch current model.
      synchronized(lock)
      {
        this.model  = model;
        this.jsDefinition = null;
      }
    }
    
    logger.info("CDE Finished Reload MetaModelManager: " + Utils.ellapsedSeconds(dtStart) + "s");
  }

  private MetaModel readModel()
  {
    // Read Components from the FS
    XmlFsPluginThingReaderFactory factory = new XmlFsPluginThingReaderFactory(CdeEnvironment.getContentAccessFactory());
    XmlFsPluginModelReader metaModelReader = factory.getMetaModelReader();
    try
    {
      // read component and property definitions
      MetaModel.Builder builder = metaModelReader.read(factory);
      // read data source definitions
      readDataSourceComponents(builder);
      return builder.build();
    }
    catch(ThingReadException ex)
    {
      logger.error("Error while reading model from file system.", ex);
    }
    catch(ValidationException ex)
    {
      logger.error("Error while building model.", ex);
    }
    return null;
  }

  private void readDataSourceComponents(MetaModel.Builder builder) {
    // --------------------
    // Read DataSource Components from each DataSourceProvider
    DataSourcesObjectReaderFactory dsFactory = new DataSourcesObjectReaderFactory();

    DataSourcesModelReader dsModelReader = dsFactory.getModelReader();
    IDataSourceManager dataSourceManager = CdeEnvironment.getDataSourceManager();
    for(IDataSourceProvider dsProvider : dataSourceManager.getProviders())
    {
      String providerId = dsProvider.getId();
      JSON jsDef = dataSourceManager.getProviderJsDefinition(providerId);
      try
      {
        // id is apparently a source
        dsModelReader.read(builder, jsDef, providerId);
      }
      catch(ThingReadException ex)
      { //TODO: isn't it be better to log and attempt to move on?
        logger.error("Error while reading model from data source definitions in '" + providerId + "'.", ex);
       // throw new ThingReadException("Error while reading model from data source definitions in '" + providerId + "'.", ex);
        //return null;
      }
    }
  }

  private String writeJsDefinition(MetaModel model)
  {
    IThingWriterFactory factory = new CdeRunJsThingWriterFactory();
    IThingWriter writer;

    try
    {
      writer = factory.getWriter(model);
    }
    catch (UnsupportedThingException ex)
    {
      logger.error("Error while obtaining the model writer from the factory.", ex);
      return null;
    }

    StringBuilder out = new StringBuilder();
    IThingWriteContext context = new DefaultThingWriteContext(factory, false);
    try
    {
      writer.write(out, context, model);
    }
    catch (ThingWriteException ex)
    {
      logger.error("Error while writing the model to JS.", ex);
      return null;
    }

    return out.toString();
  }

  //TODO: this should't be here;
  private DependenciesManager createDependencyManager(MetaModel metaModel)
  {
    DependenciesManager depMgr = DependenciesManager.getInstance();
    
    DependenciesEngine cdfDeps = depMgr.getEngine(Engines.CDF);
    DependenciesEngine rawDeps = depMgr.getEngine(Engines.CDF_RAW);
    DependenciesEngine styleDeps = depMgr.getEngine(Engines.CDF_CSS);
    DependenciesEngine ddDeps = depMgr.getEngine(Engines.CDFDD);
    
    for(ComponentType compType : metaModel.getComponentTypes())
    {
      PathOrigin origin = compType.getOrigin();
      if (origin == null) {
        logger.error("ComponentType " + compType.getName() + " src=" + compType.getImplementationPath() + " has no origin!");
      }
      IReadAccess reader = origin.getReader( CdeEnvironment.getContentAccessFactory() );
      // Implementation
      String srcImpl = compType.getImplementationPath();
      if (StringUtils.isNotEmpty(srcImpl))
      {
        try
        {
          cdfDeps.register(compType.getName(), compType.getVersion(), reader, srcImpl);
        }
        catch (Exception e)
        {
          logger.error("Failed to register dependency '" + srcImpl + "'");
        }
      }
      
      // General Resources
      for(Resource res : compType.getResources())
      {
        Resource.Type resType = res.getType();
        if(resType == Resource.Type.RAW)
        {
          try
          {
            rawDeps.registerRaw(res.getName(), res.getVersion(), res.getSource());
          }
          catch(Exception ex)
          {
            logger.error("Failed to register code fragment '" + res.getSource() + "'");
          }
        }
        else 
        {
          DependenciesEngine engine = null;
          if(resType == Resource.Type.SCRIPT)
          {
            String app = res.getApp();
            if(StringUtils.isEmpty(app) || app.equals(Engines.CDF))
            {
              engine = cdfDeps;
            }
            else if(app.equals(Engines.CDFDD))
            {
              engine = ddDeps;
            }
          }
          else if(resType == Resource.Type.STYLE)
          {
            engine = styleDeps;
          }
        
          if(engine != null)
          {
            try
            {
              //TODO: path origin
              engine.register(res.getName(), res.getVersion(), reader, res.getSource());
            }
            catch (Exception ex)
            {
              logger.error("Failed to register dependency '" + res.getSource() + "'");
            }
          }
        }
      }
    }
    
    return depMgr;
  }
}