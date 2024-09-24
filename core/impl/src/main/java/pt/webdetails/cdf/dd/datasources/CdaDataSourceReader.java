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

package pt.webdetails.cdf.dd.datasources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.apache.commons.lang.StringUtils;

import org.json.JSONException;
import pt.webdetails.cdf.dd.DashboardManager;
import pt.webdetails.cdf.dd.util.XPathUtils;

public class CdaDataSourceReader {
  public static class CdaDataSource {
    String cdaSettings;
    String dataAccessId;

    CdaDataSource( JXPathContext dataSourceContext ) {
      this( dataSourceContext, null );
    }

    CdaDataSource( JXPathContext dataSourceContext, String cdaSettings ) {
      this.dataAccessId = XPathUtils.getStringValue( dataSourceContext, "properties/value[../name='name']" );

      if ( cdaSettings != null ) {
        this.cdaSettings = cdaSettings;
      } else {
        if ( XPathUtils.exists( dataSourceContext, "properties/value[../name='cdaPath']" ) ) {
          this.cdaSettings = XPathUtils.getStringValue( dataSourceContext, "properties/value[../name='cdaPath']" );
        } else {
          this.cdaSettings =
              XPathUtils.getStringValue( dataSourceContext, "properties/value[../name='solution']" ) + '/' + XPathUtils
                  .getStringValue( dataSourceContext, "properties/value[../name='path']" ) + '/' + XPathUtils
                  .getStringValue( dataSourceContext, "properties/value[../name='file']" );
        }
      }
    }

    public CdaDataSource( String cdaSettings, String dataAccessId ) {
      this.cdaSettings = cdaSettings;
      this.dataAccessId = dataAccessId;
    }

    public String toString() {
      return "{cdaSettingsId:'" + cdaSettings + "'" + ( dataAccessId != null
          ? ( ", dataAccessId:'" + dataAccessId + "'" ) : "" ) + "}";
    }
  }

  // TODO: The instance model
  // already has a DataSourceComponent list in the Dashboard object...
  public static List<CdaDataSource> getCdaDataSources( String dashboard ) throws JSONException {
    JXPathContext context;

    try {
      context = DashboardManager.openDashboardAsJXPathContext( dashboard, /* wcdf */null );
    } catch ( FileNotFoundException e ) {
      return null;
    } catch ( IOException e ) {
      return null;
    }
    return getCdaDataSources( context );
  }

  protected static List<CdaDataSource> getCdaDataSources( JXPathContext docContext ) {
    ArrayList<CdaDataSource> dataSources = new ArrayList<CdaDataSource>();
    //external
    @SuppressWarnings( "unchecked" )
    Iterator<Pointer> extDataSources =
        docContext.iteratePointers( "/datasources/rows[properties/name='dataAccessId']" );
    while ( extDataSources.hasNext() ) {
      Pointer source = extDataSources.next();
      if ( !( source instanceof NullPointer ) ) {
        dataSources.add( new CdaDataSource( docContext.getRelativeContext( source ) ) );
      }
    }

    @SuppressWarnings( "unchecked" )
    Iterator<Pointer> builtInDataSources = docContext.iteratePointers( "/datasources/rows[meta='CDA']" );
    if ( builtInDataSources.hasNext() ) {
      //built-in
      String fileName = XPathUtils.getStringValue( docContext, "/filename" );
      if ( StringUtils.endsWith( fileName, ".wcdf" ) ) {
        fileName = StringUtils.replace( fileName, ".wcdf", ".cda" );
      } else {
        fileName = StringUtils.replace( fileName, ".cdfde", ".cda" );
      }
      //just add cda name
      dataSources.add( new CdaDataSource( fileName, null ) );
    }

    return dataSources;
  }
}
