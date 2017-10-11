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
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang.StringUtils;
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

      if ( getContext().getDashboard().getWcdf().isRequire() ) {

        layout.append( NEWLINE ).append( getIndent( 2 ) ).append( "<div class='" )
          .append( StringUtils.isEmpty( alias ) ? "container'>" : alias + "_container'>" );
        // setting indent to -1 prevents NEWLINE and indentation to be appended to layout
        renderRows( doc, layoutRows, null, alias, layout, -1 );
        layout.append( NEWLINE ).append( getIndent( 2 ) ).append( "</div>" );

      } else {

        layout.append( NEWLINE ).append( getIndent( 2 ) ).append( "<div class='container'>" );
        renderRows( doc, layoutRows, getWidgets( alias ), alias, layout, 4 );
        layout.append( NEWLINE ).append( getIndent( 2 ) ).append( "</div>" );

      }

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
      // when rendering rows for the layout of a dashboard AMD module, skip NEWLINE and indentation
      if ( indent > -1 ) {
        layout
          .append( NEWLINE )
          .append( getIndent( indent ) )
          .append( renderer.renderStart() );
        if ( widgetsByContainerId != null && widgetsByContainerId.containsKey( rowName ) ) {
          CdfRunJsDashboardWriteResult widgetResult = widgetsByContainerId.get( rowName );
          layout.append( widgetResult.getLayout() );
        } else {
          renderRows( context, context.iteratePointers( MessageFormat.format( XPATH_FILTER, rowId, "" ) ),
              widgetsByContainerId, alias, layout, indent + 2 );
        }
        layout
          .append( NEWLINE )
          .append( getIndent( indent ) )
          .append( renderer.renderClose() );
      } else {
        layout.append( renderer.renderStart() );
        if ( widgetsByContainerId != null && widgetsByContainerId.containsKey( rowName ) ) {
          CdfRunJsDashboardWriteResult widgetResult = widgetsByContainerId.get( rowName );
          layout.append( widgetResult.getLayout() );
        } else {
          renderRows( context, context.iteratePointers( MessageFormat.format( XPATH_FILTER, rowId, "" ) ),
              widgetsByContainerId, alias, layout, -1 );
        }
        layout.append( renderer.renderClose() );
      }
    }
  }

  @Override
  protected String getRenderClassName( final String type ) {
    return "pt.webdetails.cdf.dd.render.layout." + type.replace( "Layout", "" ) + "Render";
  }
}
