package pt.webdetails.cdf.dd.util;

import junit.framework.Assert;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

public class JsonUtilsTest {

  @Before
  public void setUp() throws Exception {

  }
  @Test
  public void testToJXPathContext() throws JSONException {
    JXPathContext jxPathContext = JsonUtils.toJXPathContext( createTestJSON() );

    Assert.assertEquals( "simple", jxPathContext.getValue( "/property1" ) );
    Assert.assertEquals( "simple", jxPathContext.getValue( "//property2" ));
    Assert.assertEquals( "test", jxPathContext.getValue( "//property4" ) );
    Assert.assertEquals( "test", jxPathContext.getValue( "//property4" ) );

    ArrayList<Object> array = (ArrayList<Object>) jxPathContext.getValue( "//array2" );
    Assert.assertEquals( "inside json", array.get( 0 ) );
    Assert.assertEquals( 3, array.get( 1 ) );
    Map<String, Object> map = (Map<String, Object>) array.get( 2 );
    Assert.assertEquals("property4", map.keySet().toArray()[0] );
    Assert.assertEquals("test", map.get( "property4" ) );

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
