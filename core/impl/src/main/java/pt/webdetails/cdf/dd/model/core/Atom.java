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


package pt.webdetails.cdf.dd.model.core;

/**
 * Atoms have no explicit structure and no identity.
 */
public abstract class Atom extends Thing {
  public abstract String getKind();

  public final String getId() {
    return null;
  }

  public abstract static class Builder extends Thing.Builder { }
}
