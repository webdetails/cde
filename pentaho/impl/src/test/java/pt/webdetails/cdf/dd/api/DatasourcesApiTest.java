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


package pt.webdetails.cdf.dd.api;

import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DatasourcesApiTest {

  private final String DASHBOARD = "/src/test/resources/dummyDashboard/dummy.cdfde";
  private final String EXPECTED = "[{cdaSettingsId:'dummy/dummy.cda'}]";

  @BeforeClass
  public static void setUp() throws Exception {

  }

  @Test
  public void listCdaSourcesTest() throws JSONException {
    DatasourcesApiForTesting datasourcesApi = new DatasourcesApiForTesting();
    String actualResult = datasourcesApi.listCdaSources( DASHBOARD );

    assertEquals( EXPECTED, actualResult );
  }
}
