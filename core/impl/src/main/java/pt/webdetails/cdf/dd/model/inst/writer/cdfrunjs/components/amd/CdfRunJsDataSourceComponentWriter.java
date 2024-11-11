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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.meta.DataSourceComponentType;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pt.webdetails.cdf.dd.CdeConstants.Writer;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.AttributeName.DATA_ACCESS_TYPE;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.PropertyName;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.PropertyName.STREAMING_TYPE;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.PropertyValue;

public class CdfRunJsDataSourceComponentWriter extends JsWriterAbstract implements IThingWriter {
  protected static final Log logger = LogFactory.getLog( CdfRunJsDataSourceComponentWriter.class );

  public void write( Object output, IThingWriteContext context, Thing t ) throws ThingWriteException {
    this.write( (StringBuilder) output, (CdfRunJsDashboardWriteContext) context, (DataSourceComponent) t );
  }

  public void write( StringBuilder out, CdfRunJsDashboardWriteContext context, DataSourceComponent comp )
    throws ThingWriteException {

    out.append( "{" ).append( Writer.NEWLINE );

    // write properties
    String dataAccessId = comp.tryGetPropertyValue( PropertyName.DATA_ACCESS_ID, null );
    if ( dataAccessId != null ) {
      renderCdaDatasource( out, context, comp, dataAccessId );
    } else {
      // "meta" attribute has the value "CDA", "CPK" ?
      // See DataSourceModelReader#readDataSourceComponent
      String metaType = comp.getMeta().tryGetAttributeValue( "", "" );
      if ( StringUtils.isEmpty( metaType ) ) {
        renderDatasource( out, comp );
      } else if ( metaType.equals( DataSource.META_TYPE_CDA ) ) {
        renderBuiltinCdaDatasource( out, context, comp );
      } else if ( metaType.equals( DataSource.META_TYPE_CPK ) ) {
        renderCpkDatasource( out, comp );
      } else if ( metaType.equals( DataSource.META_TYPE_SOLR ) ) {
        renderSolrDataSource( out, comp );
      } else {
        throw new ThingWriteException( "Cannot render a data source property of meta type '" + metaType + "'." );
      }
    }

    out.append( Writer.NEWLINE ).append( Writer.INDENT1 ).append( "}" );
  }

  private static String buildJsStringValue( String value ) {
    return JsonUtils.toJsString( replaceParameters( value == null ? "" : value ) );
  }

  private static final Pattern _replaceParametersPattern = Pattern.compile( "\\$\\{[^}]*\\}" );

  /**
   * Check if the parameter provided is wrapped with a ${} special token and replace such token with the Utils.ev
   * client-side JavaScript function.
   *
   * @param value the function/parameter name that might be wrapped with a special token value
   * @return the function/parameter name wrapped with the Utils.ev function that returns the function call or the parameter
   */
  private static String replaceParameters( String value ) {
    // TODO: is this replacement pattern deprecated, e.g. ${param1} vs ${p:param1}?
    if ( value != null ) {
      Matcher matcher = _replaceParametersPattern.matcher( value );
      while ( matcher.find() ) {
        String parameter = matcher.group();
        value = value
          .replace( matcher.group(), "Utils.ev(" + parameter.substring( 2, parameter.length() - 1 ) + ")" );
      }
    }
    return value;
  }

  private void renderSolrDataSource( StringBuilder out, DataSourceComponent dataSourceComp ) {
    String queryType = PropertyValue.SOLR_QUERY_TYPE;
    addFirstJsProperty( out, PropertyName.QUERY_TYPE, JsonUtils.toJsString( queryType ), Writer.INDENT2 );

    dataSourceComp.getPropertyBindings().forEach( binding -> {
      String name = binding.getAlias();
      String value = binding.getValue();

      PropertyType.ValueType valueType = binding.getProperty().getValueType();
      if ( PropertyType.ValueType.STRING.equals( valueType ) ) {
        value = JsonUtils.toJsString( value );
      }

      addJsProperty( out, name, value, Writer.INDENT2 );
    } );
  }

  private void renderCdaDatasource( StringBuilder out, CdfRunJsDashboardWriteContext context,
                                    DataSourceComponent dataSourceComp, String dataAccessId ) {

    this.renderCdaDatasource( out, dataSourceComp, dataAccessId, context.getDashboard().getSourcePath() );
  }

  private void renderCdaDatasource( StringBuilder out, DataSourceComponent dataSourceComp, String dataAccessId,
                                    String dashPath ) {

    addFirstJsProperty( out, PropertyName.DATA_ACCESS_ID, buildJsStringValue( dataAccessId ), Writer.INDENT2 );

    String dataSourceType = dataSourceComp.tryGetAttributeValue( DATA_ACCESS_TYPE, null );
    addJsProperty( out, PropertyName.DATA_ACCESS_PUSH_ENABLED, String.valueOf( STREAMING_TYPE.equals( dataSourceType ) ), Writer.INDENT2 );

    String outputIndexId = dataSourceComp.tryGetPropertyValue( PropertyName.OUTPUT_INDEX_ID, null );
    if ( outputIndexId != null ) {
      addJsProperty( out, PropertyName.OUTPUT_INDEX_ID, buildJsStringValue( outputIndexId ), Writer.INDENT2 );
    }

    // Check if we have a cdaFile
    String cdaPath = dataSourceComp.tryGetPropertyValue( PropertyName.CDA_PATH, null );
    if ( cdaPath != null ) {

      // Check if path is relative
      if ( !cdaPath.startsWith( "/" ) ) {
        dashPath = FilenameUtils.getPath( dashPath );
        cdaPath = RepositoryHelper.normalize( Util.joinPath( dashPath, cdaPath ) );
      }

      addJsProperty( out, PropertyName.PATH, buildJsStringValue( cdaPath ), Writer.INDENT2 );

    } else {

      // legacy
      String solution = dataSourceComp.tryGetPropertyValue( PropertyName.SOLUTION, "" );
      addJsProperty( out, PropertyName.SOLUTION, buildJsStringValue( solution ), Writer.INDENT2 );

      String path = dataSourceComp.tryGetPropertyValue( PropertyName.PATH, "" );
      addJsProperty( out, PropertyName.PATH, buildJsStringValue( path ), Writer.INDENT2 );

      String file = dataSourceComp.tryGetPropertyValue( PropertyName.FILE, "" );
      addJsProperty( out, PropertyName.FILE, buildJsStringValue( file ), Writer.INDENT2 );
    }
  }

  private void renderBuiltinCdaDatasource( StringBuilder out, CdfRunJsDashboardWriteContext context,
                                           DataSourceComponent dataSourceComp ) {

    String dsName = dataSourceComp.getName();
    addFirstJsProperty( out, PropertyName.DATA_ACCESS_ID, buildJsStringValue( dsName ), Writer.INDENT2 );

    String dataSourceType = dataSourceComp.tryGetAttributeValue( DATA_ACCESS_TYPE, null );
    addJsProperty( out, PropertyName.DATA_ACCESS_PUSH_ENABLED, String.valueOf( STREAMING_TYPE.equals( dataSourceType ) ), Writer.INDENT2 );

    String cdeFilePath = context.getDashboard().getSourcePath();
    if ( cdeFilePath != null ) {
      if ( cdeFilePath.contains( ".wcdf" ) ) {
        logger.error( "renderBuiltinCdaDatasource: [fileName] receiving a .wcdf when a .cdfde was expected!" );
        cdeFilePath = cdeFilePath.replace( ".wcdf", ".cda" );
      } else {
        cdeFilePath = cdeFilePath.replace( ".cdfde", ".cda" );
      }

    } else {
      logger.warn( "Error reading dashboard source path" );

    }

    addJsProperty( out, PropertyName.PATH, buildJsStringValue( cdeFilePath ), Writer.INDENT2 );

  }

  // ---------------------

  private void renderCpkDatasource( StringBuilder out, DataSourceComponent dataSourceComp ) {
    DataSourceComponentType compType = dataSourceComp.getMeta();

    String endpoint = compType.tryGetAttributeValue( PropertyName.ENDPOINT, "" );
    addFirstJsProperty( out, PropertyName.ENDPOINT, buildJsStringValue( endpoint ), Writer.INDENT2 );

    String pluginID = compType.tryGetAttributeValue( PropertyName.PLUGIN_ID, "" );
    addJsProperty( out, PropertyName.PLUGIN_ID, buildJsStringValue( pluginID ), Writer.INDENT2 );

    String outputStepName = dataSourceComp
      .tryGetPropertyValue( PropertyName.KETTLE_OUTPUT_STEP_NAME, "OUTPUT" );
    addJsProperty( out, PropertyName.KETTLE_OUTPUT_STEP_NAME, buildJsStringValue( outputStepName ), Writer.INDENT2 );

    String outputFormat = dataSourceComp
      .tryGetPropertyValue( PropertyName.KETTLE_OUTPUT_FORMAT, "Infered" );
    addJsProperty( out, PropertyName.KETTLE_OUTPUT_FORMAT, buildJsStringValue( outputFormat ), Writer.INDENT2 );

    String queryType = PropertyValue.CPK_QUERY_TYPE;
    addJsProperty( out, PropertyName.QUERY_TYPE, buildJsStringValue( queryType ), Writer.INDENT2 );
  }

  private void renderDatasource( StringBuilder out, DataSourceComponent dataSourceComp ) {

    String jndi = dataSourceComp.tryGetPropertyValue( PropertyName.JNDI, "" );
    addFirstJsProperty( out, PropertyName.JNDI, buildJsStringValue( jndi ), Writer.INDENT2 );

    String catalog = dataSourceComp.tryGetPropertyValue( PropertyName.CATALOG, "" );
    addJsProperty( out, PropertyName.CATALOG, buildJsStringValue( catalog ), Writer.INDENT2 );

    String cube = dataSourceComp.tryGetPropertyValue( PropertyName.CUBE, "" );
    addJsProperty( out, PropertyName.CUBE, buildJsStringValue( cube ), Writer.INDENT2 );

    String query = dataSourceComp.tryGetPropertyValue( PropertyName.MDX_QUERY, null );
    final String queryType;
    if ( query != null ) {
      queryType = PropertyValue.MDX_QUERY_TYPE;
      query = replaceParameters( writeFunction( query ) );
    } else {
      queryType = PropertyValue.SQL_QUERY_TYPE;
      query = buildJsStringValue( dataSourceComp.tryGetPropertyValue( PropertyName.SQL_QUERY, null ) );
    }

    addJsProperty( out, PropertyName.QUERY, query, Writer.INDENT2 );
    addJsProperty( out, PropertyName.QUERY_TYPE, buildJsStringValue( queryType ), Writer.INDENT2 );
  }

  private static final Pattern _maybeWrappedFunctionValue = Pattern.compile( "(\\\"|\\s)*function\\s*\\u0028.*\\u0029{1}\\s*\\u007b.*(\\u007d(\\\"|\\s)*)?|(\\\"|\\s)*function\\s*[a-zA-Z0-9\\u002d\\u005f]+\\u0028.*\\u0029{1}\\s*\\u007b.*(\\u007d(\\\"|\\s)*)?" );

  private String writeFunction( String canonicalValue ) {
    // See comments on _maybeWrappedFunctionValue.
    Matcher matcher = _maybeWrappedFunctionValue.matcher( canonicalValue );
    if ( matcher.find() ) {
      // TODO: It's a function already, but possibly wrapped...
      // Output it wrapped???
      return canonicalValue;
    }

    // ASSUME it's a ~literal~ string expression.
    // Compile into a function that builds/evaluates the string in runtime.

    // 1. remove all newlines
    canonicalValue = canonicalValue.replace( "\n", " " ).replace( "\r", " " );

    // 2. escape «"»
    canonicalValue = canonicalValue.replace( "\"", "\\\"" );

    // 3. change ${} with " + ${} + "  (assuming it's in the middle of a string)
    canonicalValue = canonicalValue.replaceAll( "(\\$\\{[^}]*\\})", "\"+ $1 + \"" );

    // 4 -> return a function with the expression «function() { return "..."; }»
    return "function() { return \"" + canonicalValue + "\"; }";
  }
}
