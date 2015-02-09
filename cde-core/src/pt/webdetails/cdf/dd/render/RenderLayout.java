/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.render;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;
import pt.webdetails.cdf.dd.render.layout.Render;
import pt.webdetails.cdf.dd.util.XPathUtils;

public class RenderLayout extends Renderer {
  private static final String RESOURCES = "and type!='LayoutResourceFile' and type!='LayoutResourceCode'";

  boolean includeResources;

  public RenderLayout( JXPathContext doc, CdfRunJsDashboardWriteContext context ) {
    super( doc, context );
    this.includeResources = true;
  }

  public RenderLayout( JXPathContext doc, CdfRunJsDashboardWriteContext context, boolean includeResources ) {
    super( doc, context );
    this.includeResources = includeResources;
  }

  public String render( String alias ) throws Exception {
    try {
      String filter = MessageFormat.format( XPATH_FILTER, UNIQUEID, !includeResources ? RESOURCES : "" );
      final Iterator layoutRows = doc.iteratePointers( filter );

      StringBuffer layout = new StringBuffer( );

      layout.append( NEWLINE ).append( getIndent( 2 ) ).append( "<div class='container'>" );
      renderRows( doc, layoutRows, getWidgets( alias ), alias, layout, 4 );
      layout.append( NEWLINE ).append( getIndent( 2 ) ).append( "</div>" );

      return layout.toString();
    } catch ( RenderException ex ) {
      return ex.getMessage();
    }
  }

  private void renderRows( final JXPathContext doc, final Iterator nodeIterator,
                           final Map<String, CdfRunJsDashboardWriteResult> widgetsByContainerId, final String alias,
                           final StringBuffer layout, final int indent ) throws Exception {
    while ( nodeIterator.hasNext() ) {
      final Pointer pointer = (Pointer) nodeIterator.next();
      final JXPathContext context = doc.getRelativeContext( pointer );

      final String rowId = (String) context.getValue( "id" );
      final String rowName = XPathUtils.getStringValue( context, "properties[name='name']/value" );

      final Render renderer = (Render) getRender( context );
      renderer.processProperties();
      renderer.aliasId( alias );
      layout.append( NEWLINE ).append( getIndent( indent ) );
      layout.append( renderer.renderStart() );

      if ( widgetsByContainerId.containsKey( rowName ) ) {
        CdfRunJsDashboardWriteResult widgetResult = widgetsByContainerId.get( rowName );
        layout.append( widgetResult.getLayout() );
      } else {
        renderRows( context, context.iteratePointers( MessageFormat.format( XPATH_FILTER, rowId, "" ) ),
          widgetsByContainerId, alias, layout, indent + 2 );
      }

      layout.append( NEWLINE ).append( getIndent( indent ) );
      layout.append( renderer.renderClose() );
    }
  }

  @Override
  protected String getRenderClassName( final String type ) {
    return "pt.webdetails.cdf.dd.render.layout." + type.replace( "Layout", "" ) + "Render";
  }
}
