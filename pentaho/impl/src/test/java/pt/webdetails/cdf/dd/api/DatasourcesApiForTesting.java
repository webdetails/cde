/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package pt.webdetails.cdf.dd.api;

import pt.webdetails.cdf.dd.datasources.CdaDataSourceReader;
import pt.webdetails.cdf.dd.datasources.CdaDataSourceReaderForTesting;

import java.util.List;

public class DatasourcesApiForTesting extends DatasourcesApi {

  protected List<CdaDataSourceReader.CdaDataSource> getCdaDataSources( String dashboard ) {
    return CdaDataSourceReaderForTesting.getCdaDataSources( dashboard );
  }
}
