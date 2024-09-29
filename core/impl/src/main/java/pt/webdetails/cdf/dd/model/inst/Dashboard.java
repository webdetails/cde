/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cdf.dd.model.inst;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.validation.RequiredAttributeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.inst.validation.DashboardDuplicateComponentError;
import pt.webdetails.cdf.dd.model.meta.DashboardType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Dashboard extends Instance<DashboardType> {
  private static final Log _logger = LogFactory.getLog( Dashboard.class );

  private final DashboardWcdfDescriptor _wcdf;
  private final String _sourcePath;
  private final Date _sourceDate;

  private final Map<String, DataSourceComponent> _dataSourceComponentsByLowerName;
  private final Map<String, LayoutComponent> _layoutComponentsByLowerName;
  private final Map<String, Component> _regularComponentsByLowerName;

  private final List<DataSourceComponent> _dataSourceComponents;
  private final List<LayoutComponent> _layoutComponents;
  private final List<Component> _regularComponents;

  protected Dashboard( Builder builder, MetaModel metaModel ) throws ValidationException {
    super( builder );

    if ( metaModel == null ) {
      throw new IllegalArgumentException( "metaModel" );
    }

    if ( builder._wcdf == null ) {
      throw new ValidationException( new RequiredAttributeError( "Wcdf" ) );
    }

    this._wcdf = builder._wcdf;
    this._sourcePath = Utils.sanitizeSlashesInPath( StringUtils.defaultIfEmpty( builder._sourcePath, "" ) );
    this._sourceDate = builder._sourceDate == null ? new Date() : builder._sourceDate;

    // NOTE: During editing, components may have no name and even duplicate names...

    this._dataSourceComponentsByLowerName = new LinkedHashMap<String, DataSourceComponent>();
    this._dataSourceComponents = new ArrayList<DataSourceComponent>();

    for ( DataSourceComponent.Builder compBuilder : builder._dataSourceComponents ) {
      DataSourceComponent comp;
      try {
        comp = compBuilder.build( metaModel );
      } catch ( ValidationException ex ) {
        // Ignore datasource component, log warning and continue.
        _logger.warn( ex.getError() );
        continue;
      }

      this._dataSourceComponents.add( comp );

      String key = comp.getName().toLowerCase();
      if ( !this._dataSourceComponentsByLowerName.containsKey( key ) ) {
        this._dataSourceComponentsByLowerName.put( key, comp );
      } else {
        // Don't index datasource component by lower name and log warning.
        _logger.warn( new DashboardDuplicateComponentError( comp.getName(), this.getId() ) );
      }
    }

    this._layoutComponentsByLowerName = new LinkedHashMap<String, LayoutComponent>();
    this._layoutComponents = new ArrayList<LayoutComponent>();
    for ( LayoutComponent.Builder compBuilder : builder._layoutComponents ) {
      LayoutComponent comp;
      try {
        comp = compBuilder.build( metaModel );
      } catch ( ValidationException ex ) {
        // Ignore layout component, log warning and continue.
        _logger.warn( ex.getError() );
        continue;
      }

      this._layoutComponents.add( comp );

      String key = comp.getName().toLowerCase();
      if ( !this._layoutComponentsByLowerName.containsKey( key ) ) {
        this._layoutComponentsByLowerName.put( key, comp );
      } else {
        // Don't index layout component by lower name and log warning.
        _logger.warn( new DashboardDuplicateComponentError( comp.getName(), this.getId() ) );
      }
    }

    this._regularComponentsByLowerName = new LinkedHashMap<String, Component>();
    this._regularComponents = new ArrayList<Component>();
    for ( Component.Builder compBuilder : builder._regularComponents ) {
      Component comp;
      try {
        comp = compBuilder.build( metaModel );
      } catch ( ValidationException ex ) {
        // Ignore regular component, log warning and continue.
        _logger.warn( ex.getError() );
        continue;
      }

      this._regularComponents.add( comp );

      String key = comp.getName().toLowerCase();
      if ( !this._regularComponentsByLowerName.containsKey( key ) ) {
        this._regularComponentsByLowerName.put( key, comp );
      } else {
        // Don't index regular component by lower name and log warning.
        _logger.warn( new DashboardDuplicateComponentError( comp.getName(), this.getId() ) );
      }
    }
  }

  @Override
  public DashboardType getMeta() {
    return super.getMeta();
  }

  @Override
  public String getKind() {
    return KnownThingKind.Dashboard;
  }

  @Override
  public final String getId() {
    // TODO: Not sure what to return here...
    return this.getMeta().getId();
  }

  public DashboardWcdfDescriptor getWcdf() {
    return this._wcdf;
  }

  public final String getSourcePath() {
    return this._sourcePath;
  }

  public final Date getSourceDate() {
    return this._sourceDate;
  }

  public DataSourceComponent getDataSource( String name ) {
    DataSourceComponent comp = this.tryGetDataSource( name );
    if ( comp == null ) {
      throw new IllegalArgumentException( "There is no data source component with name '" + name + "'." );
    }

    return comp;
  }

  public DataSourceComponent tryGetDataSource( String name ) {
    if ( name == null ) {
      throw new IllegalArgumentException( "name" );
    }

    return this._dataSourceComponentsByLowerName.get( name.toLowerCase() );
  }

  public Iterable<DataSourceComponent> getDataSources() {
    return this._dataSourceComponents;
  }

  public int getDataSourceCount() {
    return this._dataSourceComponents.size();
  }

  // -----------------

  public LayoutComponent getLayout( String name ) {
    LayoutComponent comp = this.tryGetLayout( name );
    if ( comp == null ) {
      throw new IllegalArgumentException( "There is no layout component with name '" + name + "'." );
    }

    return comp;
  }

  public LayoutComponent tryGetLayout( String name ) {
    if ( StringUtils.isEmpty( name ) ) {
      throw new IllegalArgumentException( "name" );
    }

    return this._layoutComponentsByLowerName.get( name.toLowerCase() );
  }

  public Iterable<LayoutComponent> getLayouts() {
    return this._layoutComponents;
  }

  public int getLayoutCount() {
    return this._layoutComponents.size();
  }

  // -----------------

  public Component getRegular( String name ) {
    Component comp = this.tryGetRegular( name );
    if ( comp == null ) {
      throw new IllegalArgumentException( "There is no regular component with name '" + name + "'." );
    }

    return comp;
  }

  public Component tryGetRegular( String name ) {
    if ( StringUtils.isEmpty( name ) ) {
      throw new IllegalArgumentException( "name" );
    }

    return this._regularComponentsByLowerName.get( name.toLowerCase() );
  }

  public Iterable<Component> getRegulars() {
    return this._regularComponents;
  }

  public int getRegularCount() {
    return this._regularComponents.size();
  }

  /**
   * Class to create and modify Dashboard instances.
   */
  public static class Builder extends Instance.Builder<DashboardType> {
    private final List<DataSourceComponent.Builder> _dataSourceComponents;
    private final List<LayoutComponent.Builder> _layoutComponents;
    private final List<Component.Builder> _regularComponents;
    private DashboardWcdfDescriptor _wcdf;
    private String _sourcePath;
    private Date _sourceDate;

    public Builder() {
      this._dataSourceComponents = new ArrayList<DataSourceComponent.Builder>();
      this._layoutComponents = new ArrayList<LayoutComponent.Builder>();
      this._regularComponents = new ArrayList<Component.Builder>();
    }

    public DashboardWcdfDescriptor getWcdf() {
      return this._wcdf;
    }

    public Builder setWcdf( DashboardWcdfDescriptor wcdf ) {
      this._wcdf = wcdf;

      return this;
    }

    public String getSourcePath() {
      return this._sourcePath;
    }

    public Builder setSourcePath( String sourcePath ) {
      this._sourcePath = sourcePath;
      return this;
    }

    public final Date getSourceDate() {
      return this._sourceDate;
    }

    public Builder setSourceDate( Date sourceDate ) {
      this._sourceDate = sourceDate;
      return this;
    }

    public Builder addComponent( Component.Builder comp ) {
      if ( comp == null ) {
        throw new IllegalArgumentException( "comp" );
      }

      Class<?> compClass = comp.getClass();

      if ( DataSourceComponent.Builder.class.isAssignableFrom( compClass ) ) {
        this._dataSourceComponents.add( (DataSourceComponent.Builder) comp );
      } else if ( LayoutComponent.Builder.class.isAssignableFrom( compClass ) ) {
        this._layoutComponents.add( (LayoutComponent.Builder) comp );
      } else {
        this._regularComponents.add( comp );
      }

      return this;
    }

    public Dashboard build( MetaModel metaModel ) throws ValidationException {
      return new Dashboard( this, metaModel );
    }
  }
}
