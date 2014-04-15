/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cdf.dd;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.engine.IParameterProvider;
import sun.security.x509.IPAddressName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;


public class DashboardDesignerContentGeneratorTest {

  private final String DASHBOARD = "/test-resources/dummyDashboard/dummy.cdfde";
  private final String EXPECTED = "[{cdaSettingsId:'dummy/dummy.cda'}]";

  @BeforeClass
  public static void setUp(){

  }

  @Test
  public void listCdaSourcesTest() throws IOException {
    MockParameterProvider requestMap = new MockParameterProvider();
    requestMap.setParameter("dashboard", DASHBOARD);
    Map<String,IParameterProvider > parameterProviders = new HashMap<String, IParameterProvider>();
    parameterProviders.put( "request", requestMap );
    DashboardDesignerContentGenerator ddcg = new DashboardDesignerContentGeneratorForTesting();
    ddcg.setParameterProviders( parameterProviders );
    OutputStream out = new ByteArrayOutputStream(  );
    ddcg.listCdaSources(out);
    String actualResult = out.toString();
    Assert.assertEquals( EXPECTED, actualResult );
  }

}
