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

package pt.webdetails.cdf.dd.model.core.writer;

import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;

public interface IThingWriterFactory {

  String SIMPLE_PARAMETER = "parameter";
  String OLAP_PARAMETER = "olapparameter";
  String DATE_PARAMETER = "dateparameter";
  String JS_EXPRESSION_PARAMETER = "javascriptparameter";

  /**
   * Obtains a thing writer for a given thing,
   * and a pre-specified output format.
   */
  IThingWriter getWriter( Thing t ) throws UnsupportedThingException;

}
