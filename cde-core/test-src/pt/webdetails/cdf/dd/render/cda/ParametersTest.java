/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd.render.cda;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class ParametersTest {
    private Parameters parametersRenderer;

    private Element parameter;
    private Element parameters;

    private Element dataAccess;


    @Before
    public void setUp() throws Exception {
        parametersRenderer = new Parameters();

        Document doc = mock( Document.class );

        Element name = mock( Element.class );
        Element defaultValue = mock( Element.class );
        Element type = mock( Element.class );
        Element access = mock( Element.class );
        Element pattern = mock( Element.class );

        dataAccess = mock( Element.class );
        parameter = mock( Element.class );
        parameters = mock( Element.class );

        doReturn( parameter ).when( doc ).createElement( "Parameter" );
        doReturn( parameters ).when( doc ).createElement( "Parameters" );
        doReturn( doc ).when( dataAccess ).getOwnerDocument();
        doReturn( name ).when( doc ).createElement( "name" );
        doReturn( defaultValue ).when( doc ).createElement( "default" );
        doReturn( type ).when( doc ).createElement( "type" );
        doReturn( access ).when( doc ).createElement( "access" );
        doReturn( pattern ).when( doc ).createElement( "pattern" );
    }

    @Test
    public void testRenderIntoPublicAccess() throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put( "value", "[[\"paramName\",\"paramValue\",\"paramType\",\"\",\"\"]]" );

        parametersRenderer.setDefinition( map );
        parametersRenderer.renderInto( dataAccess );

        verify( dataAccess, times( 1 ) ).appendChild( parameters );

        verify( parameter, times( 1 ) ).setAttribute( "name", "paramName" );
        verify( parameter, times( 1 ) ).setAttribute( "default", "paramValue" );
        verify( parameter, times( 1 ) ).setAttribute( "type", "paramType" );
        verify( parameter, times( 0 ) ).setAttribute( "access", "" );
        verify( parameter, times( 0 ) ).setAttribute( "pattern", "" );
    }

    @Test
    public void testRenderIntoPrivateAccess() throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put( "value", "[[\"paramName\",\"paramValue\",\"paramType\",\"private\",\"\"]]" );

        parametersRenderer.setDefinition( map );
        parametersRenderer.renderInto( dataAccess );

        verify( dataAccess, times( 1 ) ).appendChild( parameters );

        verify( parameter, times( 1 ) ).setAttribute( "name", "paramName" );
        verify( parameter, times( 1 ) ).setAttribute( "default", "paramValue" );
        verify( parameter, times( 1 ) ).setAttribute( "type", "paramType" );
        verify( parameter, times( 1 ) ).setAttribute( "access", "private" );
        verify( parameter, times( 0 ) ).setAttribute( "pattern", "" );
    }

    @Test
    public void testRenderIntoWithPattern() throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put( "value", "[[\"paramName\",\"paramValue\",\"paramType\",\"\",\"paramPattern\"]]" );

        parametersRenderer.setDefinition( map );
        parametersRenderer.renderInto( dataAccess );

        verify( dataAccess, times( 1 ) ).appendChild( parameters );

        verify( parameter, times( 1 ) ).setAttribute( "name", "paramName" );
        verify( parameter, times( 1 ) ).setAttribute( "default", "paramValue" );
        verify( parameter, times( 1 ) ).setAttribute( "type", "paramType" );
        verify( parameter, times( 0 ) ).setAttribute( "access", "" );
        verify( parameter, times( 1 ) ).setAttribute( "pattern", "paramPattern" );
    }
}
