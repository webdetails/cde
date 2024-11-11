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


package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.PropertyBinding;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.meta.DataSourceComponentType;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.AttributeName.DATA_ACCESS_TYPE;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.PropertyName.STREAMING_TYPE;

public class CdfRunJsDataSourcePropertyBindingWriter extends CdfRunJsPropertyBindingWriter {
  protected static final Log logger = LogFactory.getLog( CdfRunJsDataSourcePropertyBindingWriter.class );

  private static final String META_TYPE_CDA = "CDA";
  private static final String META_TYPE_CPK = "CPK";

  /**
   * This class contains the constants that represent the names of the
   * data source properties that are to be rendered to Javascript.
   */
  private static class PropertyName {
    // Datasource
    public static final String QUERY_TYPE = "queryType";
    public static final String QUERY = "query";
    public static final String SQL_QUERY = "sqlquery";
    public static final String MDX_QUERY = "mdxquery";
    public static final String JNDI = "jndi";
    public static final String CATALOG = "catalog";
    public static final String CUBE = "cube";
    public static final String PUSH_ENABLED = "pushEnabled";

    // CPK DataSource
    public static final String DATA_ACCESS_ID = "dataAccessId";
    public static final String ENDPOINT = "endpoint";
    public static final String PLUGIN_ID = "pluginId";
    public static final String KETTLE_OUTPUT_FORMAT = "kettleOutput";
    public static final String KETTLE_OUTPUT_STEP_NAME = "stepName";

    // CDA DataSource
    public static final String OUTPUT_INDEX_ID = "outputIndexId";
    public static final String SOLUTION = "solution";
    public static final String PATH = "path";
    public static final String FILE = "file";
  }

  /**
   * This class contains the constant values for data source properties.
   */
  private static class PropertyValue {
    public static final String CPK_QUERY_TYPE = "cpk";
    public static final String MDX_QUERY_TYPE = "mdx";
    public static final String SQL_QUERY_TYPE = "sql";
  }


  protected static String buildJsStringValue( String value ) {
    return JsonUtils.toJsString( replaceParameters( value == null ? "" : value ) );
  }

  protected static String replaceParameters( String value ) {
    // TODO: Someone explain this SHIT!
    if ( value != null ) {
      Pattern pattern = Pattern.compile( "\\$\\{[^}]*\\}" );
      Matcher matcher = pattern.matcher( value );
      while ( matcher.find() ) {
        String parameter = matcher.group();
        value =
          value.replace( matcher.group(), "Dashboards.ev(" + parameter.substring( 2, parameter.length() - 1 ) + ")" );
      }
    }
    return value;
  }

  public void write( StringBuilder out, CdfRunJsDashboardWriteContext context, PropertyBinding propBind )
    throws ThingWriteException {
    DataSourceComponent dataSourceComp = this.getDataSourceComponent( context, propBind );
    if ( dataSourceComp == null ) {
      return;
    }

    String dataAccessId = dataSourceComp.tryGetPropertyValue( PropertyName.DATA_ACCESS_ID, null );
    if ( dataAccessId != null ) {
      renderCdaDatasource( out, context, dataSourceComp, dataAccessId );
    } else {
      // "meta" attribute has the value "CDA", "CPK" ?
      // See DataSourceModelReader#readDataSourceComponent
      String metaType = dataSourceComp.getMeta().tryGetAttributeValue( "", "" );
      if ( StringUtils.isEmpty( metaType ) ) {
        renderDatasource( out, context, dataSourceComp );
      } else if ( metaType.equals( META_TYPE_CDA ) ) {
        renderBuiltinCdaDatasource( out, context, dataSourceComp );
      } else if ( metaType.equals( META_TYPE_CPK ) ) {
        renderCpkDatasource( out, context, dataSourceComp );
      } else {
        throw new ThingWriteException( "Cannot render a data source property of meta type '" + metaType + "'." );
      }
    }
  }

  protected DataSourceComponent getDataSourceComponent( CdfRunJsDashboardWriteContext context,
                                                        PropertyBinding propBind ) {
    String dataSourceName = propBind.getValue();
    return StringUtils.isEmpty( dataSourceName )
      ? null
      : context.getDashboard().tryGetDataSource( dataSourceName );
  }

  protected void renderCdaDatasource( StringBuilder out, CdfRunJsDashboardWriteContext context,
                                      DataSourceComponent dataSourceComp, String dataAccessId ) {
    String dashPath = context.getDashboard().getSourcePath();
    this.renderCdaDatasource( out, context, dataSourceComp, dataAccessId, dashPath );

  }

  protected void renderCdaDatasource( StringBuilder out, CdfRunJsDashboardWriteContext context,
                                      DataSourceComponent dataSourceComp, String dataAccessId, String dashPath ) {
    String indent = context.getIndent();

    addJsProperty( out, PropertyName.DATA_ACCESS_ID, buildJsStringValue( dataAccessId ),
      indent, context.isFirstInList() );

    context.setIsFirstInList( false );

    String outputIndexId = dataSourceComp.tryGetPropertyValue( PropertyName.OUTPUT_INDEX_ID, null );
    if ( outputIndexId != null ) {
      addJsProperty( out, PropertyName.OUTPUT_INDEX_ID, buildJsStringValue( outputIndexId ),
        indent, false );
    }

    // Check if we have a cdaFile
    String cdaPath = dataSourceComp.tryGetPropertyValue( "cdaPath", null );
    if ( cdaPath != null ) {
      // Check if path is relative
      if ( !cdaPath.startsWith( "/" ) ) {
        dashPath = FilenameUtils.getPath( dashPath );
        cdaPath = RepositoryHelper.normalize( Util.joinPath( dashPath, cdaPath ) );
      }
      addJsProperty( out, PropertyName.PATH, buildJsStringValue( cdaPath ), indent, false );
    } else {
      // legacy
      String solution = buildJsStringValue( dataSourceComp.tryGetPropertyValue( PropertyName.SOLUTION, "" ) );
      addJsProperty( out, PropertyName.SOLUTION, solution, indent, false );

      String path = buildJsStringValue( dataSourceComp.tryGetPropertyValue( PropertyName.PATH, "" ) );
      addJsProperty( out, PropertyName.PATH, path, indent, false );

      String file = buildJsStringValue( dataSourceComp.tryGetPropertyValue( PropertyName.FILE, "" ) );
      addJsProperty( out, PropertyName.FILE, file, indent, false );
    }

    String dataSourceType = dataSourceComp.tryGetAttributeValue( DATA_ACCESS_TYPE, null );
    addJsProperty( out, PropertyName.PUSH_ENABLED,
      String.valueOf( STREAMING_TYPE.equals( dataSourceType ) ),
      indent, false );
  }

  protected void renderBuiltinCdaDatasource(
    StringBuilder out,
    CdfRunJsDashboardWriteContext context,
    DataSourceComponent dataSourceComp ) {
    String indent = context.getIndent();

    addJsProperty( out, PropertyName.DATA_ACCESS_ID, buildJsStringValue( dataSourceComp.getName() ), indent,
      context.isFirstInList() );

    context.setIsFirstInList( false );

    String cdeFilePath = context.getDashboard().getSourcePath();
    if ( cdeFilePath.contains( ".wcdf" ) ) {
      logger.error( "renderBuiltinCdaDatasource: [fileName] receiving a .wcdf when a .cdfde was expected!" );
      cdeFilePath = cdeFilePath.replace( ".wcdf", ".cda" );
    }

    String cdaFilePath = cdeFilePath.replaceAll( ".cdfde", ".cda" );

    addJsProperty( out, PropertyName.PATH, JsonUtils.toJsString( cdaFilePath ), indent, false );

    String dataSourceType = dataSourceComp.tryGetAttributeValue( DATA_ACCESS_TYPE, null );
    addJsProperty( out, PropertyName.PUSH_ENABLED,
      String.valueOf( STREAMING_TYPE.equals( dataSourceType ) ),
      indent, false );
  }

  // ---------------------

  protected void renderCpkDatasource(
    StringBuilder out,
    CdfRunJsDashboardWriteContext context,
    DataSourceComponent dataSourceComp ) {

    String indent = context.getIndent();

    DataSourceComponentType compType = dataSourceComp.getMeta();

    String endPoint = buildJsStringValue( compType.tryGetAttributeValue( PropertyName.ENDPOINT, "" ) );
    addJsProperty( out, PropertyName.ENDPOINT, endPoint, indent, context.isFirstInList() );
    context.setIsFirstInList( false );

    String pluginId = buildJsStringValue( compType.tryGetAttributeValue( PropertyName.PLUGIN_ID, "" ) );
    addJsProperty( out, PropertyName.PLUGIN_ID, pluginId, indent, false );

    String stepName = dataSourceComp.tryGetPropertyValueByName( PropertyName.KETTLE_OUTPUT_STEP_NAME, "OUTPUT" );
    addJsProperty( out, PropertyName.KETTLE_OUTPUT_STEP_NAME, buildJsStringValue( stepName ), indent, false );

    String kettleOutput = dataSourceComp.tryGetPropertyValueByName( PropertyName.KETTLE_OUTPUT_FORMAT, "Infered" );
    addJsProperty( out, PropertyName.KETTLE_OUTPUT_FORMAT, buildJsStringValue( kettleOutput ), indent, false );

    addJsProperty( out, PropertyName.QUERY_TYPE, JsonUtils.toJsString( PropertyValue.CPK_QUERY_TYPE ), indent, false );
  }

  protected void renderDatasource(
    StringBuilder out,
    CdfRunJsDashboardWriteContext context,
    DataSourceComponent dataSourceComp ) {

    String indent = context.getIndent();

    String jndi = buildJsStringValue( dataSourceComp.tryGetPropertyValue( PropertyName.JNDI, "" ) );
    addJsProperty( out, PropertyName.JNDI, jndi, indent, context.isFirstInList() );
    context.setIsFirstInList( false );

    String catalog = buildJsStringValue( dataSourceComp.tryGetPropertyValue( PropertyName.CATALOG, "" ) );
    addJsProperty( out, PropertyName.CATALOG, catalog, indent, false );

    String cube = buildJsStringValue( dataSourceComp.tryGetPropertyValue( PropertyName.CUBE, "" ) );
    addJsProperty( out, PropertyName.CUBE, cube, indent, false );


    String query = dataSourceComp.tryGetPropertyValue( PropertyName.MDX_QUERY, null );
    String queryType;
    if ( query != null ) {
      queryType = PropertyValue.MDX_QUERY_TYPE;
    } else {
      queryType = PropertyValue.SQL_QUERY_TYPE;
      query = dataSourceComp.tryGetPropertyValue( PropertyName.SQL_QUERY, null );
    }

    if ( query != null ) {
      query = writeFunction( query );
    }

    addJsProperty( out, PropertyName.QUERY, replaceParameters( query ), indent, false );
    addJsProperty( out, PropertyName.QUERY_TYPE, JsonUtils.toJsString( queryType ), indent, false );
  }
}
