/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.amd;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;
import pt.webdetails.cdf.dd.render.ResourceMap;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.Utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static pt.webdetails.cdf.dd.CdeConstants.Writer.NEWLINE;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DEFINE_START;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_START_EMPTY_ALIAS;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_NORMALIZE_ALIAS;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_GET_MESSAGES_PATH;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_START;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_LAYOUT;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_RENDERER;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_SETUP_DOM;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_PROCESS_COMPONENTS;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_STOP;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DEFINE_STOP;

public class CdfRunJsDashboardModuleWriter extends CdfRunJsDashboardWriter {
  protected static Log logger = LogFactory.getLog( CdfRunJsDashboardModuleWriter.class );

  public CdfRunJsDashboardModuleWriter( DashboardWcdfDescriptor.DashboardRendererType type ) {
    super( type );
  }

  /**
   * Writes the dashboard module to a provided builder object.
   *
   * @param builder the builder object to where the processed dashboard will be stored
   * @param context the dashboard context.
   * @param dashboard the dashboard to write.
   * @throws ThingWriteException
   */
  @Override
  public void write( CdfRunJsDashboardWriteResult.Builder builder, CdfRunJsDashboardWriteContext context,
                     Dashboard dashboard ) throws ThingWriteException {

    assert dashboard == context.getDashboard();

    // content resources
    ResourceMap resources;
    try {
      final JXPathContext layoutJXPContext = dashboard.getLayout( "TODO" ).getLayoutXPContext();
      final String aliasPrefix = context.getOptions().getAliasPrefix();

      resources = getResourceRenderer( layoutJXPContext, context ).renderResources( aliasPrefix );
    } catch ( Exception ex ) {
      throw new ThingWriteException( "Error rendering resources.", ex );
    }

    // content layout, prepend the CSS code snippets
    final String layout;
    try {
      final String cssResourcesContent = this.writeCssCodeResources( resources );
      final String layoutContent = this.writeLayout( context, dashboard );

      layout = context.replaceTokensAndAlias( cssResourcesContent + layoutContent );
    } catch ( Exception ex ) {
      throw new ThingWriteException( "Error rendering layout", ex );
    }

    StringBuilder out = new StringBuilder();

    // content wcdf settings, write WCDF settings
    final String wcdfSettings = writeWcdfSettings( dashboard );

    // content components, get component AMD modules and write the components to the StringBuilder
    final Map<String, String> componentModules = this.writeComponents( context, dashboard, out );
    final String components = replaceAliasTagWithAlias(
      context.replaceHtmlAlias( context.replaceTokens( out.toString() ) )
    );

    // content
    final String content = wrapRequireModuleDefinitions(
      layout, resources, componentModules, wcdfSettings + components, context
    );

    // Export
    builder
      .setTemplate( "" )
      .setHeader( "" )
      .setLayout( layout )
      .setComponents( components )
      .setContent( content )
      .setFooter( "" )
      .setLoadedDate( context.getDashboard().getSourceDate() );
  }
  /**
   * Replaces all alias tags contained in the provided content string.
   *
   * @param content the string containing some JavaScript sourcecode
   * @return the string with all alias tags replaced with the appropriate sourcecode
   */
  protected String replaceAliasTagWithAlias( String content ) {
    return content.replaceAll( CdeConstants.DASHBOARD_ALIAS_TAG, "\" + this._alias +\"" );
  }

  /**
   * Wraps the JavaScript code, contained in the input parameters, as a requirejs module definition.
   *
   * @param layout the dashboard's layout HTML sourcecode
   * @param resources the dashboard's resources
   * @param componentModules the dashboard component modules
   * @param content the dashboard generated JavaScript sourcecode to be wrapped
   * @param context the dashboard context
   *
   * @return the string containing the dashboard module definition.
   */
  protected String wrapRequireModuleDefinitions( String layout, ResourceMap resources,
                                                 Map<String, String> componentModules, String content,
                                                 CdfRunJsDashboardWriteContext context ) {

    StringBuilder output = new StringBuilder();

    ArrayList<String> moduleIds = new ArrayList<>();        // AMD module paths
    ArrayList<String> moduleClassNames = new ArrayList<>(); // AMD module class names

    // Add default dashboard module ids and class names
    addDefaultDashboardModules( moduleIds, moduleClassNames );

    // store component AMD modules ids and class names
    Iterator it = componentModules.entrySet().iterator();
    Map.Entry pair;
    while ( it.hasNext() ) {
      pair = (Map.Entry) it.next();
      // Add component AMD module path
      moduleIds.add( (String) pair.getValue() );
      // Add component AMD module class name
      if ( !StringUtils.isEmpty( (String) pair.getKey() ) ) {
        moduleClassNames.add( (String) pair.getKey() );
      }
    }

    // write RequireJS module path configurations for external JS and CSS resources
    Map<String, String> fileResourceModules = writeFileResourcesRequireJSPathConfig( output, resources, context );

    // Add external resource module ids to the list
    moduleIds.addAll( fileResourceModules.keySet() );
    // Add external resource module class names to the list
    moduleClassNames.addAll( fileResourceModules.values() );

    // Output module paths and module class names
    writeRequireJsExecutionFunction( output, moduleIds, moduleClassNames );

    final String dashSourcePath = context.getDashboard().getSourcePath();
    final String noNewlinesLayout = StringEscapeUtils.escapeJavaScript( layout.replace( NEWLINE, "" ) );

    String aliasPrefix = context.getOptions().getAliasPrefix();
    if ( aliasPrefix.contains( CdeConstants.DASHBOARD_ALIAS_TAG ) ) {
      aliasPrefix = aliasPrefix.replace( CdeConstants.DASHBOARD_ALIAS_TAG, "\" + this._alias + \"" );
      output
        .append( MessageFormat.format( DASHBOARD_MODULE_START_EMPTY_ALIAS, noNewlinesLayout ) )
        .append( MessageFormat.format( DASHBOARD_MODULE_NORMALIZE_ALIAS, aliasPrefix ) )
        .append( MessageFormat.format( DASHBOARD_MODULE_GET_MESSAGES_PATH,  getWcdfReposPath( dashSourcePath ) ) );
    } else {
      output
        .append( DASHBOARD_MODULE_START )
        .append( MessageFormat.format( DASHBOARD_MODULE_LAYOUT, noNewlinesLayout ) )
        .append( MessageFormat.format( DASHBOARD_MODULE_NORMALIZE_ALIAS, aliasPrefix ) )
        .append( MessageFormat.format( DASHBOARD_MODULE_GET_MESSAGES_PATH, getWcdfReposPath( dashSourcePath ) ) );
    }

    final String jsCodeSnippets = writeJsCodeResources( resources );

    content = jsCodeSnippets.length() > 0 ? jsCodeSnippets + NEWLINE + content : content;

    output
      .append( DASHBOARD_MODULE_RENDERER ).append( NEWLINE )
      .append( DASHBOARD_MODULE_SETUP_DOM ).append( NEWLINE )
      .append( MessageFormat.format( DASHBOARD_MODULE_PROCESS_COMPONENTS, content ) )
      .append( DASHBOARD_MODULE_STOP ).append( NEWLINE )
      .append( DEFINE_STOP );

    return output.toString();
  }

  /**
   * Writes the RequireJS 'define' JavaScript function sourcecode to the given string builder.
   *
   * @param out the string builder to where the sourcecode will be written
   * @param ids the array list containing all module ids
   * @param classNames the array list containing all module class names
   */
  @Override
  protected void writeRequireJsExecutionFunction( StringBuilder out, List<String> ids, List<String> classNames ) {
    // remove empty external resource module class names from the list
    classNames.removeIf( StringUtils::isEmpty );

    // Output module paths and module class names
    out.append( MessageFormat.format( DEFINE_START,
        StringUtils.join( ids, "', '" ),
        StringUtils.join( classNames, ", " ) ) );
  }

  String getWcdfReposPath( String path ) {
    if ( StringUtils.isEmpty( path ) ) {
      return "undefined";
    }

    return "\"" + replaceCdfdeExtension( Utils.getWcdfReposPath( path ) ) + "/\"";
  }

  String replaceCdfdeExtension( String path ) {
    if ( path.endsWith( ".cdfde" ) ) {
      return path.substring( 0, path.lastIndexOf( ".cdfde" )  ) + ".wcdf";
    }

    return path;
  }
}
