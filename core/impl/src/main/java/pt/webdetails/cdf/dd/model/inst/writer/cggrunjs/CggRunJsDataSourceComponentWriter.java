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

package pt.webdetails.cdf.dd.model.inst.writer.cggrunjs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.util.JsonUtils;

import static pt.webdetails.cdf.dd.CdeConstants.Writer.*;

public class CggRunJsDataSourceComponentWriter extends JsWriterAbstract implements IThingWriter {
  private static final Log logger = LogFactory.getLog( CggRunJsDataSourceComponentWriter.class );
  public void write( Object output, IThingWriteContext context, Thing t ) throws ThingWriteException {
    this.write( (StringBuilder) output, (CggRunJsComponentWriteContext) context, (DataSourceComponent) t );
  }

  public void write( StringBuilder out, CggRunJsComponentWriteContext context, DataSourceComponent comp )
    throws ThingWriteException {
    String jsParamsArray = comp.tryGetPropertyValue( "parameters", null );
    if ( jsParamsArray != null ) {
      try {
        renderParameters( out, new JSONArray( jsParamsArray ) );
      } catch ( JSONException e ) {
        throw new ThingWriteException( "Couldn't render parameters", e );
      }
    }
  }

  private void renderParameters( StringBuilder out, JSONArray params ) throws JSONException {
    if ( params.length() == 0 ) {
      return;
    }

    /* ex:
      cgg.initParameter
      ('productLine', 'Classic Cars')
      ('territory'    'EMEA')
      ;
     */
    out.append( "cgg.initParameter" );
    out.append( NEWLINE );

    for ( int i = 0; i < params.length(); i++ ) {
      JSONArray param = params.getJSONArray( i );
      String paramName = param.get( 0 ).toString();
      String defaultValue = param.get( 1 ).toString();

      out.append( "(" );
      out.append( JsonUtils.toJsString( paramName ) );
      out.append( ", " );
      out.append( JsonUtils.toJsString( defaultValue ) );
      out.append( ")" );
      out.append( NEWLINE );
    }

    out.append( ";" );
  }
}
