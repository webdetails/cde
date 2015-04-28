package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import junit.framework.Assert;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.junit.Test;
import org.junit.BeforeClass;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.legacy.CdfRunJsThingWriterFactory;
import pt.webdetails.cdf.dd.model.meta.DashboardType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class PentahoCdfRunJsDashboardWriteContextTest {

  private static final String GET_RESOURCES = "api/resources";
  private static final String ROOT = "test-resources";
  private static final String TEST_FOLDER = "test";
  private static final String DASHBOARD = "testDashboard.wcdf";
  private static final String SYSTEM = "system";
  private static final String TEST_PLUGIN = "test-plugin";
  private static final String CDE_PLUGIN_URL = "/pentaho/plugin/pentaho-cdf-dd/";

  private static final String indent = "";
  private static final boolean bypassCacheRead = true;

  private static CdfRunJsDashboardWriteOptions options;
  private static CdfRunJsThingWriterFactory factory;

  private static PentahoCdfRunJsDashboardWriteContext context;

  @BeforeClass
  public static void setUp() throws Exception {
    factory = new CdfRunJsThingWriterFactory();
    options = getCdfRunJsDashboardWriteOptions();
  }

  @Test
  public void testReplaceTokensForSolutionDashboard() {
    //setup context
    String dashboardPath = RepositoryHelper.joinPaths( ROOT, TEST_FOLDER, DASHBOARD ).substring( 1 );
    context = new PentahoCdfRunJsDashboardWriteContextForTesting( factory,
      indent, bypassCacheRead, getDashboard( dashboardPath, false ), options );

    String jsResource = "${res:script.js}";
    String cssResource = "${res:style.css}";
    String absoluteResource = "${res:/test-resources/style.css}";
    String solutionAbsoluteResource = "${solution:script.js}";
    String solutionRelativeResource = "${solution:/test-resources/script.js}";

    String jsResourceExpected = CDE_PLUGIN_URL +
      RepositoryHelper.joinPaths( GET_RESOURCES, ROOT, TEST_FOLDER, "script.js" ).substring( 1 );
    String cssResourceExpected =
      CDE_PLUGIN_URL + RepositoryHelper.joinPaths( GET_RESOURCES, ROOT, TEST_FOLDER, "style.css" ).substring(
        1 );
    String absoluteExpected =
      CDE_PLUGIN_URL + RepositoryHelper.joinPaths( GET_RESOURCES, ROOT, "style.css" ).substring( 1 );
    String solutionAbsoluteResourceExpected = CDE_PLUGIN_URL +
      RepositoryHelper.joinPaths( GET_RESOURCES, ROOT, TEST_FOLDER, "script.js" ).substring(
        1 );
    String solutionRelativeResourceExpected =
      CDE_PLUGIN_URL + RepositoryHelper.joinPaths( GET_RESOURCES, ROOT, "script.js" ).substring(
        1 );

    String jsResourceReplaced = removeParams( context.replaceTokens( jsResource ) );
    String cssResourceReplaced = removeParams( context.replaceTokens( cssResource ) );
    String absoluteResourceReplaced = removeParams( context.replaceTokens( absoluteResource ) );
    String solutionAbsoluteResourceReplaced = removeParams( context.replaceTokens( solutionAbsoluteResource ) );
    String solutionRelativeResourceReplaced = removeParams( context.replaceTokens( solutionRelativeResource ) );

    Assert.assertEquals( "${res:script.js} replacement failed", jsResourceExpected, jsResourceReplaced );
    Assert.assertEquals( "${res:style.css} replacement failed", cssResourceExpected, cssResourceReplaced );
    Assert.assertEquals( "${res:/test-resources/style.css} replacement failed", absoluteExpected,
      absoluteResourceReplaced );
    Assert.assertEquals( "${solution:script.js} replacement failed", solutionAbsoluteResourceExpected,
      solutionAbsoluteResourceReplaced );
    Assert.assertEquals( "${solution:/test-resources/script.js} replacement failed", solutionRelativeResourceExpected,
      solutionRelativeResourceReplaced );
  }

  @Test
  public void testReplaceTokensForSystemDashboard() {
    //setup context
    String dashboardPath =
      RepositoryHelper.joinPaths( ROOT, SYSTEM, TEST_PLUGIN, TEST_FOLDER, DASHBOARD ).substring( 1 );
    context = new PentahoCdfRunJsDashboardWriteContextForTesting( factory,
      indent, bypassCacheRead, getDashboard( dashboardPath, true ), options );

    String systemRelativeResource = "${system:script.js}";
    String systemAbsoluteResource = "${system:/test/script.js}";

    String systemResourceExpected = CDE_PLUGIN_URL +
      RepositoryHelper.joinPaths( GET_RESOURCES, SYSTEM, TEST_PLUGIN, TEST_FOLDER, "script.js" ).substring( 1 );

    String systemAbsoluteResourceReplaced = removeParams( context.replaceTokens( systemAbsoluteResource ) );
    String systemRelativeResourceReplaced = removeParams( context.replaceTokens( systemRelativeResource ) );

    Assert
      .assertEquals( "${system:script.js} replacement failed", systemResourceExpected, systemAbsoluteResourceReplaced );
    Assert.assertEquals( "${system:/test/script.js} replacement failed", systemResourceExpected,
      systemRelativeResourceReplaced );
  }

  @Test
  public void testAbsoluteSchemePaths() {
    String jsResourceReplaced;
    //setup context
    String dashboardPath = RepositoryHelper.joinPaths( ROOT, TEST_FOLDER, DASHBOARD ).substring( 1 );

    options = new CdfRunJsDashboardWriteOptions( false, false, "", "" );
    context = new PentahoCdfRunJsDashboardWriteContextForTesting( factory,
      indent, bypassCacheRead, getDashboard( dashboardPath, false ), options );

    String jsResource = "${res:script.js}";

    String jsResourceExpected = CDE_PLUGIN_URL +
      RepositoryHelper.joinPaths( GET_RESOURCES, ROOT, TEST_FOLDER, "script.js" ).substring( 1 );
    String jsResourceAbsoluteExpected = "http://localhost:8080/pentaho/plugin/pentaho-cdf-dd" +
      RepositoryHelper.joinPaths( GET_RESOURCES, ROOT, TEST_FOLDER, "script.js" );

    jsResourceReplaced = removeParams( context.replaceTokens( jsResource ) );
    Assert.assertEquals( "${res:script.js} replacement failed", jsResourceExpected, jsResourceReplaced );

    options = new CdfRunJsDashboardWriteOptions( true, false, "localhost:8080", "http" );
    context = new PentahoCdfRunJsDashboardWriteContextForTesting( factory,
      indent, bypassCacheRead, getDashboard( dashboardPath, false ), options );

    jsResourceReplaced = removeParams( context.replaceTokens( jsResource ) );
    Assert.assertEquals( "${res:script.js} replacement failed", jsResourceAbsoluteExpected, jsResourceReplaced );

    options = new CdfRunJsDashboardWriteOptions( false, false, "localhost:8080", "http" );
    context = new PentahoCdfRunJsDashboardWriteContextForTesting( factory,
      indent, bypassCacheRead, getDashboard( dashboardPath, false ), options );

    jsResourceReplaced = removeParams( context.replaceTokens( jsResource ) );
    Assert.assertEquals( "${res:script.js} replacement failed", jsResourceExpected, jsResourceReplaced );


  }


  private static Dashboard getDashboard( String path, boolean isSystem ) {
    Document wcdfDoc = null;
    try {
      wcdfDoc = Utils.getDocument( new FileInputStream( new File( path ) ) );
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

    if ( !isSystem ) {
      builder.setSourcePath( path );
    } else {
      // this is needed because we need to remove the test-resources path used to get the file
      // this is secure because there are no folder before the system while getting files from the server
      builder.setSourcePath( path.replace( ROOT + "/", "" ) );
    }

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

  private static CdfRunJsDashboardWriteOptions getCdfRunJsDashboardWriteOptions() {
    boolean absolute = false;
    boolean debug = false;
    String absRoot = "";
    String scheme = "";
    return new CdfRunJsDashboardWriteOptions( absolute, debug, absRoot, scheme );
  }

  private String removeParams( String msg ) {
    return msg.substring( 0, msg.indexOf( "?" ) );
  }

}
