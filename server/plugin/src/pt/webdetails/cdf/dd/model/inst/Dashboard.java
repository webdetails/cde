
package pt.webdetails.cdf.dd.model.inst;

import java.util.*;
import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.validation.RequiredAttributeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.inst.validation.DashboardDuplicateComponentError;
import pt.webdetails.cdf.dd.model.meta.DashboardType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.structure.WcdfDescriptor;

/**
 * @author dcleao
 */
public class Dashboard<TM extends DashboardType> extends Instance<TM>
{
  private final WcdfDescriptor _wcdf;
  private final String _sourcePath;
  private final Date   _sourceDate;
  
  private final Map<String, DataSourceComponent> _dataSourceComponents;
  private final Map<String, LayoutComponent>     _layoutComponents;
  private final Map<String, Component>           _regularComponents;
  
  protected Dashboard(Builder builder, MetaModel metaModel) throws ValidationException
  {
    super(builder);
    
    if(metaModel == null) { throw new IllegalArgumentException("metaModel"); }
    
    if(builder._wcdf == null) 
    {
      throw new ValidationException(new RequiredAttributeError("Wcdf"));
    }
    
    this._wcdf = builder._wcdf;
    this._sourcePath = StringUtils.defaultIfEmpty(builder._sourcePath, "");
    this._sourceDate = builder._sourceDate == null ? new Date() : builder._sourceDate;
    
    this._dataSourceComponents = new LinkedHashMap<String, DataSourceComponent>();
    
    for(DataSourceComponent.Builder compBuilder : builder._dataSourceComponents)
    {
      DataSourceComponent comp = compBuilder.build(metaModel);
      
      String key = comp.getName().toLowerCase();
      if(this._dataSourceComponents.containsKey(key))
      {
        throw new ValidationException(
              new DashboardDuplicateComponentError(comp.getName(), this.getId()));
      }
      
      this._dataSourceComponents.put(key, comp);
    }
    
    this._layoutComponents = new LinkedHashMap<String, LayoutComponent>();
    for(LayoutComponent.Builder compBuilder : builder._layoutComponents)
    {
      LayoutComponent comp = compBuilder.build(metaModel);
      
      String key = comp.getName().toLowerCase();
      if(this._layoutComponents.containsKey(key))
      {
        throw new ValidationException(
              new DashboardDuplicateComponentError(comp.getName(), this.getId()));
      }
      
      this._layoutComponents.put(key, comp);
    }
    
    this._regularComponents = new LinkedHashMap<String, Component>();
    for(Component.Builder compBuilder : builder._regularComponents)
    {
      Component comp = compBuilder.build(metaModel);
      
      String key = comp.getName().toLowerCase();
      if(this._regularComponents.containsKey(key))
      {
        throw new ValidationException(
              new DashboardDuplicateComponentError(comp.getName(), this.getId()));
      }
      
      this._regularComponents.put(key, comp);
    }
  }

  @Override
  public TM getMeta()
  {
    return super.getMeta();
  }

  @Override
  public String getKind()
  {
    return KnownThingKind.Dashboard;
  }
  
  @Override
  public final String getId()
  {
    // TODO: Not sure what to return here...
    return this.getMeta().getId();
  }
  
  public final WcdfDescriptor getWcdf()
  {
    return this._wcdf;
  }
  
  public final String getSourcePath()
  {
    return this._sourcePath;
  }
  
  public final Date getSourceDate()
  {
    return this._sourceDate;
  }
  
  public DataSourceComponent getDataSource(String name)
  {
    DataSourceComponent comp = this.tryGetDataSource(name);
    if(comp == null)
    {
      throw new IllegalArgumentException("There is no data source component with name '" + name + "'.");
    }

    return comp;
  }

  public DataSourceComponent tryGetDataSource(String name)
  {
    if(StringUtils.isEmpty(name)) { throw new IllegalArgumentException("name"); }

    return this._dataSourceComponents.get(name.toLowerCase());
  }

  public Iterable<DataSourceComponent> getDataSources()
  {
    return this._dataSourceComponents.values();
  }

  public int getDataSourceCount()
  {
    return this._dataSourceComponents.size();
  }
  
  // -----------------
  
  public LayoutComponent getLayout(String name)
  {
    LayoutComponent comp = this.tryGetLayout(name);
    if(comp == null)
    {
      throw new IllegalArgumentException("There is no layout component with name '" + name + "'.");
    }

    return comp;
  }

  public LayoutComponent tryGetLayout(String name)
  {
    if(StringUtils.isEmpty(name)) { throw new IllegalArgumentException("name"); }

    return this._layoutComponents.get(name.toLowerCase());
  }

  public Iterable<LayoutComponent> getLayouts()
  {
    return this._layoutComponents.values();
  }

  public int getLayoutCount()
  {
    return this._layoutComponents.size();
  }
  
  // -----------------
  
  public Component getRegular(String name)
  {
    Component comp = this.tryGetRegular(name);
    if(comp == null)
    {
      throw new IllegalArgumentException("There is no regular component with name '" + name + "'.");
    }

    return comp;
  }

  public Component tryGetRegular(String name)
  {
    if(StringUtils.isEmpty(name)) { throw new IllegalArgumentException("name"); }

    return this._regularComponents.get(name.toLowerCase());
  }

  public Iterable<Component> getRegulars()
  {
    return this._regularComponents.values();
  }

  public int getRegularCount()
  {
    return this._regularComponents.size();
  }
  
  /**
   * Class to create and modify Dashboard instances.
   */
  public static class Builder extends Instance.Builder
  {
    private WcdfDescriptor _wcdf;
    private String _sourcePath;
    private Date   _sourceDate;
    
    private final List<DataSourceComponent.Builder> _dataSourceComponents;
    private final List<LayoutComponent.Builder>     _layoutComponents;
    private final List<Component.Builder>           _regularComponents;
    
    public Builder()
    {
      this._dataSourceComponents = new ArrayList<DataSourceComponent.Builder>();
      this._layoutComponents     = new ArrayList<LayoutComponent.Builder>();
      this._regularComponents      = new ArrayList<Component.Builder>();
    }
    
    public WcdfDescriptor getWcdf()
    {
      return this._wcdf;
    }
    
    public Builder setWcdf(WcdfDescriptor wcdf)
    {
      this._wcdf = wcdf;
      
      return this;
    }
    
    public String getSourcePath()
    {
      return this._sourcePath;
    }

    public Builder setSourcePath(String sourcePath)
    {
      this._sourcePath = sourcePath;
      return this;
    }
    
    public final Date getSourceDate()
    {
      return this._sourceDate;
    }
    
    public Builder setSourceDate(Date sourceDate)
    {
      this._sourceDate = sourceDate;
      return this;
    }
    
    public Builder addComponent(Component.Builder comp)
    {
      if(comp == null) { throw new IllegalArgumentException("comp"); }
      
      Class compClass = comp.getClass();
      
      if(DataSourceComponent.Builder.class.isAssignableFrom(compClass))
      {
        this._dataSourceComponents.add((DataSourceComponent.Builder)comp);
      }
      else if(LayoutComponent.Builder.class.isAssignableFrom(compClass))
      {
        this._layoutComponents.add((LayoutComponent.Builder)comp);
      } 
      else 
      {
        this._regularComponents.add(comp);
      }
      
      return this;
    }
    
    public Dashboard build(MetaModel metaModel) throws ValidationException
    {
      return new Dashboard(this, metaModel);
    }
  }
}