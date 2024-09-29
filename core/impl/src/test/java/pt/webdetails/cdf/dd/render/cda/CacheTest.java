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


package pt.webdetails.cdf.dd.render.cda;

import org.apache.commons.jxpath.JXPathContext;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CacheTest {

  private JXPathContext context;
  private Cache cacheRenderer;
  private Element dataAccess;
  private Element cache;
  private Element key;
  private Document doc;

  @Before
  public void setUp() throws Exception {
    context = mock( JXPathContext.class );
    cacheRenderer = new Cache( context );

    dataAccess = mock( Element.class );
    cache = mock( Element.class );
    key = mock( Element.class );
    doc = mock( Document.class );
    HashMap<String, Object> map = new HashMap<>();
    map.put( "value", "true" );
    cacheRenderer.setDefinition( map );

    doReturn( doc ).when( dataAccess ).getOwnerDocument();
    doReturn( cache ).when( doc ).createElement( "Cache" );
    doReturn( key ).when( doc ).createElement( "Key" );
  }

  @Test
  public void testRenderCacheTag() throws JSONException {
    doReturn( "500" ).when( context ).getValue( "properties/.[name='cacheDuration']/value" );
    doReturn( "[]" ).when( context ).getValue( "properties/.[name='cacheKeys']/value" );

    cacheRenderer.renderInto( dataAccess );

    verify( cache, times( 1 ) ).setAttribute( "enabled", "true" );
    verify( cache, times( 1 ) ).setAttribute( "duration", "500" );
    verify( dataAccess, times( 1 ) ).appendChild( cache );
  }

  @Test
  public void testRenderCacheTagWithKeys() throws JSONException {
    doReturn( "3600" ).when( context ).getValue( "properties/.[name='cacheDuration']/value" );
    doReturn( "[[\"Hello\",\"World\",\"Foo\"]]" ).when( context ).getValue( "properties/.[name='cacheKeys']/value" );

    cacheRenderer.renderInto( dataAccess );

    verify( cache, times( 1 ) ).setAttribute( "enabled", "true" );
    verify( cache, times( 1 ) ).setAttribute( "duration", "3600" );
    verify( dataAccess, times( 1 ) ).appendChild( cache );

    verify( key, times( 1 ) ).setAttribute( "name", "Hello" );
    verify( key, times( 1 ) ).setAttribute( "value", "World" );
    verify( key, times( 1 ) ).setAttribute( "default", "Foo" );
    verify( cache, times( 1 ) ).appendChild( key );
  }
}
