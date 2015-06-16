/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.PrimitiveComponent;
import pt.webdetails.cdf.dd.model.inst.CustomComponent;
import pt.webdetails.cdf.dd.model.inst.VisualComponent;
import pt.webdetails.cdf.dd.model.inst.WidgetComponent;
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
import pt.webdetails.cpf.packager.origin.PluginRepositoryOrigin;
import pt.webdetails.cpf.packager.origin.StaticSystemOrigin;
import pt.webdetails.cpf.packager.origin.OtherPluginStaticSystemOrigin;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pt.webdetails.cdf.dd.CdeConstants.Writer.*;

public class CdfRunJsDashboardWriter
    extends pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy.CdfRunJsDashboardWriter {

  private static final String WEBCONTEXT = "webcontext.js?context={0}&amp;requireJsOnly={1}";
  protected Map<String, String> requireJsResourcesList = new LinkedHashMap<String, String>();
  protected Map<String, String> requireCssResourcesList = new LinkedHashMap<String, String>();
  protected StringBuffer jsCodeSnippets = new StringBuffer();
  private Map<String, String> componentList = new LinkedHashMap<String, String>();
  private static final Pattern schemePattern = Pattern.compile( "^(ht|f)tps?\\:\\/\\/" );

  public CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType type, boolean isWidget ) {
    super( type, isWidget );
  }

  /**
   * @param builder
   * @param ctx
   * @param dash
   * @throws ThingWriteException
   */
  @Override
  public void write( CdfRunJsDashboardWriteResult.Builder builder, CdfRunJsDashboardWriteContext ctx, Dashboard dash )
    throws ThingWriteException {
    assert dash == ctx.getDashboard();

    if ( ctx.getOptions().isAmdModule() ) {
      writeModule( builder, ctx, dash );
      return;
    }

    DashboardWcdfDescriptor wcdf = dash.getWcdf();

    String template;
    try {
      template = this.readTemplate( wcdf );
    } catch ( IOException ex ) {
      throw new ThingWriteException( "Could not read style template file.", ex );
    }

    template = ctx.replaceTokens( template );

    String footer;
    try {
      footer =
        Util.toString(
          CdeEnvironment.getPluginSystemReader().getFileInputStream( CdeConstants.RESOURCE_FOOTER_REQUIRE ) );
    } catch ( IOException ex ) {
      throw new ThingWriteException( "Could not read footer file.", ex );
    }

    final String cssResources = ctx.replaceTokensAndAlias( this.writeResources( ctx, dash ) );
    // Prepend the CSS (<style> and <link>) code to the HTML layout
    final String layout = cssResources + ctx.replaceTokensAndAlias( this.writeLayout( ctx, dash ) );
    final String components = ctx.replaceTokensAndAlias( this.writeComponents( ctx, dash ) );
    final String content = writeContent( layout, components, ctx.getOptions().getContextConfiguration() );
    final String header = ctx.replaceTokens( writeHeaders( content, ctx ) );

    // Leave the DASHBOARD_HEADER_TAG to replace additional stuff on render.
    template = template
      .replaceAll( CdeConstants.DASHBOARD_HEADER_TAG,
        Matcher.quoteReplacement( header ) + CdeConstants.DASHBOARD_HEADER_TAG )
      .replaceAll( CdeConstants.DASHBOARD_FOOTER_TAG, Matcher.quoteReplacement( footer ) )
      .replaceAll( CdeConstants.DASHBOARD_CONTENT_TAG, Matcher.quoteReplacement( content ) );

    // Export
    builder
      .setTemplate( template )
      .setHeader( header )
      .setLayout( layout )
      .setComponents( components )
      .setContent( content )
      .setFooter( footer )
      .setLoadedDate( ctx.getDashboard().getSourceDate() );
  }

  /**
   * @param context
   * @param dash
   * @returns A String containing the <style> and <link> CSS resources
   */
  protected String writeResources( CdfRunJsDashboardWriteContext context, Dashboard dash ) {
    if ( dash.getLayoutCount() == 1 ) {
      JXPathContext docXP = dash.getLayout( "TODO" ).getLayoutXPContext();
      try {
        ResourceMap resources =
            getResourceRenderer( docXP, context ).renderResources( context.getOptions().getAliasPrefix() );

        List<ResourceMap.Resource> cssResources = resources.getCssResources();
        List<ResourceMap.Resource> javascriptResources = resources.getJavascriptResources();

        StringBuffer buffer = new StringBuffer();
        for ( ResourceMap.Resource jsResource : javascriptResources ) {
          if ( jsResource.getResourceType().equals( ResourceMap.ResourceType.FILE ) ) {
            addRequireJsResource( CdeConstants.RESOURCE_AMD_NAMESPACE + "/" + jsResource.getResourceName(),
                context.replaceTokensAndAlias( jsResource.getResourcePath().startsWith( "/" )
                ? jsResource.getResourcePath().replaceFirst( "/", "" ) : jsResource.getResourcePath() ) );
          } else {
            addJsCodeSnippet( jsResource.getProcessedResource() + NEWLINE );
          }
        }
        for ( ResourceMap.Resource cssResource : cssResources ) {
          if ( cssResource.getResourceType().equals( ResourceMap.ResourceType.FILE ) ) {
            // Use the css! requireJS loader plugin for CSS external resource loading
            addRequireCssResource( CdeConstants.REQUIREJS_PLUGIN.CSS
                + CdeConstants.RESOURCE_AMD_NAMESPACE + "/" + cssResource.getResourceName(),
                context.replaceTokensAndAlias( cssResource.getResourcePath().startsWith( "/" )
                ? cssResource.getResourcePath().replaceFirst( "/", "" ) : cssResource.getResourcePath() ) );
          } else {
            buffer.append( cssResource.getProcessedResource() ).append( NEWLINE );
          }
        }

        return buffer.toString();

      } catch ( Exception ex ) {
        logger.error( "Error rendering resources", ex );
      }
    }

    return "";
  }

  protected RenderResources getResourceRenderer( JXPathContext docXP, CdfRunJsDashboardWriteContext context ) {
    return new RenderResources( docXP, context );
  }

  protected Renderer getLayoutRenderer( JXPathContext docXP, CdfRunJsDashboardWriteContext context ) {
    return new RenderLayout( docXP, context, false );
  }

  /**
   * @param context
   * @param dash
   * @return
   * @throws ThingWriteException
   */
  protected String writeComponents( CdfRunJsDashboardWriteContext context, Dashboard dash ) throws ThingWriteException {
    DashboardWcdfDescriptor wcdf = dash.getWcdf();
    componentList.clear();

    StringBuilder out = new StringBuilder();

    // Output WCDF
    addAssignment( out, "var wcdfSettings", wcdf.toJSON().toString( 2 ) );
    out.append( NEWLINE );

    StringBuilder addCompIds = new StringBuilder();
    IThingWriterFactory factory = context.getFactory();

    Iterable<Component> comps = dash.getRegulars();
    for ( Component comp : comps ) {
      if ( StringUtils.isNotEmpty( getComponentName( comp ) ) ) {
        IThingWriter writer;
        try {
          writer = factory.getWriter( comp );
        } catch ( UnsupportedThingException ex ) {
          throw new ThingWriteException( ex );
        }

        // custom primitive widget (generic components) & layout component
        if ( isVisualComponent( comp ) ) {
          if ( isCustomComponent( comp ) || isPrimitiveComponent( comp ) ) {
            String componentClassName = getComponentClassName( comp );

            if ( !componentList.containsKey( componentClassName ) ) {
              String componentPath = getComponentPath( comp, componentClassName );
              if ( StringUtils.isEmpty( componentPath ) ) {
                continue;
              }
              componentList.put( componentClassName, componentPath );
            }
          }

          if ( addCompIds.length() > 0 ) {
            addCompIds.append( ", " );
          }
          addCompIds.append( comp.getId() );
        }

        writer.write( out, context, comp );

      }
    }

    if ( componentList.size() > 0 ) {
      out.append( NEWLINE )
        .append( "dashboard.addComponents([" )
        .append( addCompIds )
        .append( "]);" ).append( NEWLINE );
    }

    return out.toString();
  }

  protected String getComponentName( Component comp ) {
    return comp.getName();
  }

  protected String getComponentPath( Component comp, String componentClassName ) {
    StringBuilder componentPath = new StringBuilder();

    if ( isPrimitiveComponent( comp ) && isComponentStaticSystemOrigin( comp ) ) {

      // CDF component with a static system origin
      componentPath
        .append( CDF_AMD_BASE_COMPONENT_PATH )
        .append( componentClassName );

    } else if ( isCustomComponent( comp ) ) {

      if ( isComponentStaticSystemOrigin( comp ) ) {

        // CDE custom component with a static system origin
        componentPath
          .append( CDE_AMD_BASE_COMPONENT_PATH )
          .append( componentClassName );

      } else if ( isComponentPluginRepositoryOrigin( comp ) ) {

        String compImplPath = getComponentImplementationPath( comp );

        // if both versions are supported or no implementation path is provided
        // build AMD module using source path and component class name
        if ( supportsLegacy( comp ) || StringUtils.isEmpty( compImplPath ) ) {

          // assume that the AMD implementation file is in the same folder as component.xml
          // and that it has the same name as the component's class
          compImplPath = getComponentSourcePath( comp ).split( CdeConstants.CUSTOM_COMPONENT_CONFIG_FILENAME )[ 0 ]
            + componentClassName;

        } else {

          // if it only supports AMD and an implementation path is provided, use it
          compImplPath = compImplPath.substring( 0, compImplPath.lastIndexOf( ".js" ) );
        }

        // validate component's AMD module implementation path
        if ( StringUtils.isEmpty( compImplPath ) ) {
          logger.error( "Missing an implementation code source path for component " + componentClassName );
          return "";
        }

        // CDE custom component uploaded to the repository
        componentPath
          .append( CDE_AMD_REPO_COMPONENT_PATH )
          .append( compImplPath );

      } else if ( isComponentOtherPluginStaticSystemOrigin( comp ) ) {

        // custom component from another plugin (e.g. sparkl)
        componentPath
          .append( getPluginIdFromOrigin( comp ) )
          .append( PLUGIN_COMPONENT_FOLDER )
          .append( componentClassName );
      }
    } else if ( comp instanceof WidgetComponent ) {
      // TODO: process WidgetComponent
      return "";
    }

    return componentPath.toString();
  }

  protected String getComponentClassName( Component comp ) {
    return Utils.getComponentClassName( comp.getMeta().getName() );
  }

  protected boolean isPrimitiveComponent( Component comp ) {
    return comp instanceof PrimitiveComponent;
  }

  protected boolean isCustomComponent( Component comp ) {
    return comp instanceof CustomComponent;
  }

  protected boolean isVisualComponent( Component comp ) {
    return comp instanceof VisualComponent;
  }

  protected boolean isComponentStaticSystemOrigin( Component comp ) {
    return comp.getMeta().getOrigin() instanceof StaticSystemOrigin;
  }

  protected boolean isComponentPluginRepositoryOrigin( Component comp ) {
    return comp.getMeta().getOrigin() instanceof PluginRepositoryOrigin;
  }

  protected boolean isComponentOtherPluginStaticSystemOrigin( Component comp ) {
    return comp.getMeta().getOrigin() instanceof OtherPluginStaticSystemOrigin;
  }

  protected boolean supportsLegacy( Component comp ) {
    return comp.getMeta().supportsLegacy();
  }

  protected String getComponentImplementationPath( Component comp ) {
    return comp.getMeta().getImplementationPath();
  }

  protected String getComponentSourcePath( Component comp ) {
    return comp.getMeta().getSourcePath();
  }

  protected String getPluginIdFromOrigin( Component comp ) {
    return ( (OtherPluginStaticSystemOrigin) comp.getMeta().getOrigin() ).getPluginId();
  }

  /**
   * @param contents
   * @param context
   * @return
   */
  protected String writeHeaders( String contents, CdfRunJsDashboardWriteContext context ) {

    return MessageFormat.format( TITLE, context.getDashboard().getWcdf().getTitle() )
      + NEWLINE + MessageFormat.format( SCRIPT, writeWebcontext( "cdf", true ) );
  }

  protected String writeWebcontext( String context, boolean requireJsOnly ) {
    return MessageFormat.format( WEBCONTEXT, context, requireJsOnly ? "true" : "false" );
  }

  /**
   * @param layout
   * @param components
   * @return
   */
  private String writeContent( String layout, String components, String config ) {
    StringBuilder out = new StringBuilder();

    out.append( layout );

    //do the encapsulation stuff here

    wrapJsScriptTags( out, wrapRequireDefinitions( components, config ) );

    return out.toString();
  }

  /**
   * Wraps the JavaScript code, contained in the input parameter, with requirejs configurations.
   *
   * @param content Some JavaScript code to be wrapped.
   * @return The JS code wrapped with requirejs configuration
   */
  protected String wrapRequireDefinitions( String content, String config ) {
    StringBuilder out = new StringBuilder();


    ArrayList<String> cdfRequirePaths = new ArrayList<String>(), // AMD module paths
        componentClassNames = new ArrayList<String>(); // AMD module class names (except Dashboard module))

    // Add main Dashboard module class name
    componentClassNames.add( "Dashboard" );
    componentClassNames.add( "Logger" );
    componentClassNames.add( "$" );
    componentClassNames.add( "_" );
    componentClassNames.add( "moment" );
    componentClassNames.add( "cdo" );
    componentClassNames.add( "Utils" );
    // Add main Dashboard module path
    cdfRequirePaths.add( getDashboardRequireModuleId() );
    cdfRequirePaths.add( "cdf/Logger" );
    cdfRequirePaths.add( "cdf/lib/jquery" );
    cdfRequirePaths.add( CdeConstants.REQUIREJS_PLUGIN.NONAMD + "cdf/lib/underscore" );
    cdfRequirePaths.add( "cdf/lib/moment" );
    cdfRequirePaths.add( "cdf/lib/CCC/cdo" );
    cdfRequirePaths.add( "cdf/dashboard/Utils" );

    // Add component AMD modules
    Iterator it = getComponentList().entrySet().iterator();
    Map.Entry pair;
    while ( it.hasNext() ) {
      pair = (Map.Entry) it.next();
      // Add component AMD module path
      cdfRequirePaths.add( (String) pair.getValue() );
      // Add component AMD module class name
      componentClassNames.add( (String) pair.getKey() );
    }

    componentClassNames.addAll( getRequireFilteredClassNames() );
    cdfRequirePaths.addAll( getRequireJsResourcesList().keySet() );
    cdfRequirePaths.addAll( getRequireCssResourcesList().keySet() );

    out.append( getFileResourcesRequirePaths() )
      // Output module paths and module class names
      .append( MessageFormat.format( REQUIRE_START, StringUtils.join( cdfRequirePaths, "', '" ),
        StringUtils.join( componentClassNames, ", " ) ) ).append( NEWLINE )
      .append( MessageFormat.format( DASHBOARD_DECLARATION, config ) ).append( NEWLINE )
      .append( getJsCodeSnippets().toString() )
      .append( content ).append( NEWLINE )
      .append( DASHBOARD_INIT )
      .append( REQUIRE_STOP );

    return out.toString();
  }

  protected String getDashboardRequireModuleId() {
    DashboardWcdfDescriptor.DashboardRendererType dashboardType = this.getType();
    if ( dashboardType.equals( DashboardWcdfDescriptor.DashboardRendererType.BLUEPRINT ) ) {
      return "cdf/Dashboard.Blueprint";
    } else if ( dashboardType.equals( DashboardWcdfDescriptor.DashboardRendererType.BOOTSTRAP ) ) {
      return "cdf/Dashboard.Bootstrap";
    } else if ( dashboardType.equals( DashboardWcdfDescriptor.DashboardRendererType.MOBILE ) ) {
      return "cdf/Dashboard.Mobile";
    } else {
      return "cdf/Dashboard.Clean";
    }
  }

  protected String getFileResourcesRequirePaths() {
    StringBuffer out = new StringBuffer();

    Set<Map.Entry<String, String>> requireResourcesList = getRequireJsResourcesList().entrySet();
    if ( requireResourcesList.size() > 0 ) {
      for ( Map.Entry<String, String> resource : requireResourcesList ) {
        out.append( MessageFormat.format(
          schemePattern.matcher( resource.getValue() ).find() ? REQUIRE_PATH_CONFIG_FULL_URI : REQUIRE_PATH_CONFIG,
          CdeConstants.RESOURCE_AMD_NAMESPACE + "/" + getRequireFilteredClassName( resource.getKey() ),
          resource.getValue() ) ).append( NEWLINE );
      }
    }

    requireResourcesList = getRequireCssResourcesList().entrySet();
    if ( requireResourcesList.size() > 0 ) {
      for ( Map.Entry<String, String> resource : requireResourcesList ) {
        out.append( MessageFormat.format(
          schemePattern.matcher( resource.getValue() ).find() ? REQUIRE_PATH_CONFIG_FULL_URI : REQUIRE_PATH_CONFIG,
          CdeConstants.RESOURCE_AMD_NAMESPACE + "/" + getRequireFilteredClassName( resource.getKey() ),
          resource.getValue() ) ).append( NEWLINE );
      }
    }

    if ( out.length() > 0 ) {
      out.append( REQUIRE_CONFIG ).append( NEWLINE );
    }

    return out.toString();
  }

  public Map<String, String> getComponentList() {
    return componentList;
  }

  public void setComponentList( Map<String, String> componentList ) {
    this.componentList = componentList;
  }

  public Map<String, String> getRequireJsResourcesList() {
    return requireJsResourcesList;
  }

  public void addRequireJsResource( String resourceName, String resourcePath ) {
    this.requireJsResourcesList.put( resourceName, resourcePath );
  }

  public Map<String, String> getRequireCssResourcesList() {
    return requireCssResourcesList;
  }

  public void addRequireCssResource( String resourceName, String resourcePath ) {
    this.requireCssResourcesList.put( resourceName, resourcePath );
  }

  public StringBuffer getJsCodeSnippets() {
    return jsCodeSnippets;
  }

  public void addJsCodeSnippet( String jsCodeSnippet ) {
    this.jsCodeSnippets.append( jsCodeSnippet );
  }

  public void writeModule(
      CdfRunJsDashboardWriteResult.Builder builder,
      CdfRunJsDashboardWriteContext ctx,
      Dashboard dash )
      throws ThingWriteException {

    final String cssResources = ctx.replaceTokensAndAlias( this.writeResources( ctx, dash ) );
    // Prepend the CSS (<style> and <link>) code to the HTML layout
    final String layout = cssResources + ctx.replaceTokens( this.writeLayout( ctx, dash ) );
    final String components = replaceAliasTagWithAlias(
        ctx.replaceHtmlAlias( ctx.replaceTokens( this.writeComponents( ctx, dash ) ) ) );

    boolean emptyAlias = ctx.getOptions().getAliasPrefix().contains( CdeConstants.DASHBOARD_ALIAS_TAG );
    final String content = this.wrapRequireModuleDefinitions( components, layout, emptyAlias,
      ctx.getOptions().getContextConfiguration() );

    // Export
    builder
      .setTemplate( "" )
      .setHeader( "" )
      .setLayout( layout )
      .setComponents( components )
      .setContent( content )
      .setFooter( "" )
      .setLoadedDate( ctx.getDashboard().getSourceDate() );
  }

  protected String replaceAliasTagWithAlias( String content ) {
    return content.replaceAll( CdeConstants.DASHBOARD_ALIAS_TAG, "\" + this._alias +\"" );
  }

  /**
   * Wraps the JavaScript code, contained in the input parameters, as a requirejs module definition.
   *
   * @param content Some JavaScript code to be wrapped.
   * @return A string containing an AMD module definition.
   */
  protected String wrapRequireModuleDefinitions( String content, String layout, boolean emptyAlias, String config ) {
    StringBuilder out = new StringBuilder();

    ArrayList<String> cdfRequirePaths = new ArrayList<String>(), // AMD module ids
        componentClassNames = new ArrayList<String>(); // AMD module class names

    // Add main Dashboard module class name
    componentClassNames.add( "Dashboard" );
    componentClassNames.add( "Logger" );
    componentClassNames.add( "$" );
    componentClassNames.add( "_" );
    componentClassNames.add( "moment" );
    componentClassNames.add( "cdo" );
    componentClassNames.add( "Utils" );
    // Add main Dashboard module id
    cdfRequirePaths.add( getDashboardRequireModuleId() );
    cdfRequirePaths.add( "cdf/Logger" );
    cdfRequirePaths.add( "cdf/lib/jquery" );
    cdfRequirePaths.add( CdeConstants.REQUIREJS_PLUGIN.NONAMD + "cdf/lib/underscore" );
    cdfRequirePaths.add( "cdf/lib/moment" );
    cdfRequirePaths.add( "cdf/lib/CCC/cdo" );
    cdfRequirePaths.add( "cdf/dashboard/Utils" );

    // Add component AMD modules
    Iterator it = getComponentList().entrySet().iterator();
    Map.Entry pair;
    while ( it.hasNext() ) {
      pair = (Map.Entry) it.next();
      // Add component AMD module id/path
      cdfRequirePaths.add( (String) pair.getValue() );
      // Add component AMD module class name
      componentClassNames.add( (String) pair.getKey() );
    }

    componentClassNames.addAll( getRequireFilteredClassNames() );
    cdfRequirePaths.addAll( getRequireJsResourcesList().keySet() );
    cdfRequirePaths.addAll( getRequireCssResourcesList().keySet() );

    out.append( getFileResourcesRequirePaths() )
      // Output module paths and module class names
        .append( MessageFormat.format( DEFINE_START,
        StringUtils.join( cdfRequirePaths, "', '" ),
        StringUtils.join( componentClassNames, ", " ) ) );

    if ( emptyAlias ) {
      out.append( MessageFormat.format( DASHBOARD_MODULE_START_EMPTY_ALIAS, config,
          StringEscapeUtils.escapeJavaScript( layout.replace( NEWLINE, "" ) ) ) );
    } else {
      out.append( MessageFormat.format(DASHBOARD_MODULE_START, config) )
          .append( MessageFormat.format( DASHBOARD_MODULE_LAYOUT,
          StringEscapeUtils.escapeJavaScript( layout.replace( NEWLINE, "" ) ) ) );
    }

    final String jsCodeSnippets = getJsCodeSnippets().toString();

    out.append( DASHBOARD_MODULE_RENDERER ).append( NEWLINE )
      .append( DASHBOARD_MODULE_SETUP_DOM ).append( NEWLINE )
      .append( MessageFormat.format( DASHBOARD_MODULE_PROCESS_COMPONENTS,
        jsCodeSnippets.length() > 0 ? jsCodeSnippets + NEWLINE + content : content ) )
      .append( DASHBOARD_MODULE_STOP ).append( NEWLINE )
      .append( DEFINE_STOP );

    return out.toString();
  }

  /**
   * Filters AMD module class names, removing any prepended requireJS loader plugin references from them and excluding
   * CSS resources.
   *
   * @return ArrayList containing the filtered AMD module ids/class names.
   */
  protected ArrayList<String> getRequireFilteredClassNames() {
    ArrayList<String> classNames = new ArrayList<String>( getRequireJsResourcesList().size() );
    // Filter and remove known RequireJS Loader plugin from class names
    for ( String className : getRequireJsResourcesList().keySet() ) {

      classNames.add( getRequireFilteredClassName( className ) );
    }

    return classNames;
  }

  /**
   * Filters a AMD module class name, removing any prepended requireJS loader plugin references from it.
   *
   * @param className The unfiltered module id/class name.
   * @return String containing the filtered AMD module id/class name.
   */
  protected String getRequireFilteredClassName( String className ) {
    // remove prepended requireJS loader plugins from class name
    for ( CdeConstants.REQUIREJS_PLUGIN plugin : CdeConstants.REQUIREJS_PLUGIN.values() ) {
      className = className.replace( plugin.toString(), "" ).replace(
        CdeConstants.RESOURCE_AMD_NAMESPACE + "/", "" );
    }

    return className;
  }
}
