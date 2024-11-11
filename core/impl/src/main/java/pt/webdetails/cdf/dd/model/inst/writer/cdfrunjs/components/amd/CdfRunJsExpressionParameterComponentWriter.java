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


package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd;

import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.util.JsonUtils;

import static pt.webdetails.cdf.dd.CdeConstants.Writer.NEWLINE;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.PROPER_EXPRESSION_CONTEXT;

public class CdfRunJsExpressionParameterComponentWriter extends CdfRunJsParameterComponentWriter {

  @Override
  public void write( StringBuilder out, CdfRunJsDashboardWriteContext context, ParameterComponent comp )
    throws ThingWriteException {
    String name = JsonUtils.toJsString( comp.getId() );
    String value = sanitizeExpression( comp.tryGetPropertyValue( "javaScript", "" ) );
    Boolean isBookmarkable = "true".equalsIgnoreCase( comp.tryGetPropertyValue( "bookmarkable", null ) );

    addSetParameterAssignment( out, name, value );
    if ( isBookmarkable ) {
      addBookmarkable( out, name );
    }
  }

  protected static String sanitizeExpression( String expr ) {
    expr = expr.replaceAll( "[;\\s]+$", "" );
    if ( expr.startsWith( "function" ) ) {
      return bindProperContext( expr );
    } else {
      return bindProperContext( "function() { return " + expr + NEWLINE + "}" ) + "()";
    }
  }

  /**
   * Binds a function with a proper context.
   * This allows to call this.dashboard inside the function.
   * Typically used by custom parameters, ensures consistency.
   *
   * @param expr Function to bind context to.
   * @return the properly bound function.
   * */
  protected static String bindProperContext( String expr ) {
    return "_.bind(" + expr + ", " + PROPER_EXPRESSION_CONTEXT + ")";
  }
}
