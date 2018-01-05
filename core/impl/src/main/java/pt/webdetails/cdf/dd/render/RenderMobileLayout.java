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
