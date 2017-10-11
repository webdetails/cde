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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.amd;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import pt.webdetails.cdf.dd.CdeConstants;
import static pt.webdetails.cdf.dd.CdeConstants.AmdModule;
import static pt.webdetails.cdf.dd.CdeConstants.RequireJSPlugin;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.*;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;
import pt.webdetails.cdf.dd.render.RenderLayout;
import pt.webdetails.cdf.dd.render.RenderResources;
import pt.webdetails.cdf.dd.render.Renderer;
import pt.webdetails.cdf.dd.render.ResourceMap;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.Util;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

public class CdfRunJsDashboardWriter extends JsWriterAbstract implements IThingWriter {
  protected static final Log logger = LogFactory.getLog( CdfRunJsDashboardWriter.class );

  private DashboardWcdfDescriptor.DashboardRendererType type;

  public CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType type ) {
    super();
    this.type = type;
  }

  /**
   * Writes the dashboard to a provided builder object.
   *
   * @param output the builder object to where the processed dashboard will be stored
   * @param context the dashboard context.
   * @param t the dashboard to write.
   * @throws ThingWriteException
   */
  public void write( Object output, IThingWriteContext context, Thing t ) throws ThingWriteException {
    this.write( (CdfRunJsDashboardWriteResult.Builder) output, (CdfRunJsDashboardWriteContext) context, (Dashboard) t );
  }

  /**
   * Writes the dashboard to a provided builder object.
   *
   * @param builder the builder object to where the processed dashboard will be stored
   * @param ctx the dashboard context.
   * @param dash the dashboard to write.
   * @throws ThingWriteException
   */
  public void write( CdfRunJsDashboardWriteResult.Builder builder, CdfRunJsDashboardWriteContext ctx, Dashboard dash )
    throws ThingWriteException {
    assert dash == ctx.getDashboard();

    DashboardWcdfDescriptor wcdf = dash.getWcdf();

    // header
    final String header = ctx.replaceTokens( writeHeaders( dash ) );

    // content resources
    ResourceMap resources;
    try {
      resources = getResourceRenderer( dash.getLayout( "TODO" ).getLayoutXPContext(), ctx )
          .renderResources( ctx.getOptions().getAliasPrefix() );
    } catch ( Exception ex ) {
      throw new ThingWriteException( "Error rendering resources.", ex );
    }

    // content layout
    final String layout;
    try {
      layout = ctx.replaceTokensAndAlias( this.writeCssCodeResources( resources ) + this.writeLayout( ctx, dash ) );
    } catch ( Exception ex ) {
      throw new ThingWriteException( "Error rendering layout", ex );
    }

    StringBuilder out = new StringBuilder();
    // content dashboard wcdf settings, write WCDF settings
    final String wcdfSettings = writeWcdfSettings( dash );
    // content dashboard components, write component AMD modules and add them to the componentModules map
    final Map<String, String> componentModules = writeComponents( ctx, dash, out );
    final String components = ctx.replaceTokensAndAlias( out.toString() );
    // content
    final String content = writeContent( resources, layout, componentModules, wcdfSettings + components, ctx );

    // footer
    final String footer;
    try {
      footer = Util.toString(
          CdeEnvironment.getPluginSystemReader().getFileInputStream( CdeConstants.RESOURCE_FOOTER_REQUIRE ) );
    } catch ( IOException ex ) {
      throw new ThingWriteException( "Could not read footer file.", ex );
    }

    // template
    String template;
    try {
      template = ctx.replaceTokens( Utils.readTemplate( wcdf ) );
    } catch ( IOException ex ) {
      throw new ThingWriteException( "Could not read style template file.", ex );
    }
    // Leave the DASHBOARD_HEADER_TAG to replace additional stuff on render.
    template = template
      .replaceAll( CdeConstants.DASHBOARD_HEADER_TAG,
        Matcher.quoteReplacement( header ) + CdeConstants.DASHBOARD_HEADER_TAG )
      .replaceAll( CdeConstants.DASHBOARD_CONTENT_TAG, Matcher.quoteReplacement( content ) )
      .replaceAll( CdeConstants.DASHBOARD_FOOTER_TAG, Matcher.quoteReplacement( footer ) );

    // Export
    builder
      .setHeader( header )
      .setLayout( layout )
      .setComponents( components )
      .setContent( content )
      .setFooter( footer )
      .setTemplate( template )
      .setLoadedDate( dash.getSourceDate() );
  }

  /**
   * Return a string containing the dashboard HTML layout.
   *
   * @param context the dashboard context
   * @param dash the dashboard to write
   * @return the string containing the dashboard's layout
   */
  protected String writeLayout( CdfRunJsDashboardWriteContext context, Dashboard dash ) throws Exception {
    if ( dash.getLayoutCount() > 0 ) {
      JXPathContext docXP = dash.getLayout( "TODO" ).getLayoutXPContext();
      return getLayoutRenderer( docXP, context ).render( context.getOptions().getAliasPrefix() );
    } else {
      logger.warn( "Unable to render layout: no layout found." );
      return "";
    }
  }

  /**
   * Returns a layout renderer that allows to render the dashboard's HTML layout to a string.
   *
   * @param docXP the JXPathContext object
   * @param context the dashboard context
   * @return the generated layout renderer object
   */
  protected Renderer getLayoutRenderer( JXPathContext docXP, CdfRunJsDashboardWriteContext context ) {
    return new RenderLayout( docXP, context, false );
  }

  /**
   * Returns a string with the dashboard's CSS code snippet resources contained in HTML style tags.
   *
   * @param resources the dashboard's resources
   * @return the string containing the CSS code snippet resources
   */
  protected String writeCssCodeResources( ResourceMap resources ) {

    StringBuilder out = new StringBuilder();
    //CSS
    for ( ResourceMap.Resource resource : resources.getCssResources() ) {
      if ( resource.getResourceType().equals( ResourceMap.ResourceType.CODE ) ) {
        out.append( resource.getProcessedResource() ).append( NEWLINE );
      }
    }

    return out.toString();
  }

  /**
   * Returns a string containing the JavaScript code snippet resources.
   *
   * @param resources the dashboard's resources
   * @return the string containing the processed JavaScript code snippet resources
   */
  protected String writeJsCodeResources( ResourceMap resources ) {
    //JS
    StringBuilder sb = new StringBuilder();
    for ( ResourceMap.Resource resource : resources.getJavascriptResources() ) {
      if ( resource.getResourceType().equals( ResourceMap.ResourceType.CODE ) ) {
        sb.append( resource.getProcessedResource() ).append( NEWLINE );
      }
    }

    return sb.toString();
  }

  /**
   * Returns a resource renderer that allows to render the dashboard's resources.
   *
   * @param docXP the JXPathContext object
   * @param context the dashboard context
   * @return the generated resource renderer object
   */
  protected RenderResources getResourceRenderer( JXPathContext docXP, CdfRunJsDashboardWriteContext context ) {
    return new RenderResources( docXP, context );
  }

  /**
   * Returns a string containing the dashboard WCDF settings of the provided dashboard.
   *
   * @param dash the dashboard
   * @return the WCDF settings for the provided dashboard
   * @throws ThingWriteException
   */
  protected String writeWcdfSettings( Dashboard dash ) throws ThingWriteException {

    StringBuilder wcdfSettings = new StringBuilder();
    DashboardWcdfDescriptor wcdf = dash.getWcdf();

    // Output WCDF
    try {
      return MessageFormat.format( GET_WCDF_SETTINGS_FUNCTION, wcdf.toJSON().toString( 6 ) );
    } catch ( JSONException ex ) {
      throw new ThingWriteException( "Converting wcdf to json", ex );
    }
  }

  /**
   * Writes the dashboard components sourcecode to the provided StringBuilder
   * and returns the module ids and class names of the valid component modules found.
   *
   * @param context the dashboard context
   * @param dash the dashboard
   * @param out the StringBuilder to where the components sourcecode will be written
   * @return the Map containing the processed component module ids and class names
   * @throws ThingWriteException
   */
  protected Map<String, String> writeComponents(
      CdfRunJsDashboardWriteContext context,
      Dashboard dash,
      StringBuilder out ) throws ThingWriteException {


    IThingWriterFactory factory = context.getFactory();
    StringBuilder tmp = new StringBuilder();

    // write data source components
    // store data source
    //tmp.setLength( 0 );
    Iterable<DataSourceComponent> dataSourceComps = dash.getDataSources();
    for ( DataSourceComponent comp : dataSourceComps ) {
      if ( StringUtils.isNotEmpty( comp.getName() ) ) {
        IThingWriter writer;
        try {
          writer = factory.getWriter( comp );
          tmp.setLength( 0 );
        } catch ( UnsupportedThingException ex ) {
          throw new ThingWriteException( ex );
        }

        out.append( MessageFormat.format( DASHBOARD_ADD_DATA_SOURCE_INIT, comp.getName() ) );
        writer.write( tmp, context, comp );
        out.append( MessageFormat.format( DASHBOARD_ADD_DATA_SOURCE_END, tmp.toString() ) );
      }
    }

    // write regular components
    Map<String, String> componentModules = new LinkedHashMap<String, String>();
    // store component ids
    tmp.setLength( 0 );
    Iterable<Component> comps = dash.getRegulars();
    for ( Component comp : comps ) {
      if ( StringUtils.isNotEmpty( comp.getName() ) ) {
        IThingWriter writer;
        try {
          writer = factory.getWriter( comp );
        } catch ( UnsupportedThingException ex ) {
          throw new ThingWriteException( ex );
        }

        // custom primitive widget (generic components) & layout component
        if ( comp.isVisualComponent() ) {
          if ( comp.isCustomComponent() || comp.isPrimitiveComponent() ) {
            String componentClassName = comp.getComponentClassName();

            if ( !componentModules.containsKey( componentClassName ) ) {
              String componentModuleId = writeComponentModuleId( comp, componentClassName );
              if ( StringUtils.isEmpty( componentModuleId ) ) {
                continue;
              }
              componentModules.put( componentClassName, componentModuleId );
            }
          }

          if ( tmp.length() > 0 ) {
            tmp.append( ", " );
          }
          tmp.append( comp.getId() );
        }

        writer.write( out, context, comp );
      }
    }

    if ( tmp.length() > 0 ) {
      out.append( MessageFormat.format( DASHBOARD_ADD_COMPONENTS, tmp.toString() ) );
    }

    return componentModules;
  }

  /**
   * Returns a string containing the module id given a component and a class name.
   *
   * @param comp the component used to build the module id
   * @param className the class name used to build the module id
   * @return the string containing the component module id
   */
  protected String writeComponentModuleId( Component comp, String className ) {
    StringBuilder componentModuleId = new StringBuilder();

    if ( comp.isPrimitiveComponent() && comp.isComponentStaticSystemOrigin() ) {

      // CDF component with a static system origin
      componentModuleId
        .append( CDF_AMD_BASE_COMPONENT_PATH )
        .append( className );

    } else if ( comp.isCustomComponent() ) {

      if ( comp.isComponentStaticSystemOrigin() ) {

        // CDE custom component with a static system origin
        componentModuleId
          .append( CDE_AMD_BASE_COMPONENT_PATH )
          .append( className );

      } else if ( comp.isComponentPluginRepositoryOrigin() ) {

        String compImplPath = comp.getComponentImplementationPath();

        // if both versions are supported or no implementation path is provided
        // build AMD module using source path and component class name
        if ( comp.supportsLegacy() || StringUtils.isEmpty( compImplPath ) ) {

          // assume that the AMD implementation file is in the same folder as component.xml
          // and that it has the same name as the component's class
          compImplPath = comp.getComponentSourcePath().split( CdeConstants.CUSTOM_COMPONENT_CONFIG_FILENAME )[ 0 ]
            + className;

        } else {

          // if it only supports AMD and an implementation path is provided, use it
          compImplPath = compImplPath.substring( 0, compImplPath.lastIndexOf( ".js" ) );
        }

        // validate component's AMD module implementation path
        if ( StringUtils.isEmpty( compImplPath ) ) {
          logger.error( "Missing an implementation code source path for component " + className );
          return "";
        }

        // CDE custom component uploaded to the repository
        componentModuleId
          .append( CDE_AMD_REPO_COMPONENT_PATH )
          .append( compImplPath );

      } else if ( comp.isComponentOtherPluginStaticSystemOrigin() ) {

        // custom component from another plugin (e.g. sparkl)
        componentModuleId
          .append( comp.getPluginIdFromOrigin() )
          .append( PLUGIN_COMPONENT_FOLDER )
          .append( className );
      }
    } else if ( comp.isWidgetComponent() ) {
      // TODO: process WidgetComponent
      logger.error( "Unsupported component: " + className );
      return "";
    }

    return componentModuleId.toString();
  }

  /**
   * Returns a string containing the HTML header sourcecode for the given dashboard.
   *
   * @param dash the dashboard object
   * @return the string that contains the dashboard HTML header sourcecode
   */
  protected String writeHeaders( Dashboard dash ) {

    return MessageFormat.format( TITLE, dash.getWcdf().getTitle() )
      + NEWLINE + MessageFormat.format( SCRIPT, writeWebcontext( "cdf", true ) );
  }

  /**
   * Returns a string containing the URL that allows to import webcontext into the dashboard's HTML page.
   *
   * @param context the URL context query parameter value
   * @param requireJsOnly the URL requireJsOnly query parameter value
   * @return the string containing the URL to the webcontext including query parameters
   */
  protected String writeWebcontext( String context, boolean requireJsOnly ) {
    return MessageFormat.format( WEBCONTEXT, context, Boolean.toString( requireJsOnly ).toLowerCase() );
  }

  /**
   * Returns the dashboard's generated JavaScript sourcecode.
   *
   * @param resources the dashboard resources
   * @param layout the dashboard HTML layout sourcecode.
   * @param componentModules the dashboard components' AMD modules
   * @param components the dashboard's components
   * @param ctx the dashboard context
   * @return the string containing the dashboard's generated JavaScript sourcecode
   */
  protected String writeContent(
      ResourceMap resources,
      String layout,
      Map<String, String> componentModules,
      String components,
      CdfRunJsDashboardWriteContext ctx ) {

    StringBuilder out = new StringBuilder();

    out.append( layout );

    //do the encapsulation stuff here

    wrapJsScriptTags( out, wrapRequireDefinitions( resources, componentModules, components, ctx ) );

    return out.toString();
  }

  /**
   * Wraps the JavaScript code, contained in the input parameter, with requirejs configurations.
   *
   * @param resources the dashboard's resources
   * @param componentModules the dashboard's component modules
   * @param content the dashboard generated JavaScript sourcecode to be wrapped
   * @param ctx the dashboard context
   * @return the dashboard's JavaScript sourcecode
   */
  protected String wrapRequireDefinitions(
      ResourceMap resources,
      Map<String, String> componentModules,
      String content,
      CdfRunJsDashboardWriteContext ctx ) {

    StringBuilder out = new StringBuilder();

    ArrayList<String> moduleIds = new ArrayList<String>(),
        moduleClassNames = new ArrayList<String>();

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

    // write RequireJS module path configurations for JS and CSS file resources
    Map<String, String> fileResourceModules = writeFileResourcesRequireJSPathConfig( out, resources, ctx );

    // Add file resource module ids to the list
    moduleIds.addAll( fileResourceModules.keySet() );
    // Add file resource module class names to the list
    moduleClassNames.addAll( fileResourceModules.values() );

    // Output module paths and module class names
    writeRequireJsExecutionFunction( out, moduleIds, moduleClassNames );

    //write dashboard declaration
    if ( ctx.getOptions().isDebug() ) {
      out.append( DASHBOARD_DECLARATION_DEBUG ).append( NEWLINE );
    } else {
      out.append( DASHBOARD_DECLARATION ).append( NEWLINE );
    }

    // write JS Code snippets
    out.append( writeJsCodeResources( resources ) );

    // write content
    out.append( content ).append( NEWLINE )
      .append( DASHBOARD_INIT )
      .append( REQUIRE_STOP );

    return out.toString();
  }

  /**
   * Adds the default dashboard module ids and class names to the provided lists.
   *
   * @param moduleIds the array list that will hold the default module ids
   * @param moduleClassNames the array list that will hold the default module class names
   */
  protected void addDefaultDashboardModules( ArrayList<String> moduleIds, ArrayList<String> moduleClassNames ) {
    final AmdModule dashboardModule = getDashboardModule();
    // Add default module ids
    moduleIds.add( dashboardModule.getId() );
    moduleIds.add( AmdModule.LOGGER.getId() );
    moduleIds.add( AmdModule.JQUERY.getId() );
    moduleIds.add( AmdModule.UNDERSCORE.getId() );
    moduleIds.add( AmdModule.MOMENT.getId() );
    moduleIds.add( AmdModule.CCC_CDO.getId() );
    moduleIds.add( AmdModule.CCC_DEF.getId() );
    moduleIds.add( AmdModule.CCC_PV.getId() );
    moduleIds.add( AmdModule.CCC_PVC.getId() );
    moduleIds.add( AmdModule.UTILS.getId() );
    // Add default module class names
    moduleClassNames.add( dashboardModule.getClassName() );
    moduleClassNames.add( AmdModule.LOGGER.getClassName() );
    moduleClassNames.add( AmdModule.JQUERY.getClassName() );
    moduleClassNames.add( AmdModule.UNDERSCORE.getClassName() );
    moduleClassNames.add( AmdModule.MOMENT.getClassName() );
    moduleClassNames.add( AmdModule.CCC_CDO.getClassName() );
    moduleClassNames.add( AmdModule.CCC_DEF.getClassName() );
    moduleClassNames.add( AmdModule.CCC_PV.getClassName() );
    moduleClassNames.add( AmdModule.CCC_PVC.getClassName() );
    moduleClassNames.add( AmdModule.UTILS.getClassName() );
  }

  /**
   * Writes the RequireJS 'require' JavaScript function sourcecode to the provided StringBuilder instance.
   *
   * @param out the string builder to where the sourcecode will be written
   * @param ids the array list containing all module ids
   * @param classNames the array list containing all module class names
   */
  protected void writeRequireJsExecutionFunction( StringBuilder out, List<String> ids, List<String> classNames ) {
    // remove file resource module empty class names from the list
    Iterator<String> i = classNames.iterator();
    while ( i.hasNext() ) {
      String className = i.next();
      if ( StringUtils.isEmpty( className ) ) {
        i.remove();
      }
    }
    out
      .append( MessageFormat.format(
        REQUIRE_START,
        StringUtils.join( ids, "'," + NEWLINE + INDENT1 + "'" ),
        StringUtils.join( classNames, "," + NEWLINE + INDENT1 ) ) )
      .append( NEWLINE );
  }

  /**
   * Returns a string containing the dashboard module id generated using the current dashboard type.
   *
   * @return the dashboard module id
   */
  protected AmdModule getDashboardModule() {
    DashboardWcdfDescriptor.DashboardRendererType dashboardType = getType();
    if ( dashboardType.equals( DashboardWcdfDescriptor.DashboardRendererType.BLUEPRINT ) ) {
      return AmdModule.DASHBOARD_BLUEPRINT;
    } else if ( dashboardType.equals( DashboardWcdfDescriptor.DashboardRendererType.BOOTSTRAP ) ) {
      return AmdModule.DASHBOARD_BOOTSTRAP;
    } else if ( dashboardType.equals( DashboardWcdfDescriptor.DashboardRendererType.MOBILE ) ) {
      return AmdModule.DASHBOARD_MOBILE;
    } else {
      return AmdModule.DASHBOARD_CLEAN;
    }
  }

  /**
   * Returns the current dashboard renderer type.
   *
   * @return the dashboard renderer type
   */
  public DashboardWcdfDescriptor.DashboardRendererType getType() {
    return this.type;
  }

  /**
   * Writes the RequireJS module path configuration sourcecode to a given string builder.
   *
   * @param out the string builder to where the RequireJS module path configuration sourcecode will be written
   * @param resources the dashboard's resources
   * @param ctx the dashboard context
   * @return the Map containing the dashboard resource modules ids and class names
   */
  protected Map<String, String> writeFileResourcesRequireJSPathConfig(
      StringBuilder out,
      ResourceMap resources,
      CdfRunJsDashboardWriteContext ctx ) {

    Map<String, String> resourceModules = new LinkedHashMap<String, String>();
    // File Resources with empty names should be placed last in the array dependency list
    Map<String, String> unnamedResourceModules = new LinkedHashMap<String, String>();
    String path;
    StringBuilder id = new StringBuilder();

    // JS
    for ( ResourceMap.Resource resource : resources.getJavascriptResources() ) {
      if ( resource.getResourceType().equals( ResourceMap.ResourceType.FILE ) ) {

        // replace tokens and alias
        path = ctx.replaceTokensAndAlias( resource.getResourcePath() );
        // reset
        id.setLength( 0 );

        // full URI
        if ( SCHEME_PATTERN.matcher( path ).find() ) {

          if ( StringUtils.isEmpty( resource.getResourceName() ) ) {
            id.append( RESOURCE_AMD_NAMESPACE ).append( "/" ).append( getRandomUUID() );
            // store the generated module id and class name to be added at the end of the dependency array
            unnamedResourceModules.put( id.toString(), resource.getResourceName() );
          } else {
            id.append( RESOURCE_AMD_NAMESPACE ).append( "/" ).append( resource.getResourceName() );
            // store the generated module id and class name
            resourceModules.put( id.toString(), resource.getResourceName() );
          }

          // output RequireJS path configuration
          out.append( MessageFormat.format( REQUIRE_PATH_CONFIG_FULL_URI, id, path.replaceFirst( "(?i).js$", "" ) ) )
            .append( NEWLINE );

        } else {

          // normalize the path
          path = Util.normalizeUri( path );
          if ( path.startsWith( "/" ) ) {
            path =  path.replaceFirst( "/", "" );
          }

          // remove file extension
          id.append( RESOURCE_AMD_NAMESPACE ).append( "/" ).append( path.replaceFirst( "(?i).js$", "" ) );

          if ( StringUtils.isEmpty( resource.getResourceName() ) ) {
            // store the generated module id and class name to be added at the end of the dependency array
            unnamedResourceModules.put( id.toString(), resource.getResourceName() );
          } else {
            // store the generated module id and class name, maintain order
            resourceModules.put( id.toString(), resource.getResourceName() );
          }

          // no need for RequireJS path configuration (built based on cde-require-js-cfg.js cde/resources/)
        }
      }
    }

    // CSS
    for ( ResourceMap.Resource resource : resources.getCssResources() ) {
      if ( resource.getResourceType().equals( ResourceMap.ResourceType.FILE ) ) {

        // replace tokens and alias
        path = ctx.replaceTokensAndAlias( resource.getResourcePath() );
        // reset
        id.setLength( 0 );

        // full URI
        if ( SCHEME_PATTERN.matcher( path ).find() ) {

          id.append( RESOURCE_AMD_NAMESPACE )
            .append( "/" )
            .append( ( StringUtils.isEmpty( resource.getResourceName() ) ? getRandomUUID() : resource.getResourceName() ) );
          // output RequireJS path configuration
          out.append( MessageFormat.format( REQUIRE_PATH_CONFIG_FULL_URI, id, path.replaceFirst( "(?i).css$", "" ) ) )
            .append( NEWLINE );

        } else {

          // normalize the path
          path = Util.normalizeUri( path );
          if ( path.startsWith( "/" ) ) {
            path = path.replaceFirst( "/", "" );
          }

          // remove file extension
          id.append( RESOURCE_AMD_NAMESPACE )
            .append( "/" )
            .append( path.replaceFirst( "(?i).css$", "" ) );
          // no need for RequireJS path configuration (built based on cde-require-js-cfg.js cde/resources/)

        }

        // prepend css! RequireJS loader plugin and don't provide a class name for CSS resources
        resourceModules.put( RequireJSPlugin.CSS + id.toString(), "" );

      }
    }

    // write require config function call
    if ( out.length() > 0 ) {
      out.append( REQUIRE_CONFIG ).append( NEWLINE );
    }

    // Finally append the unnamed JS file resources to be added at the end of the dependency array
    resourceModules.putAll( unnamedResourceModules );

    return resourceModules;
  }

  /**
   * Generates a UUID to be used as part of an AMD module id.
   *
   * @return the string containing the random UUID value
   */
  protected String getRandomUUID() {
    return UUID.randomUUID().toString();
  }

  /**
   * Filters AMD module class names, removing any prepended requireJS loader plugin references from them. Excludes
   * CSS resource modules (file resources). These are used as the input parameters of the require function's callback
   * function parameter.
   *
   * @param resources the dashboard resources
   * @return the list of the JavaScript file resource modules
   */
  protected ArrayList<String> getJsModuleClassNames( ResourceMap resources ) {
    ArrayList<String> classNames = new ArrayList<String>();
    String className;
    // Filter and remove known RequireJS Loader plugin from class names
    // JS
    for ( ResourceMap.Resource resource : resources.getJavascriptResources() ) {
      if ( resource.getResourceType().equals( ResourceMap.ResourceType.FILE ) ) {
        if ( StringUtils.isEmpty( className = getModuleClassName( resource.getResourceName() ) ) ) {
          continue;
        }
        classNames.add( className );
      }
    }

    // CSS AMD modules are not included as input parameters in require function's callback function parameter

    return classNames;
  }

  /**
   * Filters an AMD module class name, removing any prepended RequireJS loader plugin references from it.
   *
   * @param className the unfiltered module class name
   * @return the string containing the filtered module class name
   */
  protected String getModuleClassName( String className ) {
    if ( StringUtils.isEmpty( className ) ) {
      return "";
    }
    // remove prepended requireJS loader plugins and resource namespace from class name
    for ( RequireJSPlugin plugin : RequireJSPlugin.values() ) {
      className = className.replace( plugin.toString(), "" ).replace( RESOURCE_AMD_NAMESPACE + "/", "" );
    }

    return className;
  }
}
