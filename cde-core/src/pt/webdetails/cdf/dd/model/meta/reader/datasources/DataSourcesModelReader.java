/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*                
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed unde r the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cdf.dd.model.meta.reader.datasources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.model.meta.DataSourceComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cpf.packager.origin.PathOrigin;
import pt.webdetails.cpf.packager.origin.OtherPluginStaticSystemOrigin;

/**
 * Loads XML model files, component types and property types, from the file system, of a Pentaho CDE plugin
 * instalation.
 *
 * @author dcleao
 */
public final class DataSourcesModelReader {
  protected static final Log logger = LogFactory.getLog( DataSourcesModelReader.class );


  public void read( MetaModel.Builder model, JSON cdaDefs, String sourcePath ) throws ThingReadException {
    assert model != null;

    logger.info( "Loading data source components of '" + sourcePath + "'" );

    final JXPathContext doc = JXPathContext.newContext( cdaDefs );

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

    JSONObject def = (JSONObject) pointer.getNode();
    JXPathContext jctx = JXPathContext.newContext( def );

    String label = (String) jctx.getValue( "metadata/name" );

    String dataSourceType = (String) jctx.getValue( "metadata/datype" );

    boolean isCPK = dataSourceType.equalsIgnoreCase( "cpk" );
    boolean isCDA = !isCPK;

    //TODO: oh so wrong
    PathOrigin origin = new OtherPluginStaticSystemOrigin( isCPK ? "cpk" : "cda", "" );
    builder.setOrigin( origin );

    // This specific Data Source has special treatment below
    boolean isKettleOverX = isCDA && "kettle over kettleTransFromFile".equalsIgnoreCase( label );

    logger.debug( String.format( "\t%s", label ) );

    String connType = (String) jctx.getValue( "metadata/conntype" );
    connType = connType != null ? connType : "";

    builder
      .setName( pointer.asPath().replaceAll( ".*name='(.*?)'.*", "$1" ) )
      .setLabel( label )
      .setTooltip( label )
      .setCategory( (String) jctx.getValue( "metadata/group" ) )
      .setCategoryLabel( (String) jctx.getValue( "metadata/groupdesc" ) )
      .setSourcePath( sourcePath )
      .addAttribute( "", isCPK ? "CPK" : "CDA" ); // meta: "CDA"

    if ( isCDA ) {
      builder
        .addAttribute( "conntype", connType )
        .addAttribute( "datype", dataSourceType );
    } else if ( isCPK ) {
      builder
        .useProperty( null, "stepName" )
        .useProperty( null, "kettleOutput" )
        .addAttribute( "pluginId", (String) jctx.getValue( "metadata/pluginId" ) )
        .addAttribute( "endpoint", (String) jctx.getValue( "metadata/endpoint" ) );
    }

    if ( isCDA ) {
      for ( String cdaPropName : this.getCDAPropertyNames( def ) ) {
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
        } else if ( isKettleOverX && cdaPropName.equalsIgnoreCase( "query" ) ) {
          builder.useProperty( cdaPropName, "kettleQuery" );
        } else {
          builder.useProperty( null, cdaPropName );
        }
      }
    }
    model.addComponent( builder );
  }

  private List<String> getCDAPropertyNames( JSONObject def ) {
    ArrayList<String> props = new ArrayList<String>();

    JXPathContext context = JXPathContext.newContext( def );

    JSONObject connection = (JSONObject) context.getValue( "definition/connection", JSONObject.class );
    if ( connection != null ) {
      @SuppressWarnings( "unchecked" )
      Set<String> keys = connection.keySet();
      props.addAll( keys );
    }

    JSONObject dataaccess = (JSONObject) context.getValue( "definition/dataaccess", JSONObject.class );
    if ( dataaccess != null ) {
      @SuppressWarnings( "unchecked" )
      Set<String> keys = dataaccess.keySet();
      props.addAll( keys );
    }

    return props;
  }
}
