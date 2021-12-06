/*!
 * Copyright 2002 - 2021 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.PropertyBinding;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.meta.PropertyTypeUsage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CdfRunJsPropertyBindingWriterTest {
  private CdfRunJsPropertyBindingWriterForTests propertyBindingWriter;

  @Before
  public void setUp() {
    propertyBindingWriter = new CdfRunJsPropertyBindingWriterForTests();
  }

  @Test
  public void testWriteString() throws ThingWriteException {
    makeWriteTest( CdfRunJsPropertyBindingWriterForTests.TYPE_STRING, "testString", "\"testString\"" );
    makeWriteTest( CdfRunJsPropertyBindingWriterForTests.TYPE_STRING,
        "<script> test </script>",
        "\"<script> test </\" + \"script>\"" );
  }

  @Test
  public void testWriteNumber() throws ThingWriteException {
    makeWriteTest( CdfRunJsPropertyBindingWriterForTests.TYPE_NUMBER, "42" );
  }

  @Test
  public void testWriteBoolean() throws ThingWriteException {
    makeWriteTest( CdfRunJsPropertyBindingWriterForTests.TYPE_BOOLEAN, "false" );
    makeWriteTest( CdfRunJsPropertyBindingWriterForTests.TYPE_BOOLEAN, "true" );
  }

  @Test
  public void testWriteLiteral() throws ThingWriteException {
    makeWriteTest( CdfRunJsPropertyBindingWriterForTests.TYPE_LITERAL, "literal" );
  }

  @Test
  public void testWriteArray() throws ThingWriteException {
    makeWriteTest( CdfRunJsPropertyBindingWriterForTests.TYPE_ARRAY, "[42,\"answer\"]" );
    makeWriteTest( CdfRunJsPropertyBindingWriterForTests.TYPE_ARRAY,
        "[42,\"<script>answer</script>\"]",
        "[42,\"<script>answer</\" + \"script>\"]" );
    // the writeArray function will also unwrap functions
    makeWriteTest( CdfRunJsPropertyBindingWriterForTests.TYPE_ARRAY,
        "\"function(answer){return 42;}\"",
        "function(answer){return 42;}" );
  }

  @Test
  public void testWriteFunction() throws ThingWriteException {
    String testFunction = "function(){return {answer: 42};}";
    makeWriteTest( CdfRunJsPropertyBindingWriterForTests.TYPE_FUNCTION, testFunction );
    makeWriteTest( CdfRunJsPropertyBindingWriterForTests.TYPE_FUNCTION, "\"" + testFunction + "\"" );
    makeWriteTest( CdfRunJsPropertyBindingWriterForTests.TYPE_FUNCTION, "42", wrapInFunction( "\"42\"" ) );
    makeWriteTest( CdfRunJsPropertyBindingWriterForTests.TYPE_FUNCTION, "answer", wrapInFunction( "\"answer\"" ) );
  }

  private String wrapInFunction( String value ) {
    return "function() { return " + value + "; }";
  }

  private void makeWriteTest( int type, String test ) throws ThingWriteException {
    makeWriteTest( type, test, test );
  }

  private void makeWriteTest( int type, String test, String expected ) throws ThingWriteException {
    StringBuilder out = new StringBuilder();
    propertyBindingWriter.setType( type );
    try {
      propertyBindingWriter.write( out, null, getPropertyBinding( test ) );
    } catch ( ValidationException e ) {
      fail( "ValidationException occurred" );
    }
    assertEquals( expected, out.toString() );
  }

  private PropertyBinding getPropertyBinding( String value ) throws ValidationException {
    PropertyBinding.Builder builder = getBuilder();
    builder.setValue( value );
    Component component = Mockito.mock( Component.class );
    MetaModel metaModel = Mockito.mock( MetaModel.class );
    return new PropertyBinding( builder, component, metaModel ) {
      @Override
      public PropertyTypeUsage getPropertyUsage() {
        return null;
      }

      @Override
      public String getAlias() {
        return null;
      }

      @Override
      public String getInputType() {
        return null;
      }

      @Override
      public PropertyType getProperty() {
        return null;
      }
    };
  }

  private PropertyBinding.Builder getBuilder() {
    return new PropertyBinding.Builder() {
      @Override
      public String getAlias() {
        return null;
      }

      @Override
      public PropertyBinding build( Component owner, MetaModel metaModel ) throws ValidationException {
        return null;
      }
    };
  }
}
