/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs;

import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;

public abstract class CdfRunJsThingWriterBaseFactory {
  private static final String SIMPLE_PARAMETER = "parameter";
  private static final String OLAP_PARAMETER = "olapparameter";
  private static final String DATE_PARAMETER = "dateparameter";
  private static final String JS_EXPRESSION_PARAMETER = "javascriptparameter";

  public IThingWriter getParameterWriter( Thing t ) {
    ParameterComponent paramComp = (ParameterComponent) t;
    String typeName = paramComp.getMeta().getName().toLowerCase();

    IThingWriter parameterWriter;
    switch ( typeName ) {
      case SIMPLE_PARAMETER:
      case OLAP_PARAMETER:
        parameterWriter = getSimpleParameter();
        break;
      case DATE_PARAMETER:
        parameterWriter = getDateParameter();
        break;
      case JS_EXPRESSION_PARAMETER:
        parameterWriter = getJsExpressionParameter();
        break;
      default:
        parameterWriter = null;
    }

    return parameterWriter;
  }

  public abstract IThingWriter getSimpleParameter();

  public abstract IThingWriter getDateParameter();

  public abstract IThingWriter getJsExpressionParameter();
}
