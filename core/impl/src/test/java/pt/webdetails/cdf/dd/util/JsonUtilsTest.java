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


package pt.webdetails.cdf.dd.util;

import org.apache.commons.jxpath.JXPathContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JsonUtilsTest {

  @Before
  public void setUp() throws Exception {

  }
  @Test
  public void testToJXPathContext() throws JSONException {
    JXPathContext jxPathContext = JsonUtils.toJXPathContext( createTestJSON() );

    assertEquals( "simple", jxPathContext.getValue( "/property1" ) );
    assertEquals( "simple", jxPathContext.getValue( "//property2" ) );
    assertEquals( "test", jxPathContext.getValue( "//property4" ) );
    assertEquals( "test", jxPathContext.getValue( "//property4" ) );

    ArrayList<Object> array = (ArrayList<Object>) jxPathContext.getValue( "//array2" );
    assertEquals( "inside json", array.get( 0 ) );
    assertEquals( 3, array.get( 1 ) );
    Map<String, Object> map = (Map<String, Object>) array.get( 2 );
    assertEquals("property4", map.keySet().toArray()[0] );
    assertEquals("test", map.get( "property4" ) );
  }

  private JSONObject createTestJSON() throws JSONException {
    JSONObject json = new JSONObject();
    JSONObject innerJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    JSONArray innerJsonArray = new JSONArray();

    jsonArray.put( "simple" ).put( 3 ).put( new JSONObject( "{property3:\"test\"}" ) );

    innerJsonArray.put( "inside json" ).put( 3 ).put( new JSONObject( "{property4:\"test\"}" ) );

    innerJson.put( "property2", "simple" );
    innerJson.put( "array2", innerJsonArray );

    json.put( "property1", "simple" );
    json.put( "json", innerJson );
    json.put( "array1", jsonArray );

    return json;
  }
}
