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
package pt.webdetails.cdf.dd.render;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.render.layout.ResourceCodeRender;
import pt.webdetails.cdf.dd.render.layout.ResourceFileRender;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
    doReturn( resourceCodeRender ).when( renderResourcesSpy ).getRender( Mockito.<JXPathContext>any() );
    
    doReturn( CdeConstants.JAVASCRIPT ).when( renderResourcesSpy ).getResourceType( Mockito.<JXPathContext>any() );
    doReturn( "" ).when( renderResourcesSpy ).getResourceCodeContent( Mockito.<JXPathContext>any() );
    renderResourcesSpy.processResource( Mockito.<JXPathContext>any(), any(), anyInt() );
    verify( renderResourcesSpy, times( 1 ) ).getResourceType( Mockito.<JXPathContext>any() );
    verify( renderResourcesSpy, times( 1 ) ).getResourceCodeContent( Mockito.<JXPathContext>any() );

    doReturn( CdeConstants.CSS ).when( renderResourcesSpy ).getResourceType( Mockito.<JXPathContext>any() );
    renderResourcesSpy.processResource( Mockito.<JXPathContext>any(), any(), anyInt() );
    verify( renderResourcesSpy, times( 2 ) ).getResourceType( Mockito.<JXPathContext>any() );
    verify( renderResourcesSpy, times( 1 ) ).getResourceCodeContent( Mockito.<JXPathContext>any() );

    doReturn( false ).when( dashboardWcdfDescriptor ).isRequire();
    renderResourcesSpy.processResource( Mockito.<JXPathContext>any(), any(), anyInt() );
    verify( renderResourcesSpy, times( 2 ) ).getResourceType( Mockito.<JXPathContext>any() );

    ResourceFileRender resourceFileRender = mock( ResourceFileRender.class );
    doReturn( resourceFileRender ).when( renderResourcesSpy ).getRender( Mockito.<JXPathContext>any() );
    doReturn( true ).when( dashboardWcdfDescriptor ).isRequire();

    doReturn( CdeConstants.JAVASCRIPT ).when( renderResourcesSpy ).getResourceType( Mockito.<JXPathContext>any() );
    doReturn( "" ).when( renderResourcesSpy ).getResourceCodeContent( Mockito.<JXPathContext>any() );
    renderResourcesSpy.processResource( Mockito.<JXPathContext>any(), any(), anyInt() );
    verify( renderResourcesSpy, times( 3 ) ).getResourceType( Mockito.<JXPathContext>any() );
    verify( renderResourcesSpy, times( 1 ) ).getResourceCodeContent( Mockito.<JXPathContext>any() );

    doReturn( CdeConstants.CSS ).when( renderResourcesSpy ).getResourceType( Mockito.<JXPathContext>any() );
    renderResourcesSpy.processResource( Mockito.<JXPathContext>any(), any(), anyInt() );
    verify( renderResourcesSpy, times( 4 ) ).getResourceType( Mockito.<JXPathContext>any() );
    verify( renderResourcesSpy, times( 1 ) ).getResourceCodeContent( Mockito.<JXPathContext>any() );

    doReturn( false ).when( dashboardWcdfDescriptor ).isRequire();
    renderResourcesSpy.processResource( Mockito.<JXPathContext>any(), any(), anyInt() );
    verify( renderResourcesSpy, times( 4 ) ).getResourceType( Mockito.<JXPathContext>any() );
  }
}
