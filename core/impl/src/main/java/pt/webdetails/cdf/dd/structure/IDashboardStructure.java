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



package pt.webdetails.cdf.dd.structure;

import java.util.HashMap;

//TODO: ever used?
public interface IDashboardStructure {

  public abstract HashMap<String, String> save( HashMap<String, Object> parameters ) throws Exception;

  public abstract Object load( String filePath ) throws Exception;

  public abstract void delete( HashMap<String, Object> parameters ) throws Exception;

}
