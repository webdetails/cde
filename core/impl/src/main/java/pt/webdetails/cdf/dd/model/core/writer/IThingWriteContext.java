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


package pt.webdetails.cdf.dd.model.core.writer;

/**
 * Allows passing context information during the writing process.
 */
public interface IThingWriteContext {
  boolean getBreakOnError();

  IThingWriterFactory getFactory();
}
