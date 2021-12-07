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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.PROPER_EXPRESSION_CONTEXT;

public class CdfRunJsExpressionParameterComponentWriterTest {

  private CdfRunJsExpressionParameterComponentWriter expressionParameterComponentWriter;
  protected static final String NEWLINE = System.getProperty( "line.separator" );

  @Before
  public void setUp() throws Exception {
    expressionParameterComponentWriter = new CdfRunJsExpressionParameterComponentWriter();
  }

  @After
  public void tearDown() throws Exception {
    expressionParameterComponentWriter = null;
  }

  @Test
  public void testBindProperContext() {
    String functionToBind = "function() {}";
    String returnValue = "_.bind(" + functionToBind + ", " + PROPER_EXPRESSION_CONTEXT + ")";
    assertEquals(
        "Function is correctly bound to a proper context",
        returnValue,
        bind( functionToBind )
    );
  }

  @Test
  public void testExpressionParameterComponentWriterSimpleValue() {
    String customParamValue = "value;       ";
    String returnValue = expressionParameterComponentWriter.sanitizeExpression( customParamValue );
    String expectedReturnValue = bind( "function() { return value" + NEWLINE + "}" ) + "()";
    assertEquals( returnValue, expectedReturnValue );
  }

  @Test
  public void testExpressionParameterComponentWriterMultipleLineValue() {
    String customParamValue = "value;" + NEWLINE + "//comment";
    String returnValue = expressionParameterComponentWriter.sanitizeExpression( customParamValue );
    String expectedReturnValue = bind( "function() { return value;" + NEWLINE + "//comment" + NEWLINE + "}" ) + "()";
    assertEquals( returnValue, expectedReturnValue );
  }

  @Test
  public void testExpressionParameterComponentWriterFunction() {
    String customParamValue = "function() { return 'value'; }";
    String returnValue = expressionParameterComponentWriter.sanitizeExpression( customParamValue );
    String expectedReturnValue = bind( "function() { return 'value'; }" );
    assertEquals( returnValue, expectedReturnValue );
  }

  @Test
  public void testExpressionParameterComponentWriterMultipleLineFunction() {
    String customParamValue = "function() {" + NEWLINE + "return 'value';" + NEWLINE + "}";
    String returnValue = expressionParameterComponentWriter.sanitizeExpression( customParamValue );
    String expectedReturnValue = bind( "function() {" + NEWLINE + "return 'value';" + NEWLINE + "}" );
    assertEquals( returnValue, expectedReturnValue );
  }

  private String bind( String toBind ) {
    return CdfRunJsExpressionParameterComponentWriter.bindProperContext( toBind );
  }

}
