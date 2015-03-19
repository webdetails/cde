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

package pt.webdetails.cdf.dd.render;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;
import pt.webdetails.cdf.dd.render.layout.Render;
import pt.webdetails.cdf.dd.util.XPathUtils;

public class RenderLayout extends Renderer {
  public RenderLayout( JXPathContext doc, CdfRunJsDashboardWriteContext context ) {
    super( doc, context );
  }

  public String render( String alias ) throws Exception {
    try {
      @SuppressWarnings( "unchecked" )
      final Iterator<Pointer> rootRows = doc.iteratePointers( "/layout/rows[parent='UnIqEiD']" );

      StringBuffer layout = new StringBuffer();

      layout.append( NEWLINE ).append( getIndent( 2 ) ).append( "<div class='container'>" );

      Map<String, CdfRunJsDashboardWriteResult> widgetsByContainerId = getWidgets( alias );

      renderRows( doc, rootRows, widgetsByContainerId, alias, layout, 4 );

      layout.append( NEWLINE ).append( getIndent( 2 ) ).append( "</div>" );

      return layout.toString();
    } catch ( RenderException ex ) {
      return ex.getMessage();
    }
  }

  private void renderRows(
    final JXPathContext doc,
    final Iterator<Pointer> nodeIterator,
    final Map<String, CdfRunJsDashboardWriteResult> widgetsByContainerId,
    final String alias,
    final StringBuffer layout,
    final int indent ) throws Exception {
    while ( nodeIterator.hasNext() ) {
      final Pointer pointer = nodeIterator.next();
      final JXPathContext context = doc.getRelativeContext( pointer );

      final String rowId = (String) context.getValue( "id" );
      final String rowName = XPathUtils.getStringValue( context, "properties[name='name']/value" );

      @SuppressWarnings( "unchecked" )
      final Render renderer = (Render) getRender( context );
      renderer.processProperties();
      renderer.aliasId( alias );
      layout.append( NEWLINE ).append( getIndent( indent ) );
      layout.append( renderer.renderStart() );

      if ( widgetsByContainerId.containsKey( rowName ) ) {
        CdfRunJsDashboardWriteResult widgetResult = widgetsByContainerId.get( rowName );
        layout.append( widgetResult.getLayout() );
      } else {
        renderRows(
          context,
          context.iteratePointers( "/layout/rows[parent='" + rowId + "']" ),
          widgetsByContainerId,
          alias,
          layout,
          indent + 2 );
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