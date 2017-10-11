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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.DashboardManager;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.WidgetComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;

@SuppressWarnings( "unchecked" )
public abstract class Renderer {
  protected static final String UNIQUEID = "UnIqEiD";
  protected static final String XPATH_FILTER = "/layout/rows[parent=''{0}'' {1}]";

  protected static final String NEWLINE = System.getProperty( "line.separator" );
  protected static final Class<JXPathContext>[] rendererConstructorArgs = new Class[] { JXPathContext.class };
  protected static Log logger = LogFactory.getLog( Renderer.class );
  protected final JXPathContext doc;
  protected final CdfRunJsDashboardWriteContext _context;

  public Renderer( JXPathContext doc, CdfRunJsDashboardWriteContext context ) {
    this.doc = doc;
    this._context = context;
  }

  public abstract String render( final String alias ) throws Exception;

  protected abstract String getRenderClassName( String Type );

  /**
   * Obtains Widgets (renderers) for contained components (usages).
   * <p/>
   * All returned widgets are loaded with an alias that is prefixed by the specified aliasPrefix argument.
   */
  protected final Map<String, CdfRunJsDashboardWriteResult> getWidgets( String aliasPrefix ) {
    Map<String, CdfRunJsDashboardWriteResult> widgetsByContainerId = new HashMap<String, CdfRunJsDashboardWriteResult>();

    Dashboard dashboard = this.getContext().getDashboard();
    if ( dashboard.getRegularCount() > 0 ) {
      DashboardManager dashMgr = DashboardManager.getInstance();
      CdfRunJsDashboardWriteOptions options = this.getContext().getOptions();

      Iterable<Component> components = dashboard.getRegulars();
      for ( Component comp : components ) {
        if ( StringUtils.isNotEmpty( comp.getName() ) && comp instanceof WidgetComponent ) {
          WidgetComponent widgetComp = (WidgetComponent) comp;
          CdfRunJsDashboardWriteOptions childOptions = options.addAliasPrefix( comp.getName() ); // <-- NOTE:!
          CdfRunJsDashboardWriteResult dashResult = null;
          try {
            dashResult = dashMgr.getDashboardCdfRunJs( widgetComp.getWcdfPath(), childOptions,
              this.getContext().isBypassCacheRead() );
          } catch ( ThingWriteException ex ) {
            logger.error( "Could not render widget '" + widgetComp.getWcdfPath() + "'", ex );
          }

          String containerId =
              widgetComp.tryGetPropertyValue( "htmlObject", "" ).replaceAll( "\\$\\{.*:(.*)\\}", "$1" );
          widgetsByContainerId.put( containerId, dashResult );
        }
      }
    }

    return widgetsByContainerId;
  }

  protected Object getRender( JXPathContext context ) throws Exception {
    String renderType = null;
    try {
      renderType = (String) context.getValue( "type" );
      if ( !renderType.equals( "Label" ) ) {
        Class<?> rendererClass = Class.forName( getRenderClassName( renderType ) );

        Constructor<?> constructor = rendererClass.getConstructor( rendererConstructorArgs );
        return constructor.newInstance( new Object[] { context } );
      }
    } catch ( InstantiationException e ) {
      logger.error( e.getStackTrace() );
    } catch ( IllegalAccessException e ) {
      // TODO Auto-generated catch block
      logger.error( e.getStackTrace() );
    } catch ( ClassNotFoundException e ) {
      // TODO Auto-generated catch block
      logger.error( "Class not found: " + renderType );
      //throw new RenderException("Render not found for: " + renderType);
    }

    return null;
  }

  protected final String getIndent( int indent ) {
    switch ( indent ) {
      case 0:
        return "";
      case 1:
        return " ";
      case 2:
        return "  ";
      case 3:
        return "   ";
      case 4:
        return "    ";
      case 8:
        return "        ";
    }

    StringBuilder identStr = new StringBuilder();
    for ( int i = 0; i < indent; i++ ) {
      identStr.append( " " );
    }
    return identStr.toString();
  }

  public static String aliasName( String aliasPrefix, String name ) {
    aliasPrefix = StringUtils.isEmpty( aliasPrefix ) ? "" : ( aliasPrefix + "_" );

    return aliasPrefix + name;
  }

  public CdfRunJsDashboardWriteContext getContext() {
    return _context;
  }
}
