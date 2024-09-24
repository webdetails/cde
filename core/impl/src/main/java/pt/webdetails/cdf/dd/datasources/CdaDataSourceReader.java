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
