/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Matcher;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.model.core.*;
import pt.webdetails.cdf.dd.model.core.writer.*;
import pt.webdetails.cdf.dd.model.core.writer.js.*;
import pt.webdetails.cdf.dd.model.inst.*;

import pt.webdetails.cdf.dd.render.DependenciesManager;
import pt.webdetails.cdf.dd.render.DependenciesManager.StdPackages;
import pt.webdetails.cdf.dd.render.RenderLayout;
import pt.webdetails.cdf.dd.render.Renderer;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.packager.StringFilter;

/**
 * @author dcleao
 */
public abstract class CdfRunJsDashboardWriter extends JsWriterAbstract implements IThingWriter {
  private static final Log logger = LogFactory.getLog( CdfRunJsDashboardWriter.class );

  private static final String EPILOGUE = wrapJsScriptTags( "Dashboards.init();" );

  // ------------

  protected static String readStyleTemplateOrDefault( String styleName ) throws IOException {
    if ( StringUtils.isNotEmpty( styleName ) ) {
      try {
        return readStyleTemplate( styleName );
      } catch ( IOException ex ) {
      }
    }

    // Couldn't open template file, attempt to use default
    return readStyleTemplate( CdeConstants.DEFAULT_STYLE );
  }

  protected static String readStyleTemplate( String styleName ) throws IOException {
    return readTemplateFile( CdeEnvironment.getPluginResourceLocationManager().getStyleResourceLocation( styleName ) );
  }

  protected static String readTemplateFile( String templateFile ) throws IOException {
    try {
      if ( CdeEnvironment.getPluginRepositoryReader().fileExists( templateFile ) ) {
        // template is in solution repository
        return Util.toString( CdeEnvironment.getPluginRepositoryReader().getFileInputStream( templateFile ) );

      } else if ( CdeEnvironment.getPluginSystemReader().fileExists( templateFile ) ) {
        // template is in system
        return Util.toString( CdeEnvironment.getPluginSystemReader().getFileInputStream( templateFile ) );
      } else if ( Utils.getAppropriateReadAccess( templateFile ).fileExists( templateFile ) ) {
        return Util.toString( Utils.getAppropriateReadAccess( templateFile ).getFileInputStream( templateFile ) );
      } else {
        // last chance : template is in user-defined folder
        return Util.toString( CdeEnvironment.getUserContentAccess().getFileInputStream( templateFile ) );
      }
    } catch ( IOException ex ) {
      logger.error( MessageFormat.format( "Couldn't open template file '{0}'.", templateFile ), ex );
      throw ex;
    }
  }

  public void write( Object output, IThingWriteContext context, Thing t ) throws ThingWriteException {
    this.write( (CdfRunJsDashboardWriteResult.Builder) output,
      (CdfRunJsDashboardWriteContext) context,
      (Dashboard) t );
  }

  // -----------------

  public abstract String getType();

  public void write(
    CdfRunJsDashboardWriteResult.Builder builder,
    CdfRunJsDashboardWriteContext ctx,
    Dashboard dash )
    throws ThingWriteException {
    assert dash == ctx.getDashboard();

    DashboardWcdfDescriptor wcdf = dash.getWcdf();

    // ------------

    String template;
    try {
      template = this.readTemplate( wcdf );
    } catch ( IOException ex ) {
      throw new ThingWriteException( "Could not read style template file.", ex );
    }

    template = ctx.replaceTokens( template );

    // ------------

    String footer;
    try {
      footer =
        Util.toString( CdeEnvironment.getPluginSystemReader().getFileInputStream( CdeConstants.RESOURCE_FOOTER ) );
    } catch ( IOException ex ) {
      throw new ThingWriteException( "Could not read footer file.", ex );
    }

    // ------------

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

  // -----------------

  protected String readTemplate( DashboardWcdfDescriptor wcdf ) throws IOException {
    return readStyleTemplateOrDefault( wcdf.getStyle() );
  }

  // -----------------

  protected String writeLayout( CdfRunJsDashboardWriteContext context, Dashboard dash ) {
    // TODO: HACK: uses pass-through XPath node...
    if ( dash.getLayoutCount() == 1 ) {
      JXPathContext docXP = dash.getLayout( "TODO" ).getLayoutXPContext();
      try {
        return getLayoutRenderer( docXP, context )
          .render( context.getOptions().getAliasPrefix() );
      } catch ( Exception ex ) {
        logger.error( "Error rendering layout", ex );
      }
    }

    return "";
  }

  // -----------------

  protected Renderer getLayoutRenderer( JXPathContext docXP, CdfRunJsDashboardWriteContext context ) {
    return new RenderLayout( docXP, context );
  }

  protected String writeComponents( CdfRunJsDashboardWriteContext context, Dashboard dash ) throws ThingWriteException {
    DashboardWcdfDescriptor wcdf = dash.getWcdf();

    StringBuilder out = new StringBuilder();
    StringBuilder widgetsOut = new StringBuilder();

    // Output WCDF
    out.append( "wcdfSettings = " );
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
      out.append( "Dashboards.addComponents([" ); out.append( addCompIds ); out.append( "]);" );
      out.append( NEWLINE );
    }

    out.append( widgetsOut );

    return out.toString();
  }

  protected String writeHeaders(
    String contents,
    CdfRunJsDashboardWriteContext context ) {
    CdfRunJsDashboardWriteOptions options = context.getOptions();

    DashboardWcdfDescriptor wcdf = context.getDashboard().getWcdf();

    final String title = "<title>" + wcdf.getTitle() + "</title>";

    // Get CDF headers
    String cdfDeps;
    try {
      cdfDeps = CdeEngine.getEnv().getCdfIncludes( contents, this.getType(), options.isDebug(), options.isAbsolute(),
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
        return String.format(
          "\t\t<link href=\"%s%s\" rel=\"stylesheet\" type=\"text/css\" />\n",
          baseUrl, baseUrl.endsWith( "/" ) && input.startsWith( "/" ) ? input.replaceFirst( "/", "" ) : input );
      }

      public String filter( String input, String baseUrl ) {
        return String.format(
          "\t\t<link href=\"%s%s\" rel=\"stylesheet\" type=\"text/css\" />\n",
          baseUrl, baseUrl.endsWith( "/" ) && input.startsWith( "/" ) ? input.replaceFirst( "/", "" ) : input );
      }
    };

    StringFilter jsFilter = new StringFilter() {
      public String filter( String input ) {
        return String.format(
          "\t\t<script language=\"javascript\" type=\"text/javascript\" src=\"%s%s\"></script>\n",
          baseUrl, baseUrl.endsWith( "/" ) && input.startsWith( "/" ) ? input.replaceFirst( "/", "" ) : input );
      }

      public String filter( String input, String baseUrl ) {
        return String.format(
          "\t\t<script language=\"javascript\" type=\"text/javascript\" src=\"%s%s\"></script>\n",
          baseUrl, baseUrl.endsWith( "/" ) && input.startsWith( "/" ) ? input.replaceFirst( "/", "" ) : input );
      }
    };

    DependenciesManager depMgr = DependenciesManager.getInstance();
    boolean isPackaged = !options.isDebug();
    String scriptDeps = depMgr.getPackage( StdPackages.COMPONENT_DEF_SCRIPTS ).getDependencies( jsFilter, isPackaged );
    String styleDeps = depMgr.getPackage( StdPackages.COMPONENT_STYLES ).getDependencies( cssFilter, isPackaged );
    String rawDeps = depMgr.getPackage( StdPackages.COMPONENT_SNIPPETS ).getRawDependencies( false );

    return title + cdfDeps + rawDeps + scriptDeps + styleDeps;
  }

  private String writeContent( String layout, String components ) {
    StringBuilder out = new StringBuilder();

    out.append( layout );

    wrapJsScriptTags( out, components );

    out.append( EPILOGUE );

    return out.toString();
  }
}
