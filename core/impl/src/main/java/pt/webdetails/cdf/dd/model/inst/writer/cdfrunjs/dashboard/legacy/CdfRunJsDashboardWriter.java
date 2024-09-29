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


package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Matcher;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONException;

import org.owasp.encoder.Encode;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.CdeEngine;

import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.VisualComponent;
import pt.webdetails.cdf.dd.model.inst.WidgetComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;
import pt.webdetails.cdf.dd.render.DependenciesManager;
import pt.webdetails.cdf.dd.render.DependenciesManager.StdPackages;
import pt.webdetails.cdf.dd.render.RenderLayout;
import pt.webdetails.cdf.dd.render.Renderer;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.packager.StringFilter;

import static pt.webdetails.cdf.dd.CdeConstants.Writer.*;

public class CdfRunJsDashboardWriter extends JsWriterAbstract implements IThingWriter {
  protected static final Log logger = LogFactory.getLog( CdfRunJsDashboardWriter.class );

  private DashboardWcdfDescriptor.DashboardRendererType type;

  public CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType type ) {
    super();
    this.type = type;
  }

  public void write( Object output, IThingWriteContext context, Thing t ) throws ThingWriteException {
    this.write( (CdfRunJsDashboardWriteResult.Builder) output,
        (CdfRunJsDashboardWriteContext) context,
        (Dashboard) t );
  }

  public DashboardWcdfDescriptor.DashboardRendererType getType() {
    return this.type;
  }

  public void write( CdfRunJsDashboardWriteResult.Builder builder, CdfRunJsDashboardWriteContext ctx, Dashboard dash )
    throws ThingWriteException {
    assert dash == ctx.getDashboard();

    DashboardWcdfDescriptor wcdf = dash.getWcdf();

    String template;
    try {
      template = Utils.readTemplate( wcdf );
    } catch ( IOException ex ) {
      throw new ThingWriteException( "Could not read style template file.", ex );
    }

    template = ctx.replaceTokens( template );

    String footer;
    try {
      footer =
        Util.toString( CdeEnvironment.getPluginSystemReader().getFileInputStream( CdeConstants.RESOURCE_FOOTER ) );
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

  protected String writeLayout( CdfRunJsDashboardWriteContext context, Dashboard dash ) {
    if ( dash.getLayoutCount() == 1 ) {
      JXPathContext docXP = dash.getLayout( "TODO" ).getLayoutXPContext();
      try {
        return getLayoutRenderer( docXP, context ).render( context.getOptions().getAliasPrefix() );
      } catch ( Exception ex ) {
        logger.error( "Error rendering layout", ex );
      }
    }

    return "";
  }

  protected Renderer getLayoutRenderer( JXPathContext docXP, CdfRunJsDashboardWriteContext context ) {
    return new RenderLayout( docXP, context );
  }

  protected String writeComponents( CdfRunJsDashboardWriteContext context, Dashboard dash )
    throws ThingWriteException {
    DashboardWcdfDescriptor wcdf = dash.getWcdf();

    StringBuilder out = new StringBuilder();
    StringBuilder widgetsOut = new StringBuilder();

    // Output WCDF
    try {
      addAssignment( out, "wcdfSettings", wcdf.toJSON().toString( 2 ) );
    } catch ( JSONException ex ) {
      throw new ThingWriteException( "Converting wcdf to json", ex );
    }
    out.append( NEWLINE );

    boolean isFirstComp = true;

    StringBuilder addCompIds = new StringBuilder();
    boolean isFirstAddComp = true;

    IThingWriterFactory factory = context.getFactory();

    Iterable<Component> comps = dash.getRegulars();
    for ( Component comp : comps ) {
      if ( StringUtils.isNotEmpty( comp.getName() ) ) {
        IThingWriter writer;
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
      out.append( "Dashboards.addComponents([" );
      out.append( addCompIds );
      out.append( "]);" );
      out.append( NEWLINE );
    }

    out.append( widgetsOut );

    return out.toString();
  }

  protected String writeHeaders( String contents, CdfRunJsDashboardWriteContext context ) {
    CdfRunJsDashboardWriteOptions options = context.getOptions();

    DashboardWcdfDescriptor wcdf = context.getDashboard().getWcdf();

    final String title = MessageFormat.format( TITLE, wcdf.getTitle() );

    // Get CDF headers
    String cdfDeps;
    try {
      cdfDeps =
        CdeEngine.getEnv().getCdfIncludes( contents, this.getType().getType(), options.isDebug(), options.isAbsolute(),
          options.getAbsRoot(), options.getScheme() );
    } catch ( Exception ex ) {
      logger.error( "Failed to get cdf includes" );
      cdfDeps = "";
    }

    // Get CDE headers
    final String baseUrl = ( options.isAbsolute()
        ? ( !StringUtils.isEmpty( options.getAbsRoot() )
          ? options.getSchemedRoot() + "/"
          : CdeEngine.getInstance().getEnvironment().getUrlProvider().getWebappContextRoot() )
        : "" );

    StringFilter cssFilter = new StringFilter() {
      public String filter( String input ) {
        return filter( input, baseUrl );
      }

      public String filter( String input, String baseUrl ) {
        baseUrl = Encode.forHtmlAttribute( baseUrl );
        return MessageFormat.format( STYLE, joinUrls( baseUrl, input ) );
      }
    };

    StringFilter jsFilter = new StringFilter() {
      public String filter( String input ) {
        return filter( input, baseUrl );
      }

      public String filter( String input, String baseUrl ) {
        baseUrl = Encode.forHtmlAttribute( baseUrl );
        return MessageFormat.format( SCRIPT, joinUrls( baseUrl, input ) );
      }
    };

    DependenciesManager depMgr = DependenciesManager.getInstance();
    boolean isPackaged = !options.isDebug();
    String scriptDeps = depMgr.getPackage( StdPackages.COMPONENT_DEF_SCRIPTS ).getDependencies( jsFilter, isPackaged );
    String styleDeps = depMgr.getPackage( StdPackages.COMPONENT_STYLES ).getDependencies( cssFilter, isPackaged );
    String rawDeps = depMgr.getPackage( StdPackages.COMPONENT_SNIPPETS ).getRawDependencies( false );

    return title + cdfDeps + rawDeps + scriptDeps + styleDeps;
  }

  protected String joinUrls( String baseUrl, String url ) {
    return baseUrl + ( ( baseUrl.endsWith( "/" ) && url.startsWith( "/" ) ) ? url.replaceFirst( "/", "" ) : url );
  }

  private String writeContent( String layout, String components ) {
    StringBuilder out = new StringBuilder();

    out.append( layout );

    wrapJsScriptTags( out, components + NEWLINE + CdeConstants.Writer.DASHBOARDS_INIT );

    return out.toString();
  }
}
