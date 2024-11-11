/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/
package pt.webdetails.cdf.dd.render.layout;

import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cdf.dd.render.ResourceMap;

import java.util.List;

import static junit.framework.Assert.assertEquals;

public class ResourceMapTest {
  ResourceMap resourceMap;

  @Before
  public void setUp() throws Exception {
    resourceMap = new ResourceMap();
  }
  
  @Test 
  public void testConstructor() {
    assertEquals( resourceMap.getCssResources().size(), 0);
    assertEquals( resourceMap.getJavascriptResources().size(), 0);
  }
  
  @Test
  public void testAdd() {
    resourceMap.add( ResourceMap.ResourceKind.CSS, ResourceMap.ResourceType.CODE, "myCssCode", "/", "processed" );
    resourceMap.add( ResourceMap.ResourceKind.CSS, ResourceMap.ResourceType.FILE, "myCssFile", "/", "processed" );
    resourceMap.add( ResourceMap.ResourceKind.JAVASCRIPT, ResourceMap.ResourceType.CODE, "myJavascriptCode", "/", "processed" );
    resourceMap.add( ResourceMap.ResourceKind.JAVASCRIPT, ResourceMap.ResourceType.FILE, "myJavascriptFile", "/", "processed" );
    
    assertEquals( resourceMap.getCssResources().size(), 2 );
    assertEquals( resourceMap.getJavascriptResources().size(), 2 );
  }
  
  @Test
  public void testGetsAndSets() {
    resourceMap.add( ResourceMap.ResourceKind.CSS, ResourceMap.ResourceType.CODE, "myCssCode", "/", "processed" );
    
    ResourceMap.Resource cssResource = resourceMap.getCssResources().get( 0 );
    assertEquals( cssResource.getResourceType(), ResourceMap.ResourceType.CODE );
    assertEquals( cssResource.getProcessedResource(), "processed" );
    assertEquals( cssResource.getResourceName(), "myCssCode" );
    assertEquals( cssResource.getResourcePath(), "/" );

    cssResource.setResourceType( ResourceMap.ResourceType.FILE );
    cssResource.setResourceName( "newName" );
    cssResource.setResourcePath( "newPath" );
    cssResource.setProcessedResource( "newProcessed" );

    assertEquals( cssResource.getResourceType(), ResourceMap.ResourceType.FILE );
    assertEquals( cssResource.getProcessedResource(), "newProcessed" );
    assertEquals( cssResource.getResourceName(), "newName" );
    assertEquals( cssResource.getResourcePath(), "newPath" );

    resourceMap.add( ResourceMap.ResourceKind.JAVASCRIPT, ResourceMap.ResourceType.CODE, "myJsCode", "/", "processed" );

    List<ResourceMap.Resource> jsResources = resourceMap.getJavascriptResources();
    assertEquals( jsResources.get( 0 ).getResourceType(), ResourceMap.ResourceType.CODE );
    assertEquals( jsResources.get( 0 ).getProcessedResource(), "processed" );
    assertEquals( jsResources.get( 0 ).getResourceName(), "myJsCode" );
    assertEquals( jsResources.get( 0 ).getResourcePath(), "/" );
  }

  

}
