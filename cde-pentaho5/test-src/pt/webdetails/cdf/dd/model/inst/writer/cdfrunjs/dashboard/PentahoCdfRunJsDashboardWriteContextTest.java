package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import junit.framework.Assert;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.junit.Test;
import org.junit.BeforeClass;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.CdfRunJsThingWriterFactory;
import pt.webdetails.cdf.dd.model.meta.DashboardType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class PentahoCdfRunJsDashboardWriteContextTest {

  private final String GET_RESOURCES = "api/resources";
  private static final String ROOT = "/test-resources/";
  private static final String TEST_FOLDER = "test/";
  private static final String DASHBOARD = "testDashboard";


  private static PentahoCdfRunJsDashboardWriteContext context;

  @BeforeClass
  public static void setUp() throws Exception {
    CdfRunJsThingWriterFactory factory = new CdfRunJsThingWriterFactory();
    String indent = "";
    boolean bypassCacheRead = true;
    Dashboard dashboard = getDashboard();
    CdfRunJsDashboardWriteOptions options = getCdfRunJsDashboardWriteOptions();

    context = new PentahoCdfRunJsDashboardWriteContextForTesting( factory, indent, bypassCacheRead, dashboard, options);
  }

  @Test
  public void testReplaceTokens(){
    String jsResource = "${res:script.js}";
    String cssResource = "${res:style.css}";
    String absoluteResource = "${res:/test-resources/style.css}";

    String jsResourceExpected = GET_RESOURCES + ROOT + TEST_FOLDER + "script.js";
    String cssResourceExpected = GET_RESOURCES + ROOT + TEST_FOLDER + "style.css";
    String absoluteExpected = GET_RESOURCES + ROOT + "style.css";

    String jsResourceReplaced = removeParams( context.replaceTokens( jsResource ) );
    String cssResourceReplaced = removeParams( context.replaceTokens( cssResource ) );
    String absoluteResourceReplaced = removeParams( context.replaceTokens( absoluteResource ) );

    Assert.assertEquals( "", jsResourceExpected, jsResourceReplaced );
    Assert.assertEquals( "", cssResourceExpected, cssResourceReplaced );
    Assert.assertEquals( "", absoluteExpected, absoluteResourceReplaced );
  }


  private static Dashboard getDashboard(){
    Document wcdfDoc = null;
    try {
      wcdfDoc = Utils.getDocument( new FileInputStream( new File("test-resources/test/" + DASHBOARD + ".wcdf")) );
    } catch ( DocumentException e ) {
      e.printStackTrace();
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
    }
    DashboardWcdfDescriptor wcdf = DashboardWcdfDescriptor.fromXml( wcdfDoc );
    Dashboard.Builder builder = new Dashboard.Builder();
    DashboardType dashboardType = null;
    try {
      dashboardType = new DashboardType.Builder().build();
    } catch ( ValidationException e ) {
      e.printStackTrace();
    }
    builder.setSourcePath( ROOT + TEST_FOLDER + DASHBOARD + ".wcdf");
    builder.setWcdf( wcdf );
    builder.setMeta( dashboardType );
    MetaModel.Builder metaBuilder = new MetaModel.Builder();
    MetaModel model;
    Dashboard dashboard = null;
    try {
      model = metaBuilder.build();
      dashboard = builder.build( model );

    } catch ( ValidationException e ) {
      e.printStackTrace();
    }
    return dashboard;
  }

  private static CdfRunJsDashboardWriteOptions getCdfRunJsDashboardWriteOptions(){
    boolean absolute = false;
    boolean debug = false;
    String absRoot = "";
    String scheme = "";
    return new CdfRunJsDashboardWriteOptions( absolute, debug, absRoot, scheme );
  }

  private String removeParams( String msg ) {
    return msg.substring( 0,msg.indexOf( "?" ) );
  }

}
