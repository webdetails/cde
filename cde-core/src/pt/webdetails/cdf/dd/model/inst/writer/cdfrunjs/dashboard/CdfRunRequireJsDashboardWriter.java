/*!
 * Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import org.apache.commons.jxpath.JXPathContext;
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
import pt.webdetails.cdf.dd.render.DependenciesManager;
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
import java.util.regex.Matcher;

public class CdfRunRequireJsDashboardWriter extends CdfRunJsDashboardWriter {

  private static final String WEBCONTEXT = "webcontext.js?context={0}&requireJsOnly={1}";
  private static final String DASHBOARD_DECLARATION = "var dashboard = new Dashboard();";
  private static final String EPILOGUE = "dashboard.init();" + NEWLINE + "return dashboard;";
  private static final String REQUIRE_START = "require([''{0}''],"+  NEWLINE + "function({1})";
  private static final String REQUIRE_STOP = "});";
  private static final String CDF_AMD_BASE_COMPONENT_PATH = "cdf/components/";
  private static final String CDE_AMD_BASE_COMPONENT_PATH = "cde/components/";
  private static final String CDE_AMD_REPO_COMPONENT_PATH = "cde/repo/components/";
  private static final String PLUGIN_COMPONENT_FOLDER = "/components/";
  private static final String REQUIRE_PATH_CONFIG = "requireCfg[''paths''][''{0}''] = ''{1}'';";
  private static final String REQUIRE_CONFIG = "require.config(requireCfg);";
  protected Map<String, String> requireResourcesList = new LinkedHashMap<String, String>();
  protected StringBuffer jsCodeSnippets = new StringBuffer();
  private Map<String, String> componentList = new LinkedHashMap<String, String>();

  public CdfRunRequireJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType type, boolean isWidget ) {
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
    StringBuilder widgetsOut = new StringBuilder();

    // Output WCDF
    addAssignment( out, "wcdfSettings", wcdf.toJSON().toString( 2 ) );
    out.append( NEWLINE );

    boolean isFirstComp = true;

    StringBuilder addCompIds = new StringBuilder();
    boolean isFirstAddComp = true;

    IThingWriterFactory factory = context.getFactory();

    Iterable<Component> comps = dash.getRegulars();
    StringBuilder componentPath = new StringBuilder();
    String componentClassName;
    IThingWriter writer;
    for ( Component comp : comps ) {
      if ( StringUtils.isNotEmpty( comp.getName() ) ) {

        componentClassName = Utils.getComponentClassName( comp.getMeta().getName() );

        // "soft reset" StringBuilder
        componentPath.setLength( 0 );

        if ( !componentList.containsKey( componentClassName ) ) {

          //Store component "class" name and AMD module path
          if ( comp instanceof PrimitiveComponent && comp.getMeta().getOrigin() instanceof StaticSystemOrigin ) {

            // Assume it's a CDF component
            componentPath
              .append( CDF_AMD_BASE_COMPONENT_PATH )
              .append( componentClassName );
            componentList.put( componentClassName, componentPath.toString() );

          } else if ( comp instanceof CustomComponent ) {

            if ( comp.getMeta().getOrigin() instanceof StaticSystemOrigin ) {

              // Assume it's a CDE component
              componentPath
                .append( CDE_AMD_BASE_COMPONENT_PATH )
                .append( componentClassName );
              componentList.put( componentClassName, componentPath.toString() );

            } else if ( comp.getMeta().getOrigin() instanceof PluginRepositoryOrigin ) {

              // Assume it's a CDE component uploaded to the repository
              componentPath
                .append( CDE_AMD_REPO_COMPONENT_PATH )
                .append( comp.getMeta().getImplementationPath().substring(
                  0, comp.getMeta().getImplementationPath().lastIndexOf( ".js" ) ) );
              componentList.put( componentClassName, componentPath.toString() );

            } else if ( comp.getMeta().getOrigin() instanceof OtherPluginStaticSystemOrigin ) {

              // Assume it's a component from another plugin (e.g. sparkl)
              componentPath
                .append( ( (OtherPluginStaticSystemOrigin) comp.getMeta().getOrigin() ).getPluginId() )
                .append( PLUGIN_COMPONENT_FOLDER )
                .append( componentClassName );
              componentList.put( componentClassName, componentPath.toString() );

            }
          } else if ( comp instanceof WidgetComponent ) {
            // TODO: process WidgetComponent
            continue;
          }

        }

        try {
          writer = factory.getWriter( comp );
        } catch ( UnsupportedThingException ex ) {
          throw new ThingWriteException( ex );
        }

        boolean isWidget = comp instanceof WidgetComponent;

        StringBuilder out2 = isWidget ? widgetsOut : out;
        if ( !isFirstComp ) {
          out2.append( NEWLINE );
        }

        // NOTE: Widgets don't really exist at runtime,
        // only their (leaf-)content does.
        if ( comp instanceof VisualComponent && !( comp instanceof WidgetComponent ) ) {
          if ( isFirstAddComp ) {
            isFirstAddComp = false;
          } else {
            addCompIds.append( ", " );
          }

          addCompIds.append( context.getId( comp ) );
        }

        writer.write( out2, context, comp );

        isFirstComp = false;
      }
    }

    if ( !isFirstAddComp ) {
      out.append( NEWLINE )
        .append( "dashboard.addComponents([" )
        .append( addCompIds )
        .append( "]);" ).append( NEWLINE );
    }

    out.append( widgetsOut );

    return out.toString();
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

    DependenciesManager depMgr = DependenciesManager.getInstance();

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
    // Add main Dashboard module path
    cdfRequirePaths.add( getDashboardRequireModule() );

    // Add component AMD modules
    Iterator it = getComponentList().entrySet().iterator();
    Map.Entry pair;
    while ( it.hasNext() ) {
      pair = (Map.Entry) it.next();
      // Add component AMD module path
      cdfRequirePaths.add( (String) pair.getValue() );
      // Add component AMD module class name
      componentClassNames.add( (String) pair.getKey() );
      // Avoid exceptions
      it.remove();
    }



    componentClassNames.addAll( getRequireResourcesList().keySet() );
    cdfRequirePaths.addAll( getRequireResourcesList().keySet() );

    out.append( getFileResourcesRequirePaths() );

    out
      // Output module paths and module class names
      .append( MessageFormat.format( REQUIRE_START, StringUtils.join( cdfRequirePaths, "', '" ),
        StringUtils.join( componentClassNames, ", " ) ) ).append( " {" ).append( NEWLINE )
      .append( DASHBOARD_DECLARATION ).append( NEWLINE )
      .append( getJsCodeSnippets().toString() )
      .append( content ).append( NEWLINE )
      .append( EPILOGUE ).append( NEWLINE )
      .append( REQUIRE_STOP );

    return out.toString();
  }
  
  protected String getDashboardRequireModule() {
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
    StringBuffer out = new StringBuffer(  );
    
    for ( Map.Entry<String, String> resource : getRequireResourcesList().entrySet() ) {
      out.append( MessageFormat.format( REQUIRE_PATH_CONFIG, resource.getKey(), resource.getValue() ) )
        .append( NEWLINE );
    }

    out.append( REQUIRE_CONFIG ).append( NEWLINE );
    
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
}
