package pt.webdetails.cdf.dd.structure;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;

public class DashboardWcdfDescriptorTest extends TestCase {

  private DashboardWcdfDescriptorForTest wcdf;


  private class DashboardWcdfDescriptorForTest extends DashboardWcdfDescriptor {

    private HashMap<String, Object> parameters;

    public DashboardWcdfDescriptorForTest() {
      super();
      this.parameters = new HashMap<String, Object>();
    }

    public DashboardWcdfDescriptorForTest( String[] params ) {
      this();
    }

    private void addParameter(String key, Object value) {
      this.parameters.put( key, value );
    }

    private HashMap<String, Object> getParameters() {
      return this.parameters;
    }

    private String[] getWidgetParamsForTest() {
      return (String[]) this.parameters.get( "widgetParameters" );
    }

  }


  @Test
  public void testUpdateWidgetParamsSimple() {
    wcdf = new DashboardWcdfDescriptorForTest();
    wcdf.update( wcdf.getParameters() );


    Assert.assertTrue( "Should continue empty",wcdf.getWidgetParameters().length == 0 );
  }

  @Test
  public void testUpdateWidgetParamsInsert() {
    wcdf = new DashboardWcdfDescriptorForTest();
    wcdf.addParameter( "widgetParameters", new String[]{"param1"} );
    wcdf.update( wcdf.getParameters() );

    Assert.assertTrue( "Should have widget parameter 'param1'", wcdf.getWidgetParameters()[0].equals( "param1" ) );
  }

  @Test
  public void testUpdateWidgetParamsRemove() {
    wcdf = new DashboardWcdfDescriptorForTest( new String[]{"param1"} );
    wcdf.update( wcdf.getParameters() );

    Assert.assertTrue( "'param1' should have been removed", wcdf.getWidgetParameters().length == 0 );
  }
}
