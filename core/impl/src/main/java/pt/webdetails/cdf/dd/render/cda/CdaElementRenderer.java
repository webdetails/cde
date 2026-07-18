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



package pt.webdetails.cdf.dd.render.cda;

import org.json.JSONException;
import org.w3c.dom.Element;

import java.util.Map;


public interface CdaElementRenderer {

  public void renderInto( Element dataAccess ) throws JSONException;

  public void setDefinition( Map<String, Object> definition );
}
