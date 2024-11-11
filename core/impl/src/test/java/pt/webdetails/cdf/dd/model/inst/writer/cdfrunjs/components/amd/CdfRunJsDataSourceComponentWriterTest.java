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


package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.PropertyBinding;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.meta.DataSourceComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.meta.PropertyTypeUsage;
import pt.webdetails.cdf.dd.model.meta.writer.cderunjs.amd.CdeRunJsThingWriterFactory;
import pt.webdetails.cdf.dd.util.JsonUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.PropertyName;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.PropertyValue;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.INDENT1;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.INDENT2;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.NEWLINE;

public class CdfRunJsDataSourceComponentWriterTest {

  private static final String DATASOURCE_COMPONENT_NAME = "dsName";

  private Dashboard dash;
  private CdeRunJsThingWriterFactory factory;
  private CdfRunJsDashboardWriteContext context;
  private CdfRunJsDataSourceComponentWriter dataSourceComponentWriter;
  private DataSourceComponent dataSourceComponent;
  private DataSourceComponentType dataSourceComponentType;

  @Before
  public void setUp() throws Exception {
    dash = mock( Dashboard.class );
    factory = mock( CdeRunJsThingWriterFactory.class );
    dataSourceComponentWriter = new CdfRunJsDataSourceComponentWriter();
    context = mock( CdfRunJsDashboardWriteContext.class );
    dataSourceComponent = mock( DataSourceComponent.class );
    dataSourceComponentType = mock( DataSourceComponentType.class );

    when( context.getDashboard() ).thenReturn( dash );
    when( context.getFactory() ).thenReturn( factory );
    when( factory.getWriter( any( DataSourceComponent.class ) ) ).thenReturn( dataSourceComponentWriter );
    when( dataSourceComponent.getMeta() ).thenReturn( dataSourceComponentType );
  }

  @After
  public void tearDown() {
    dash = null;
    factory = null;
    dataSourceComponentWriter = null;
    context = null;
    dataSourceComponent = null;
    dataSourceComponentType = null;
  }

  @Test
  public void testDataSourceComponentWriter() throws ThingWriteException {
    Map<String, String> properties = new LinkedHashMap<>( 6 );
    properties.put( PropertyName.DATA_ACCESS_ID, null );
    properties.put( PropertyName.JNDI, "fakeJndi" );
    properties.put( PropertyName.CATALOG, "fakeCatalog" );
    properties.put( PropertyName.CUBE, "fakeCube" );
    properties.put( PropertyName.QUERY, "" );
    properties.put( PropertyName.QUERY_TYPE, PropertyValue.SQL_QUERY_TYPE );

    assertDataSourceComponentWriterOutput( "", properties );
  }

  @Test
  public void testCdaDataSourceComponentWriter() throws ThingWriteException {
    String path = "/path/fake.cda";

    Map<String, String> properties = new LinkedHashMap<>( 2 );
    properties.put( PropertyName.DATA_ACCESS_ID, "fakeDataAccessId" );
    properties.put( PropertyName.DATA_ACCESS_PUSH_ENABLED, Boolean.FALSE.toString() );
    properties.put( PropertyName.PATH, path );

    when( dataSourceComponent.tryGetPropertyValue( eq( PropertyName.CDA_PATH ), any() ) ).thenReturn( path );

    assertDataSourceComponentWriterOutput( DataSource.META_TYPE_CDA, properties, true );
  }

  @Test
  public void testBuiltInCdaDataSourceComponentWriter() throws ThingWriteException, ValidationException {
    Map<String, String> properties = new LinkedHashMap<>( 3 );
    properties.put( DATASOURCE_COMPONENT_NAME, "fakeDataAccessId" );
    properties.put( PropertyName.DATA_ACCESS_PUSH_ENABLED, Boolean.FALSE.toString() );
    properties.put( PropertyName.PATH, "" );

    when( dataSourceComponent.getName() ).thenReturn( properties.get( DATASOURCE_COMPONENT_NAME ) );

    assertDataSourceComponentWriterOutput( DataSource.META_TYPE_CDA, properties, true );
  }

  @Test
  public void testCpkDataSourceComponentWriter() throws ThingWriteException {
    String endpoint = "fakeEndpoint";
    String pluginID = "fakePluginID";

    Map<String, String> properties = new LinkedHashMap<>( 6 );
    properties.put( PropertyName.DATA_ACCESS_ID, null );
    properties.put( PropertyName.ENDPOINT, endpoint );
    properties.put( PropertyName.PLUGIN_ID, pluginID );
    properties.put( PropertyName.KETTLE_OUTPUT_STEP_NAME, "fakeStepName" );
    properties.put( PropertyName.KETTLE_OUTPUT_FORMAT, "fakeOutputFormat" );
    properties.put( PropertyName.QUERY_TYPE, PropertyValue.CPK_QUERY_TYPE );

    when( dataSourceComponentType.tryGetAttributeValue( eq( PropertyName.ENDPOINT ), any() ) ).thenReturn( endpoint );
    when( dataSourceComponentType.tryGetAttributeValue( eq( PropertyName.PLUGIN_ID ), any() ) ).thenReturn( pluginID );

    assertDataSourceComponentWriterOutput( DataSource.META_TYPE_CPK, properties );
  }

  @Test
  public void testSolrDataSourceComponentWriter() throws ThingWriteException {
    Map<String, String> properties = new LinkedHashMap<>( 2 );
    properties.put( PropertyName.DATA_ACCESS_ID, null );
    properties.put( PropertyName.QUERY_TYPE, PropertyValue.SOLR_QUERY_TYPE );

    List<PropertyBinding> propertyBindings = new ArrayList<>();
    when( dataSourceComponent.getPropertyBindings() ).thenReturn( propertyBindings );

    assertDataSourceComponentWriterOutput( DataSource.META_TYPE_SOLR, properties );
  }

  // -----

  private void assertDataSourceComponentWriterOutput( String metaType, Map<String, String> properties )
    throws ThingWriteException {
    assertDataSourceComponentWriterOutput( metaType, properties, false );
  }

  private void assertDataSourceComponentWriterOutput( String metaType, Map<String, String> properties,
                                                      boolean outputDataAccessId ) throws ThingWriteException {

    when( dataSourceComponentType.tryGetAttributeValue( eq( "" ), eq( "" ) ) ).thenReturn( metaType );

    StringJoiner expected = getDataSourceStringJoiner();
    properties.forEach( ( prop, value ) -> {
      boolean isDataSourceName = DATASOURCE_COMPONENT_NAME.equals( prop );
      if ( isDataSourceName ) {
        prop = PropertyName.DATA_ACCESS_ID;
      } else {
        when( dataSourceComponent.tryGetPropertyValue( eq( prop ), any() ) ).thenReturn( value );
      }
      boolean isDataAccessId = PropertyName.DATA_ACCESS_ID.equals( prop );
      boolean isPushEnabled = PropertyName.DATA_ACCESS_PUSH_ENABLED.equals( prop );
      if ( !isPushEnabled && ( !isDataAccessId || outputDataAccessId ) ) {
        expected.add( INDENT2 + prop + ": " + JsonUtils.toJsString( value ) );
      } else if ( isPushEnabled ) {
        expected.add( INDENT2 + prop + ": " + value );
      }

    } );

    StringBuilder result = new StringBuilder();
    dataSourceComponentWriter.write( result, context, dataSourceComponent );

    assertEquals( expected.toString(), result.toString() );
  }

  private StringJoiner getDataSourceStringJoiner() {
    String delimiter = "," + NEWLINE;
    String prefix = "{" + NEWLINE;
    String suffix = NEWLINE + INDENT1 + "}";

    return new StringJoiner( delimiter, prefix, suffix );
  }

  private PropertyBinding getPropertyBinding( String value ) throws ValidationException {
    PropertyBinding.Builder builder = getBuilder();
    builder.setValue( value );
    Component component = mock( Component.class );
    MetaModel metaModel = mock( MetaModel.class );
    return new PropertyBinding( builder, component, metaModel ) {
      @Override
      public PropertyTypeUsage getPropertyUsage() {
        return null;
      }

      @Override
      public String getAlias() {
        return null;
      }

      @Override
      public String getInputType() {
        return null;
      }

      @Override
      public PropertyType getProperty() {
        return null;
      }
    };
  }

  private PropertyBinding.Builder getBuilder() {
    return new PropertyBinding.Builder() {
      @Override
      public String getAlias() {
        return null;
      }

      @Override
      public PropertyBinding build( Component owner, MetaModel metaModel ) throws ValidationException {
        return null;
      }
    };
  }
}
