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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.legacy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CdfRunJsExpressionParameterComponentWriterTest {

  protected static final String NEWLINE = System.getProperty( "line.separator" );

  @Test
  public void testExpressionParameterComponentWriterSimpleValue() {
    String customParamValue = "value;       ";
    String returnValue = CdfRunJsExpressionParameterComponentWriter.sanitizeExpression( customParamValue );
    String expectedReturnValue = "function() { return value" + NEWLINE + "}()";
    assertEquals( expectedReturnValue, returnValue );
  }

  @Test
  public void testExpressionParameterComponentWriterMultipleLineValue() {
    String customParamValue = "value;" + NEWLINE + "//comment";
    String returnValue = CdfRunJsExpressionParameterComponentWriter.sanitizeExpression( customParamValue );
    String expectedReturnValue = "function() { return value;" + NEWLINE + "//comment" + NEWLINE + "}()";
    assertEquals( expectedReturnValue, returnValue );
  }

  @Test
  public void testExpressionParameterComponentWriterFunction() {
    String customParamValue = "function() { return 'value'; }";
    String returnValue = CdfRunJsExpressionParameterComponentWriter.sanitizeExpression( customParamValue );
    String expectedReturnValue = "function() { return 'value'; }";
    assertEquals( expectedReturnValue, returnValue );
  }

  @Test
  public void testExpressionParameterComponentWriterMultipleLineFunction() {
    String customParamValue = "function() {" + NEWLINE + "return 'value';" + NEWLINE + "}";
    String returnValue = CdfRunJsExpressionParameterComponentWriter.sanitizeExpression( customParamValue );
    String expectedReturnValue = "function() {" + NEWLINE + "return 'value';" + NEWLINE + "}";
    assertEquals( expectedReturnValue, returnValue );
  }
}
