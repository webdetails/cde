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

package pt.webdetails.cdf.dd.model.meta.reader.datasources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cdf.dd.model.meta.DataSourceComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cpf.packager.origin.PathOrigin;
import pt.webdetails.cpf.packager.origin.OtherPluginStaticSystemOrigin;

import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.AttributeName.META;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.AttributeName.CONNECTION_TYPE;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.AttributeName.DATA_ACCESS_TYPE;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.PropertyValue.CPK_QUERY_TYPE;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.META_TYPE_CDA;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.META_TYPE_CPK;

/**
 * Loads XML model files, component types and property types, from the file system, of a Pentaho CDE plugin
 * instalation.
 */
public final class DataSourcesModelReader {
  protected static final Log logger = LogFactory.getLog( DataSourcesModelReader.class );


  public void read( MetaModel.Builder model, JSONObject cdaDefs, String sourcePath )
    throws ThingReadException {
    assert model != null;

    logger.info( "Loading data source components of '" + sourcePath + "'" );

    final JXPathContext doc;
    try {
      doc = JsonUtils.toJXPathContext( cdaDefs );
    } catch ( JSONException e ) {
      throw new ThingReadException( "Couldn't get JXPathContext from json", e );
    }

    @SuppressWarnings( "unchecked" )
    Iterator<Pointer> pointers = doc.iteratePointers( "*" );

    while ( pointers.hasNext() ) {
      Pointer pointer = pointers.next();
      this.readDataSourceComponent( model, pointer, sourcePath );
    }
  }

  /**
   * @param model      where component type builders are placed
   * @param pointer
   * @param sourcePath sourcePath for all components
   */
  private void readDataSourceComponent( MetaModel.Builder model, Pointer pointer, String sourcePath ) {
    // TODO: What a generality...

    DataSourceComponentType.Builder builder = new DataSourceComponentType.Builder();

    Map<String, Object> def = (Map<String, Object>) pointer.getNode();
    JXPathContext jctx = JXPathContext.newContext( def );

    String name = pointer.asPath().replaceAll( ".*name='(.*?)'.*", "$1" );
    String label = (String) jctx.getValue( "metadata/name" );

    String category = (String) jctx.getValue( "metadata/group" );
    String categoryLabel = (String) jctx.getValue( "metadata/groupdesc" );

    String dataSourceType = (String) jctx.getValue( "metadata/datype" );
    boolean isCPK = dataSourceType.equalsIgnoreCase( CPK_QUERY_TYPE );

    //TODO: oh so wrong
    PathOrigin origin = new OtherPluginStaticSystemOrigin( isCPK ? "cpk" : "cda", "" );
    builder.setOrigin( origin );

    logger.debug( String.format( "\t%s", label ) );

    builder
      .setName( name )
      .setLabel( label )
      .setTooltip( label )
      .setCategory( category )
      .setCategoryLabel( categoryLabel )
      .setSourcePath( sourcePath )
      .addAttribute( META, isCPK ? META_TYPE_CPK : META_TYPE_CDA ); // meta: "CDA"

    if ( isCPK ) {
      readCPKDataSourceComponent( builder, jctx );
    } else {
      readCDADataSourceComponent( builder, jctx );
    }

    model.addComponent( builder );
  }

  private void readCDADataSourceComponent( DataSourceComponentType.Builder builder, JXPathContext jctx ) {
    String label = (String) jctx.getValue( "metadata/name" );

    String dataSourceType = (String) jctx.getValue( "metadata/datype" );
    String connectionType = (String) jctx.getValue( "metadata/conntype" );
    connectionType = connectionType != null ? connectionType : "";

    builder
      .addAttribute( CONNECTION_TYPE, connectionType )
      .addAttribute( DATA_ACCESS_TYPE, dataSourceType );

    for ( String cdaPropName : this.getCDAPropertyNames( jctx ) ) {
      if ( cdaPropName.equals( "id" ) || cdaPropName.equals( "connection" ) ) {
        continue;
      } else if ( cdaPropName.equals( "columns" ) ) {
        builder.useProperty( null, "cdacolumns" );
        builder.useProperty( null, "cdacalculatedcolumns" );
      } else if ( cdaPropName.equals( "output" ) ) {
        builder.useProperty( null, "output" );
        builder.useProperty( null, "outputMode" );
      } else if ( cdaPropName.equals( "left" ) ) {
        builder.useProperty( null, "left" );
        builder.useProperty( null, "leftkeys" );
      } else if ( cdaPropName.equals( "right" ) ) {
        builder.useProperty( null, "right" );
        builder.useProperty( null, "rightkeys" );
      } else if ( isKettleOverX( label ) && cdaPropName.equalsIgnoreCase( "query" ) ) {
        builder.useProperty( cdaPropName, "kettleQuery" );
      } else {
        builder.useProperty( null, cdaPropName );
      }
    }
  }

  private void readCPKDataSourceComponent( DataSourceComponentType.Builder builder, JXPathContext jctx ) {
    builder
      .useProperty( null, "stepName" )
      .useProperty( null, "kettleOutput" )
      .addAttribute( "pluginId", (String) jctx.getValue( "metadata/pluginId" ) )
      .addAttribute( "endpoint", (String) jctx.getValue( "metadata/endpoint" ) );
  }

  private List<String> getCDAPropertyNames( JXPathContext jctx ) {
    ArrayList<String> props = new ArrayList<>();

    Map<String, Object> connection = (Map<String, Object>) jctx.getValue( "definition/connection" );
    if ( connection != null ) {
      props.addAll( connection.keySet() );
    }

    Map<String, Object> dataaccess = (Map<String, Object>) jctx.getValue( "definition/dataaccess" );
    if ( dataaccess != null ) {
      props.addAll( dataaccess.keySet() );
    }

    return props;
  }

  private boolean isKettleOverX( String label ) {
    // This specific Data Source has special treatment below
    return "kettle over kettleTransFromFile".equalsIgnoreCase( label );
  }
}
