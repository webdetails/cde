/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package pt.webdetails.cdf.dd.model.core.reader;

/**
 * Allows passing context information during the reading process.
 * <br><br>A factory holder that can be cast into classes containing other methods.
 */
public interface IThingReadContext {
  IThingReaderFactory getFactory();
}
