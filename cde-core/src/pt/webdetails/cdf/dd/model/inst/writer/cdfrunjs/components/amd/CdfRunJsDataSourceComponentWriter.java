/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.*;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.INDENT1;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.INDENT2;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.NEWLINE;

public class CdfRunJsDataSourceComponentWriter extends JsWriterAbstract implements IThingWriter {
  protected static final Log logger = LogFactory.getLog( CdfRunJsDataSourceComponentWriter.class );

  public void write( Object output, IThingWriteContext context, Thing t ) throws ThingWriteException {
    this.write( (StringBuilder) output, (CdfRunJsDashboardWriteContext) context, (DataSourceComponent) t );
  }

  public void write( StringBuilder out, CdfRunJsDashboardWriteContext context, DataSourceComponent comp )
    throws ThingWriteException {

    out.append( "{" ).append( NEWLINE );

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
      } else if ( metaType.equals( META_TYPE_CDA ) ) {
        renderBuiltinCdaDatasource( out, context, comp );
      } else if ( metaType.equals( META_TYPE_CPK ) ) {
        renderCpkDatasource( out, comp );
      } else {
        throw new ThingWriteException( "Cannot render a data source property of meta type '" + metaType + "'." );
      }
    }

    out.append( NEWLINE ).append( INDENT1 ).append( "}" );
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
        value =
          value.replace( matcher.group(), "Utils.ev(" + parameter.substring( 2, parameter.length() - 1 ) + ")" );
      }
    }
    return value;
  }

  private void renderCdaDatasource( StringBuilder out,
                                      CdfRunJsDashboardWriteContext context,
                                      DataSourceComponent dataSourceComp,
                                      String dataAccessId ) {

    this.renderCdaDatasource( out, dataSourceComp, dataAccessId, context.getDashboard().getSourcePath() );
  }

  private void renderCdaDatasource( StringBuilder out,
                                    DataSourceComponent dataSourceComp,
                                    String dataAccessId,
                                    String dashPath ) {

    addJsProperty( out, PropertyName.DATA_ACCESS_ID, buildJsStringValue( dataAccessId ), INDENT2, true );

    String outputIndexId = dataSourceComp.tryGetPropertyValue( PropertyName.OUTPUT_INDEX_ID, null );
    if ( outputIndexId != null ) {
      addJsProperty( out, PropertyName.OUTPUT_INDEX_ID, buildJsStringValue( outputIndexId ), INDENT2, false );
    }

    // Check if we have a cdaFile
    String cdaPath = dataSourceComp.tryGetPropertyValue( PropertyName.CDA_PATH, null );
    if ( cdaPath != null ) {

      // Check if path is relative
      if ( !cdaPath.startsWith( "/" ) ) {
        dashPath = FilenameUtils.getPath( dashPath );
        cdaPath = RepositoryHelper.normalize( Util.joinPath( dashPath, cdaPath ) );
      }
      addJsProperty( out, PropertyName.PATH, buildJsStringValue( cdaPath ), INDENT2, false );

    } else {

      // legacy
      addJsProperty(
          out,
          PropertyName.SOLUTION,
          buildJsStringValue( dataSourceComp.tryGetPropertyValue( PropertyName.SOLUTION, "" ) ),
          INDENT2,
          false );

      addJsProperty(
          out,
          PropertyName.PATH,
          buildJsStringValue( dataSourceComp.tryGetPropertyValue( PropertyName.PATH, "" ) ),
          INDENT2,
          false );

      addJsProperty(
          out,
          PropertyName.FILE,
          buildJsStringValue( dataSourceComp.tryGetPropertyValue( PropertyName.FILE, "" ) ),
          INDENT2,
          false );
    }
  }

  private void renderBuiltinCdaDatasource( StringBuilder out,
                                           CdfRunJsDashboardWriteContext context,
                                           DataSourceComponent dataSourceComp ) {

    addJsProperty( out, PropertyName.DATA_ACCESS_ID, buildJsStringValue( dataSourceComp.getName() ), INDENT2, true );

    String cdeFilePath = context.getDashboard().getSourcePath();
    if ( cdeFilePath != null ) {
      if ( cdeFilePath.contains( ".wcdf" ) ) {
        logger.error( "renderBuiltinCdaDatasource: [fileName] receiving a .wcdf when a .cdfde was expected!" );
        cdeFilePath = cdeFilePath.replace( ".wcdf", ".cda" );
      }

      addJsProperty(
          out,
          PropertyName.PATH,
          JsonUtils.toJsString( cdeFilePath.replaceAll( ".cdfde", ".cda" ) ),
          INDENT2,
          false );

    } else {
      logger.warn( "Error reading dashboard source path" );

      addJsProperty( out, PropertyName.PATH, JsonUtils.toJsString( null ), INDENT2, false );
    }


  }

  // ---------------------

  private void renderCpkDatasource( StringBuilder out, DataSourceComponent dataSourceComp ) {

    DataSourceComponentType compType = dataSourceComp.getMeta();

    addJsProperty(
        out,
        PropertyName.ENDPOINT,
        buildJsStringValue( compType.tryGetAttributeValue( PropertyName.ENDPOINT, "" ) ),
        INDENT2,
        true );

    addJsProperty(
        out,
        PropertyName.PLUGIN_ID,
        buildJsStringValue( compType.tryGetAttributeValue( PropertyName.PLUGIN_ID, "" ) ),
        INDENT2,
        false );

    addJsProperty(
        out,
        PropertyName.KETTLE_OUTPUT_STEP_NAME,
        buildJsStringValue(
          dataSourceComp.tryGetPropertyValueByName( PropertyName.KETTLE_OUTPUT_STEP_NAME, "OUTPUT" )
        ),
        INDENT2,
        false );

    addJsProperty( out, PropertyName.KETTLE_OUTPUT_FORMAT,
        buildJsStringValue(
          dataSourceComp.tryGetPropertyValueByName( PropertyName.KETTLE_OUTPUT_FORMAT, "Infered" )
        ),
        INDENT2,
        false );

    addJsProperty( out, PropertyName.QUERY_TYPE, JsonUtils.toJsString( PropertyValue.CPK_QUERY_TYPE ), INDENT2, false );
  }

  private void renderDatasource( StringBuilder out, DataSourceComponent dataSourceComp ) {

    addJsProperty(
        out,
        PropertyName.JNDI,
        buildJsStringValue( dataSourceComp.tryGetPropertyValue( PropertyName.JNDI, "" ) ),
        INDENT2,
        true );

    addJsProperty(
        out,
        PropertyName.CATALOG,
        buildJsStringValue( dataSourceComp.tryGetPropertyValue( PropertyName.CATALOG, "" ) ),
        INDENT2,
        false );

    addJsProperty(
        out,
        PropertyName.CUBE,
        buildJsStringValue(
          dataSourceComp.tryGetPropertyValue( PropertyName.CUBE, "" )
        ),
        INDENT2,
        false );


    String query = dataSourceComp.tryGetPropertyValue( PropertyName.MDX_QUERY, null );
    final String queryType;
    if ( query != null ) {
      queryType = PropertyValue.MDX_QUERY_TYPE;
      query = replaceParameters( writeFunction( query ) );
    } else {
      queryType = PropertyValue.SQL_QUERY_TYPE;
      query = buildJsStringValue( dataSourceComp.tryGetPropertyValue( PropertyName.SQL_QUERY, null ) );
    }

    addJsProperty( out, PropertyName.QUERY, query, INDENT2, false );
    addJsProperty( out, PropertyName.QUERY_TYPE, JsonUtils.toJsString( queryType ), INDENT2, false );
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
