/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.amd;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.CdeEngine;
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
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
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

public class CdfRunJsDashboardWriter
    extends pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy.CdfRunJsDashboardWriter {

  private static final String WEBCONTEXT = "webcontext.js?context={0}&requireJsOnly={1}";
  // make the dashboard variable available in the global scope to facilitate debugging
  private static final String DASHBOARD_DECLARATION = "window.dashboard = new Dashboard();";
  private static final String DASHBOARD_INIT = "dashboard.init();" + NEWLINE;
  private static final String REQUIRE_START = "require([''{0}'']," + NEWLINE + "function({1})";
  private static final String REQUIRE_STOP = "return dashboard;" + NEWLINE + "});";
  private static final String DEFINE_START = "define([''{0}'']," + NEWLINE + INDENT1 + "function({1}) '{'" + NEWLINE;
  private static final String DEFINE_STOP = "return CustomDashboard;" + NEWLINE + "});";
  private static final String DASHBOARD_MODULE_START = "var CustomDashboard = Dashboard.extend({" + NEWLINE
      + INDENT1 + "constructor: function() { this.base.apply(this, arguments); }," + NEWLINE;
  private static final String DASHBOARD_MODULE_LAYOUT = INDENT1 + "layout: ''{0}''," + NEWLINE;
  private static final String DASHBOARD_MODULE_SETUP_DOM = "setupDOM: function(targetId) {" + NEWLINE
      + INDENT2 + "if(!$('#' + targetId).length) { Logger.warn('Invalid html target element id'); return; };" + NEWLINE
      + INDENT2 + "$('#' + targetId).empty();" + NEWLINE
      + INDENT2 + "$('#' + targetId).html(this.layout);" + NEWLINE
      + " },";
  private static final String DASHBOARD_MODULE_RENDERER = "render: function(targetId) {" + NEWLINE
      + INDENT2 + "this.setupDOM(targetId);" + NEWLINE
      + INDENT2 + "this._processComponents();" + NEWLINE
      + INDENT2 + "this.init();" + NEWLINE
      + "},";
  private static final String DASHBOARD_MODULE_PROCESS_COMPONENTS =
      INDENT1 + "_processComponents: function() '{'" + NEWLINE
      + INDENT2 + "var dashboard = this;" + NEWLINE
      + INDENT2 + "{0}" + NEWLINE
      + INDENT1 + "'}'" + NEWLINE;
  private static final String DASHBOARD_MODULE_STOP = INDENT1 + "});";
  private static final String CDF_AMD_BASE_COMPONENT_PATH = "cdf/components/";
  private static final String CDE_AMD_BASE_COMPONENT_PATH = "cde/components/";
  private static final String CDE_AMD_REPO_COMPONENT_PATH = "cde/repo/components/";
  private static final String PLUGIN_COMPONENT_FOLDER = "/components/";
  private static final String REQUIRE_PATH_CONFIG = "requireCfg[''paths''][''{0}''] = ''{1}'';";
  private static final String REQUIRE_CONFIG = "require.config(requireCfg);";
  protected Map<String, String> requireResourcesList = new LinkedHashMap<String, String>();
  protected StringBuffer jsCodeSnippets = new StringBuffer();
  private Map<String, String> componentList = new LinkedHashMap<String, String>();

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

    String resources = ctx.replaceTokensAndAlias( this.writeResources( ctx, dash ) );
    String layout = ctx.replaceTokensAndAlias( this.writeLayout( ctx, dash ) );
    String components = ctx.replaceTokensAndAlias( this.writeComponents( ctx, dash ) );
    String content = writeContent( resources + layout, components );
    String header = ctx.replaceTokens( writeHeaders( content, ctx ) );

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
      .setLayout( resources + layout )
      .setComponents( components )
      .setContent( content )
      .setFooter( footer )
      .setLoadedDate( ctx.getDashboard().getSourceDate() );
  }

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
            addRequireResource( jsResource.getResourceName(),
                context.replaceTokensAndAlias( jsResource.getResourcePath() ) );
          } else {
            addJsCodeSnippet( jsResource.getProcessedResource() + NEWLINE );
          }
        }
        for ( ResourceMap.Resource cssResource : cssResources ) {
          buffer.append( cssResource.getProcessedResource() + NEWLINE );
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
          addCompIds.append( getComponentIdFromContext( context, comp ) );
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

  protected String getComponentIdFromContext( CdfRunJsDashboardWriteContext context, Component comp ) {
    return context.getId( comp );
  }

  protected String getComponentName( Component comp ) {
    return comp.getName();
  }

  protected String getComponentPath( Component comp, String componentClassName ) {
    StringBuilder componentPath = new StringBuilder();

    if ( isPrimitiveComponent( comp ) && isComponentStaticSystemOrigin( comp ) ) {
      // Assume it's a CDF component
      componentPath
        .append( CDF_AMD_BASE_COMPONENT_PATH )
        .append( componentClassName );

    } else if ( isCustomComponent( comp ) ) {

      if ( isComponentStaticSystemOrigin( comp ) ) {

        // Assume it's a CDE component
        componentPath
          .append( CDE_AMD_BASE_COMPONENT_PATH )
          .append( componentClassName );

      } else if ( isComponentPluginRepositoryOrigin( comp ) ) {

        String compImplPath = getComponentImplementationPath( comp );

        if ( StringUtils.isEmpty( compImplPath ) ) {
          logger.error( "Missing an implementation code source path for component "
              + componentClassName );
          return "";
        }

        // Assume it's a CDE component uploaded to the repository
        componentPath
          .append( CDE_AMD_REPO_COMPONENT_PATH )
          .append( compImplPath.substring(0, compImplPath.lastIndexOf( ".js" ) ) );

      } else if ( isComponentOtherPluginStaticSystemOrigin( comp ) ) {

        // Assume it's a component from another plugin (e.g. sparkl)
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

  protected String getComponentImplementationPath( Component comp ) {
    return comp.getMeta().getImplementationPath();
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
    CdfRunJsDashboardWriteOptions options = context.getOptions();

    DashboardWcdfDescriptor wcdf = context.getDashboard().getWcdf();

    final String title = MessageFormat.format( TITLE, wcdf.getTitle() );
    final String webcontext = MessageFormat.format( SCRIPT, writeWebcontext( "cdf", true ) );

    // Get CDE headers
    final String baseUrl = ( options.isAbsolute()
        ? ( !StringUtils.isEmpty( options.getAbsRoot() )
          ? options.getSchemedRoot() + "/"
          : CdeEngine.getInstance().getEnvironment().getUrlProvider().getWebappContextRoot() )
        : "" );

    return title + NEWLINE + webcontext;
  }

  protected String writeWebcontext( String context, boolean requireJsOnly ) {
    return MessageFormat.format( WEBCONTEXT, context, requireJsOnly ? "true" : "false" );
  }

  /**
   * @param layout
   * @param components
   * @return
   */
  private String writeContent( String layout, String components ) {
    StringBuilder out = new StringBuilder();

    out.append( layout );

    //do the encapsulation stuff here

    wrapJsScriptTags( out, wrapRequireDefinitions( components ) );

    return out.toString();
  }

  /**
   * Wraps the JavaScript code, contained in the input parameter, with requirejs configurations.
   *
   * @param content Some JavaScript code to be wrapped.
   * @return
   */
  protected String wrapRequireDefinitions( String content ) {
    StringBuilder out = new StringBuilder();


    ArrayList<String> cdfRequirePaths = new ArrayList<String>(), // AMD module paths
        componentClassNames = new ArrayList<String>(); // AMD module class names (except Dashboard module))

    // Add main Dashboard module class name
    componentClassNames.add( "Dashboard" );
    componentClassNames.add( "Logger" );
    componentClassNames.add( "$" );
    componentClassNames.add( "_" );
    componentClassNames.add( "moment" );
    // Add main Dashboard module path
    cdfRequirePaths.add( getDashboardRequireModuleId() );
    cdfRequirePaths.add( "cdf/Logger" );
    cdfRequirePaths.add( "cdf/lib/jquery" );
    cdfRequirePaths.add( "amd!cdf/lib/underscore" );
    cdfRequirePaths.add( "cdf/lib/moment" );

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

    componentClassNames.addAll( getRequireResourcesList().keySet() );
    cdfRequirePaths.addAll( getRequireResourcesList().keySet() );

    out.append( getFileResourcesRequirePaths() )
      // Output module paths and module class names
      .append( MessageFormat.format( REQUIRE_START, StringUtils.join( cdfRequirePaths, "', '" ),
        StringUtils.join( componentClassNames, ", " ) ) ).append( " {" ).append( NEWLINE )
      .append( DASHBOARD_DECLARATION ).append( NEWLINE )
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
      return "cdf/Dashboard";
    }
  }

  protected String getFileResourcesRequirePaths() {
    StringBuffer out = new StringBuffer();

    Set<Map.Entry<String, String>> requireResourcesList = getRequireResourcesList().entrySet();
    if ( requireResourcesList.size() > 0 ) {
      for ( Map.Entry<String, String> resource : requireResourcesList ) {
        out.append( MessageFormat.format( REQUIRE_PATH_CONFIG, resource.getKey(), resource.getValue() ) )
          .append( NEWLINE );
      }

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

  public Map<String, String> getRequireResourcesList() {
    return requireResourcesList;
  }

  public void addRequireResource( String resourceName, String resourcePath ) {
    this.requireResourcesList.put( resourceName, resourcePath );
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

    final String layout = ctx.replaceTokensAndAlias( this.writeLayout( ctx, dash ) );
    final String components = ctx.replaceTokensAndAlias( this.writeComponents( ctx, dash ) );
    final String content = this.wrapRequireModuleDefinitions( components, layout );

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

  /**
   * Wraps the JavaScript code, contained in the input parameters, as a requirejs module.
   *
   * @param content Some JavaScript code to be wrapped.
   * @return
   */
  protected String wrapRequireModuleDefinitions( String content, String layout ) {
    StringBuilder out = new StringBuilder();


    ArrayList<String> cdfRequirePaths = new ArrayList<String>(), // AMD module ids
        componentClassNames = new ArrayList<String>(); // AMD module class names

    // Add main Dashboard module class name
    componentClassNames.add( "Dashboard" );
    componentClassNames.add( "Logger" );
    componentClassNames.add( "$" );
    componentClassNames.add( "_" );
    componentClassNames.add( "moment" );
    // Add main Dashboard module id
    cdfRequirePaths.add( getDashboardRequireModuleId() );
    cdfRequirePaths.add( "cdf/Logger" );
    cdfRequirePaths.add( "cdf/lib/jquery" );
    cdfRequirePaths.add( "amd!cdf/lib/underscore" );
    cdfRequirePaths.add( "cdf/lib/moment" );

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

    componentClassNames.addAll( getRequireResourcesList().keySet() );
    cdfRequirePaths.addAll( getRequireResourcesList().keySet() );

    out.append( getFileResourcesRequirePaths() )
      // Output module paths and module class names
      .append( MessageFormat.format( DEFINE_START,
        StringUtils.join( cdfRequirePaths, "', '" ),
        StringUtils.join( componentClassNames, ", " ) ) )
      .append( getJsCodeSnippets().toString() )
      .append( DASHBOARD_MODULE_START )
      .append( MessageFormat.format( DASHBOARD_MODULE_LAYOUT,
        StringEscapeUtils.escapeJavaScript( layout.replace( NEWLINE, "" ) ) ) )
      .append( DASHBOARD_MODULE_RENDERER ).append( NEWLINE )
      .append( DASHBOARD_MODULE_SETUP_DOM ).append( NEWLINE )
      .append( MessageFormat.format( DASHBOARD_MODULE_PROCESS_COMPONENTS, content ) )
      .append( DASHBOARD_MODULE_STOP ).append( NEWLINE )
      .append( DEFINE_STOP );

    return out.toString();
  }
}
