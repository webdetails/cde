package pt.webdetails.cdf.dd.render;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.render.layout.ResourceCodeRender;
import pt.webdetails.cdf.dd.render.layout.ResourceFileRender;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class RenderResourcesTest {
  JXPathContext doc;
  CdfRunJsDashboardWriteContext context;
  RenderResources renderResources;
  RenderResources renderResourcesSpy;

  DashboardWcdfDescriptor dashboardWcdfDescriptor;
  
  
  @Before
  public void setup() {
    dashboardWcdfDescriptor = mock( DashboardWcdfDescriptor.class );
    doReturn( true ).when( dashboardWcdfDescriptor ).isRequire();
    
    Dashboard dashboard = mock( Dashboard.class );
    when( dashboard.getWcdf() ).thenReturn( dashboardWcdfDescriptor );
    
    context = mock( CdfRunJsDashboardWriteContext.class );
    when( context.getDashboard() ).thenReturn( dashboard );
    
    doc = mock( JXPathContext.class );
    renderResources = new RenderResources( doc, context );
    renderResourcesSpy = spy( renderResources );
  }

  @Test
  public void testRenderResources() throws Exception {
    List<Pointer> objectList = new ArrayList<Pointer>( 1 );
    Pointer p = mock( Pointer.class );
    objectList.add( p );
    
    doReturn( objectList.iterator() ).when( renderResourcesSpy ).getResourcesRows();
    
    String type = "LayoutResourceFile",
      rowName = "myCssFile",
      rowKind = CdeConstants.CSS,
      rowPath = "/" + rowName,
      rowProcessedResource = "";
      
    JXPathContext jXPathContext = mock( JXPathContext.class );
    doReturn( type ).when( jXPathContext ).getValue( "type" );
    doReturn( jXPathContext ).when( renderResourcesSpy ).getRelativeContext( any( Pointer.class ) );
    doReturn( rowKind ).when( renderResourcesSpy ).getResourceType( jXPathContext );
    doReturn( rowPath ).when( renderResourcesSpy ).getResourcePath( jXPathContext );
    doReturn( rowProcessedResource ).when( renderResourcesSpy ).processResource( jXPathContext, "myAlias", 4 );
    
    ResourceMap resourceMap = renderResourcesSpy.renderResources( "myAlias" );
    verify( renderResourcesSpy, times( 1 ) ).getResourcesRows();
    verify( renderResourcesSpy, times( 1 ) ).getRelativeContext( any( Pointer.class ) );
    verify( renderResourcesSpy, times( 1 ) ).getResourcePath( jXPathContext );
    verify( renderResourcesSpy, times( 1 ) ).getResourceName( jXPathContext );
    verify( renderResourcesSpy, times( 1 ) ).getResourceType( jXPathContext );
    
    assertEquals( resourceMap.getCssResources().size(), 1 );
    
  }
  
  @Test
  public void testProcessResource() throws Exception {
    ResourceCodeRender resourceCodeRender = mock( ResourceCodeRender.class );
    doReturn( resourceCodeRender ).when( renderResourcesSpy ).getRender( any( JXPathContext.class ) );
    
    doReturn( CdeConstants.JAVASCRIPT ).when( renderResourcesSpy ).getResourceType( any( JXPathContext.class ) );
    doReturn( "" ).when( renderResourcesSpy ).getResourceCodeContent( any( JXPathContext.class ) );
    renderResourcesSpy.processResource( any( JXPathContext.class ), anyString(), anyInt() );
    verify( renderResourcesSpy, times( 1 ) ).getResourceType( any( JXPathContext.class ) );
    verify( renderResourcesSpy, times( 1 ) ).getResourceCodeContent( any( JXPathContext.class ) );

    doReturn( CdeConstants.CSS ).when( renderResourcesSpy ).getResourceType( any( JXPathContext.class ) );
    renderResourcesSpy.processResource( any( JXPathContext.class ), anyString(), anyInt() );
    verify( renderResourcesSpy, times( 2 ) ).getResourceType( any( JXPathContext.class ) );
    verify( renderResourcesSpy, times( 1 ) ).getResourceCodeContent( any( JXPathContext.class ) );

    doReturn( false ).when( dashboardWcdfDescriptor ).isRequire();
    renderResourcesSpy.processResource( any( JXPathContext.class ), anyString(), anyInt() );
    verify( renderResourcesSpy, times( 2 ) ).getResourceType( any( JXPathContext.class ) );

    ResourceFileRender resourceFileRender = mock( ResourceFileRender.class );
    doReturn( resourceFileRender ).when( renderResourcesSpy ).getRender( any( JXPathContext.class ) );
    doReturn( true ).when( dashboardWcdfDescriptor ).isRequire();

    doReturn( CdeConstants.JAVASCRIPT ).when( renderResourcesSpy ).getResourceType( any( JXPathContext.class ) );
    doReturn( "" ).when( renderResourcesSpy ).getResourceCodeContent( any( JXPathContext.class ) );
    renderResourcesSpy.processResource( any( JXPathContext.class ), anyString(), anyInt() );
    verify( renderResourcesSpy, times( 3 ) ).getResourceType( any( JXPathContext.class ) );
    verify( renderResourcesSpy, times( 1 ) ).getResourceCodeContent( any( JXPathContext.class ) );

    doReturn( CdeConstants.CSS ).when( renderResourcesSpy ).getResourceType( any( JXPathContext.class ) );
    renderResourcesSpy.processResource( any( JXPathContext.class ), anyString(), anyInt() );
    verify( renderResourcesSpy, times( 4 ) ).getResourceType( any( JXPathContext.class ) );
    verify( renderResourcesSpy, times( 1 ) ).getResourceCodeContent( any( JXPathContext.class ) );

    doReturn( false ).when( dashboardWcdfDescriptor ).isRequire();
    renderResourcesSpy.processResource( any( JXPathContext.class ), anyString(), anyInt() );
    verify( renderResourcesSpy, times( 4 ) ).getResourceType( any( JXPathContext.class ) );
  }
}
