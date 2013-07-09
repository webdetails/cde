
package pt.webdetails.cdf.dd;

import java.util.Date;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.reader.DefaultThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.IThingReader;
import pt.webdetails.cdf.dd.model.core.reader.IThingReaderFactory;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.meta.reader.cda.CdaObjectReaderFactory;
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
import pt.webdetails.cdf.dd.render.DependenciesEngine;
import pt.webdetails.cdf.dd.render.DependenciesManager;
import pt.webdetails.cdf.dd.render.DependenciesManager.Engines;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.InterPluginCall;

/**
 * @author dcleao
 */
public final class MetaModelManager
{
  protected static final Log _logger = LogFactory.getLog(MetaModelManager.class);
  
  private static MetaModelManager _instance;
  
  public static synchronized MetaModelManager getInstance()
  {
    if(_instance == null) { _instance = new MetaModelManager(); }
    return _instance;
  }

  private final Object _lock = new Object();
  private MetaModel  _model;
  private String _jsDefinition;
  private JSON   _cdaDefs;
  
  private MetaModelManager()
  {
    Date dtStart = new Date();
    _logger.info("CDE Starting Load MetaModelManager");
    
    this._cdaDefs = this.getCdaDefs(/*isRefresh*/false); // TODO: No errors? How to know when it fails?
    this._model   = readModel(this._cdaDefs);
    
    DependenciesManager.setInstance(this.createDependencyManager(this._model));
    
    _logger.info("CDE Finished Load MetaModelManager: " + Utils.ellapsedSeconds(dtStart) + "s");
  }
  
  public MetaModel getModel()
  {
      synchronized(_lock)
      {
        return this._model;
      }
  }

  public String getJsDefinition()
  {
      synchronized(_lock)
      {
        if(this._jsDefinition == null && this._model != null)
        {
          this._jsDefinition = writeJsDefinition(this._model);
        }
        return this._jsDefinition;
      }
  }
  
  
  public JSON getCdaDefinitions()
  {
    synchronized(_lock)
    {
      return this._cdaDefs;
    }
  }

  public void refresh()
  {
    Date dtStart = new Date();
    _logger.info("CDE Starting Reload MetaModelManager");
    
    JSON cdaDefs = this.getCdaDefs(/*isRefresh*/true); // TODO: No errors? How to know when it fails?
    
    MetaModel model = this.readModel(cdaDefs);
    if(model != null)
    {
      DependenciesManager depMgr = this.createDependencyManager(model);
      
      // Switch current model.
      synchronized(_lock)
      {
        this._model  = model;
        this._cdaDefs = cdaDefs;
        this._jsDefinition = null;
      }
      
      // Switch the current dependencies manager
      //  Not doing this inside the above synchronize
      //  because I guess it could create a dead-lock.
      DependenciesManager.setInstance(depMgr);
    }
    
    _logger.info("CDE Finished Reload MetaModelManager: " + Utils.ellapsedSeconds(dtStart) + "s");
  }

  private MetaModel readModel(JSON cdaDefs)
  {
    // --------------------
    // Read Components from the FS
    IThingReaderFactory factory = new XmlFsPluginThingReaderFactory();
    IThingReadContext   context = new DefaultThingReadContext(factory);

    // Obtain the root model reader from the factory
    IThingReader modelReader;
    try
    {
      modelReader = factory.getReader(KnownThingKind.MetaModel, null, null);
    }
    catch (UnsupportedThingException ex)
    {
      _logger.error("Error while obtaining the model reader from the file system factory.", ex);
      return null;
    }

    // Reading with the model reader
    MetaModel.Builder builder;
    try
    {
      builder = (MetaModel.Builder)modelReader.read(context, null, null);
    }
    catch(ThingReadException ex)
    {
      _logger.error("Error while reading model from file system.", ex);
      return null;
    }

    // --------------------
    // Read CDA Components from CDA
    factory = new CdaObjectReaderFactory();
    context = new DefaultThingReadContext(factory);
    
    // Obtain the root model reader from the factory
    try
    {
      modelReader = factory.getReader(KnownThingKind.MetaModel, null, null);
    }
    catch (UnsupportedThingException ex)
    {
      _logger.error("Error while obtaining the model reader from the CDA factory.", ex);
      return null;
    }

    try
    {
      modelReader.read(builder, context, cdaDefs, "CDAPlugin");
    }
    catch(ThingReadException ex)
    {
      _logger.error("Error while reading model from CDA definitions.", ex);
      return null;
    }

    // ---------------
    // BUILD
    try
    {
      return builder.build();
    }
    catch(ValidationException ex)
    {
      _logger.error("Error while building model.", ex);
      return null;
    }
  }
  
  private JSON getCdaDefs(boolean isRefresh)
  {
    InterPluginCall cdaListDataAccessTypes =
            new InterPluginCall(InterPluginCall.CDA, "listDataAccessTypes");

    // TODO: Current session should be irrelevant. Result is cached for all users...
    // Consider using an admin session here
    cdaListDataAccessTypes.setSession(PentahoSessionHolder.getSession());
    cdaListDataAccessTypes.putParameter("refreshCache", "" + isRefresh);

    return JSONSerializer.toJSON(cdaListDataAccessTypes.call());
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
      _logger.error("Error while obtaining the model writer from the factory.", ex);
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
      _logger.error("Error while writing the model to JS.", ex);
      return null;
    }

    return out.toString();
  }

  private DependenciesManager createDependencyManager(MetaModel metaModel)
  {
    DependenciesManager depMgr = DependenciesManager.createInstance();
    
    DependenciesEngine cdfDeps   = depMgr.getEngine(Engines.CDF    );
    DependenciesEngine rawDeps   = depMgr.getEngine(Engines.CDF_RAW);
    DependenciesEngine styleDeps = depMgr.getEngine(Engines.CDF_CSS);
    DependenciesEngine ddDeps    = depMgr.getEngine(Engines.CDFDD  );
    
    for(ComponentType compType : metaModel.getComponentTypes())
    {
      // Implementation
      String srcImpl = compType.getImplementationPath();
      if (StringUtils.isNotEmpty(srcImpl))
      {
        try
        {
          cdfDeps.register(compType.getName(), compType.getVersion(), srcImpl);
        }
        catch (Exception e)
        {
          _logger.error("Failed to register dependency '" + srcImpl + "'");
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
            _logger.error("Failed to register code fragment '" + res.getSource() + "'");
          }
        }
        else 
        {
          DependenciesEngine engine = null;
          if(resType == Resource.Type.SCRIPT)
          {
            String app = res.getApp();
            if(app == null || app.equals(Engines.CDF))
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
              engine.register(res.getName(), res.getVersion(), res.getSource());
            }
            catch (Exception ex)
            {
              _logger.error("Failed to register dependency '" + res.getSource() + "'");
            }
          }
        }
      }
    }
    
    return depMgr;
  }
}