package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.legacy;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.PentahoCdfRunJsDashboardWriteContext;

import static org.mockito.Mockito.when;

public class CdfRunJsParameterComponentWriterTest {
  private static final String ROOT = "test-resources";
  private static final String TEST_FOLDER = "test";
  private static final String DASHBOARD = "testDashboard.wcdf";

  private static IThingWriter writer;
  private static PentahoCdfRunJsDashboardWriteContext context;

  @BeforeClass
  public static void setUp() throws Exception {
    writer = new CdfRunJsParameterComponentWriter();
    context = Mockito.mock( PentahoCdfRunJsDashboardWriteContext.class );

  }

  @Test
  public void testParameterComponentWrite() {
    ParameterComponent parameterComponent = Mockito.mock( ParameterComponent.class );
    when(parameterComponent.tryGetPropertyValue( "propertyValue", "" )).thenReturn("1");
    when(parameterComponent.tryGetPropertyValue( "parameterViewRole", "unused" )).thenReturn("unused");

    when(context.getId( parameterComponent )).thenReturn( "param1" );

    StringBuilder dashboardResult = new StringBuilder();

    try {
      writer.write( dashboardResult, context, parameterComponent );

      Assert.assertEquals("Dashboards.addParameter(\"param1\", \"1\");\n"+
        "Dashboards.setParameterViewMode(\"param1\", \"unused\");\n", dashboardResult.toString());
    } catch ( ThingWriteException e ){}
  }

}
