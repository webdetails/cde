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
import pt.webdetails.cdf.dd.model.inst.VisualComponent;
import pt.webdetails.cdf.dd.model.inst.WidgetComponent;
import pt.webdetails.cdf.dd.render.DependenciesManager;
import pt.webdetails.cdf.dd.render.DependenciesManager.StdPackages;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.packager.StringFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public abstract class CdfRunRequireJsDashboardWriter extends CdfRunJsDashboardWriter {

  private static final String EPILOGUE = "dashboard.init();" + NEWLINE + "return dashboard;";
  private static final String REQUIRE_START = "require(";
  private static final String REQUIRE_STOP = "});";

  private List<String> componentList = new ArrayList<String>();

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
    for ( Component comp : comps ) {
      if ( StringUtils.isNotEmpty( comp.getName() ) ) {
        IThingWriter writer;
        if ( !componentList.contains( comp.getMeta().getName() ) && comp instanceof PrimitiveComponent ) {
          componentList.add( comp.getMeta().getName() );
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
   * Gets the class names of the components based on their type.
   * It converts the first character to upper case and appends the string 'Component'.
   *
   * @param components
   * @return
   */
  private List<String> getComponentClassNames() {
    List<String> newComponents = new ArrayList<String>();
    StringBuilder sb = new StringBuilder();

    for ( String componentType : this.componentList ) {

      if ( !StringUtils.isNotEmpty( componentType ) ) {
        continue;
      }

      // starts with upper case character
      if ( !Character.isUpperCase( componentType.charAt(0) ) ) {
        sb.append( Character.toUpperCase( componentType.charAt( 0 ) ) );
        sb.append( componentType.substring( 1 ) );
      } else {
        sb.append( componentType );
      }

      // ends with 'Component'
      if ( !componentType.endsWith( "Component" ) ) {
        sb.append( "Component" );
      }

      newComponents.add( sb.toString() );
      sb.setLength( 0 );
    }
    return newComponents;
  }

  /**
   *
   */
  protected String wrapRequireDefinitions( String content ) {
    StringBuilder out = new StringBuilder();;

    List<String> cdfRequirePaths = new ArrayList<String>();
    // Add module class names to be used as parameters of the require function (except Dashboard module))
    List<String> componentClassNames = getComponentClassNames();

    // Add Dashboard module path
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

    // Add remaining module paths
    for ( String componentClassName : componentClassNames ) {
      if ( componentClassName.startsWith( "Ccc" ) ) {
        cdfRequirePaths.add( "cdf/components/ccc/" + componentClassName );
      } else {
        cdfRequirePaths.add( "cdf/components/" + componentClassName );
      }
    }

    // Add Dashboard module class name as a parameter for the require function
    componentClassNames.add(0, "Dashboard");

    out.append( REQUIRE_START );
    // Output require module paths
    out.append( "['" + StringUtils.join( cdfRequirePaths, "', '" ) + "']" + "," );
    out.append( NEWLINE );
    // Output require function module class names
    out.append( "function(" + StringUtils.join( componentClassNames, ", " ) + ") {" );
    out.append( NEWLINE );
    out.append( "// CDE-374" );
    out.append( NEWLINE );
    out.append( "var dashboard = new Dashboard();" );
    out.append( NEWLINE );
    out.append( content );
    out.append( NEWLINE );
    out.append( EPILOGUE );
    out.append( NEWLINE );
    out.append( REQUIRE_STOP );

    return out.toString();
  }
}
