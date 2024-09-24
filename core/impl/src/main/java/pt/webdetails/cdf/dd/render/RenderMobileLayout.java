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

package pt.webdetails.cdf.dd.render;

import java.text.MessageFormat;
import java.util.Iterator;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.render.layout.Render;


public class RenderMobileLayout extends Renderer {
  public RenderMobileLayout( JXPathContext doc, CdfRunJsDashboardWriteContext context ) {
    super( doc, context );
  }

  @Override
  public String render( String alias ) throws Exception {
    StringBuffer layout = new StringBuffer( "" );

    try {
      final Iterator<Pointer> rootRows = doc.iteratePointers( MessageFormat.format( XPATH_FILTER, UNIQUEID ) );

      layout.append( NEWLINE ).append( getIndent( 2 ) ).append( "<div class='container'>" );
      renderRows( doc, rootRows, layout, 4 );
      layout.append( NEWLINE ).append( getIndent( 2 ) ).append( "</div>" );
    } catch ( RenderException e ) {
      layout = new StringBuffer( e.getMessage() );
    }

    return layout.toString();
  }

  private void renderRows( final JXPathContext doc, final Iterator<Pointer> nodeIterator, final StringBuffer layout,
                           final int ident ) throws Exception {
    while ( nodeIterator.hasNext() ) {
      final Pointer pointer = nodeIterator.next();
      final JXPathContext context = doc.getRelativeContext( pointer );
      final String rowId = (String) context.getValue( "id" );
      final Render renderer = (Render) getRender( context );

      renderer.processProperties();

      layout.append( NEWLINE ).append( getIndent( ident ) );
      layout.append( renderer.renderStart() );

      // Render Child Rows
      renderRows( context, context.iteratePointers( MessageFormat.format( XPATH_FILTER, rowId ) ), layout, ident + 2 );

      layout.append( NEWLINE ).append( getIndent( ident ) );
      layout.append( renderer.renderClose() );
    }
  }

  @Override
  protected String getRenderClassName( final String type ) {
    return "pt.webdetails.cdf.dd.render.layout." + type.replace( "Layout", "" ) + "Render";
  }
}
