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
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.packager.origin.PluginRepositoryOrigin;
import pt.webdetails.cpf.packager.origin.StaticSystemOrigin;
import pt.webdetails.cpf.packager.origin.OtherPluginStaticSystemOrigin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;

public abstract class CdfRunRequireJsDashboardWriter extends CdfRunJsDashboardWriter {

  private static final String EPILOGUE = "dashboard.init();" + NEWLINE + "return dashboard;";
  private static final String REQUIRE_START = "require(";
  private static final String REQUIRE_STOP = "});";

  private Map<String, String> componentList = new HashMap<String, String>();

  /**
   *
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
        Util.toString( CdeEnvironment.getPluginSystemReader().getFileInputStream( CdeConstants.RESOURCE_FOOTER_REQUIRE ) );
    } catch ( IOException ex ) {
      throw new ThingWriteException( "Could not read footer file.", ex );
    }

    String layout = ctx.replaceTokensAndAlias( this.writeLayout( ctx, dash ) );
    String components = ctx.replaceTokensAndAlias( this.writeComponents( ctx, dash ) );
    String content = writeContent( layout, components );
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
      .setLayout( layout )
      .setComponents( components )
      .setContent( content )
      .setFooter( footer )
      .setLoadedDate( ctx.getDashboard().getSourceDate() );
  }

  /**
   *
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
    out.append( "var wcdfSettings = " );
    out.append( wcdf.toJSON().toString( 2 ) );
    out.append( ';' );
    out.append( NEWLINE );
    out.append( NEWLINE );

    boolean isFirstComp = true;

    StringBuilder addCompIds = new StringBuilder();
    boolean isFirstAddComp = true;

    IThingWriterFactory factory = context.getFactory();

    Iterable<Component> comps = dash.getRegulars();
    StringBuilder componentPath = new StringBuilder();
    String componentClassName;
    for ( Component comp : comps ) {
      if ( StringUtils.isNotEmpty( comp.getName() ) ) {
        IThingWriter writer;
        componentClassName = getComponentClassName( comp.getMeta().getName() );

        if ( !componentList.containsKey( componentClassName ) ) {

          //Store component "class" name and AMD module path
          if ( comp instanceof PrimitiveComponent && comp.getMeta().getOrigin() instanceof StaticSystemOrigin ) {

            // Assume it's a CDF component
            componentPath.append( "cdf/components/" ).append( componentClassName );

          } else if ( comp instanceof CustomComponent ) {

            if ( comp.getMeta().getOrigin() instanceof StaticSystemOrigin ) {

              // Assume it's a CDE component
              componentPath.append( "cde/components/" ).append( componentClassName );

            } else if ( comp.getMeta().getOrigin() instanceof PluginRepositoryOrigin ) {

              // Assume it's a CDE component uploaded to the repository
              componentPath.append( "cde/solution/components/" ).append(
                  comp.getMeta().getImplementationPath().substring(
                      0, comp.getMeta().getImplementationPath().lastIndexOf( ".js" ) ) );

            } else if ( comp.getMeta().getOrigin() instanceof OtherPluginStaticSystemOrigin ) {
              
              // Assume it's a component from another plugin (e.g. sparkl)
              componentPath.append( ( ( OtherPluginStaticSystemOrigin ) comp.getMeta().getOrigin() ).getPluginId() )
                  .append( "/components/" ).append( componentClassName );

            }
          } else {
            // TODO: process other component types (e.g. WidgetComponent)
            componentPath.setLength( 0 );
            continue;
          }

          componentList.put( componentClassName, componentPath.toString() );

          // "soft reset" StringBuilder
          componentPath.setLength( 0 );

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
      out.append( NEWLINE );
      out.append( "dashboard.addComponents([" );
      out.append( addCompIds );
      out.append( "]);" );
      out.append( NEWLINE );
    }

    out.append( widgetsOut );

    return out.toString();
  }

  /**
   *
   * @param contents
   * @param context
   * @return
   */
  protected String writeHeaders( String contents, CdfRunJsDashboardWriteContext context ) {
    CdfRunJsDashboardWriteOptions options = context.getOptions();

    DashboardWcdfDescriptor wcdf = context.getDashboard().getWcdf();

    final String title = "<title>" + wcdf.getTitle() + "</title>";

    final String webcontext = "<script language=\"javascript\" type=\"text/javascript\" src=\"webcontext" +
      ".js?context=cdf&requireJsOnly=true\"></script>";

    // Get CDE headers
    final String baseUrl = ( options.isAbsolute()
      ? ( !StringUtils.isEmpty( options.getAbsRoot() )
      ? options.getSchemedRoot() + "/"
      : CdeEngine.getInstance().getEnvironment().getUrlProvider().getWebappContextRoot() )
      : "" );

    DependenciesManager depMgr = DependenciesManager.getInstance();

    return title + webcontext;
  }

  /**
   *
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
   *
   */
  protected String wrapRequireDefinitions( String content ) {
    StringBuilder out = new StringBuilder();

    // AMD module paths
    ArrayList<String> cdfRequirePaths = new ArrayList<String>();

    // AMD module class names (except Dashboard module))
    ArrayList<String> componentClassNames = new ArrayList<String>();

    // Add main Dashboard module class name
    componentClassNames.add("Dashboard");
    // Add main Dashboard module path
    final String dashboardType = this.getType();
    if ( dashboardType.equals( CdfRunRequireJsBlueprintDashboardWriter.TYPE ) ) {
      cdfRequirePaths.add( "cdf/Dashboard.Blueprint" );
    } else if ( dashboardType.equals( CdfRunRequireJsBootstrapDashboardWriter.TYPE ) ) {
      cdfRequirePaths.add( "cdf/Dashboard.Bootstrap" );
    } else if ( dashboardType.equals( CdfRunRequireJsMobileDashboardWriter.TYPE ) ) {
      cdfRequirePaths.add( "cdf/Dashboard.Mobile" );
    } else {
      cdfRequirePaths.add( "cdf/Dashboard" );
    }

    // Add component AMD modules
    Iterator it = componentList.entrySet().iterator();
    Map.Entry pair;
    while (it.hasNext()) {
      pair = (Map.Entry)it.next();
      // Add component AMD module path
      cdfRequirePaths.add((String) pair.getValue());
      // Add component AMD module class name
      componentClassNames.add( (String) pair.getKey());
      // Avoid exceptions
      it.remove();
    }

    out.append( REQUIRE_START);
    // Output module paths
    out.append( "['" + StringUtils.join( cdfRequirePaths, "', '" ) + "']" + "," ).append( NEWLINE );
    // Output module class names
    out.append( "function(" + StringUtils.join( componentClassNames, ", " ) + ") {" ).append( NEWLINE );
    out.append( "var dashboard = new Dashboard();" ).append( NEWLINE );
    out.append( content );
    out.append( NEWLINE );
    out.append( EPILOGUE );
    out.append( NEWLINE );
    out.append( REQUIRE_STOP );

    return out.toString();
  }

  /**
   * Gets the name of the class of the component based on it's type.
   *
   * @param componentType
   * @return
   */
  private String getComponentClassName( String componentType ) {

    if ( !StringUtils.isNotEmpty( componentType ) ) {
      return componentType;
    }

    StringBuilder sb = new StringBuilder();

    // starts with upper case character
    if ( !Character.isUpperCase( componentType.charAt(0) ) ) {
      sb.append( Character.toUpperCase( componentType.charAt( 0 ) ) );
      sb.append( componentType.substring( 1 ) );
    } else {
      sb.append( componentType );
    }

    // ends with "Component"
    if ( !componentType.endsWith( "Component" ) ) {
      sb.append( "Component" );
    }

    return sb.toString();
  }

}
