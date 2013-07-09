/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core;

/**
 * Atoms have no explicit structure and no identity.
 * 
 * @author dcleao
 */
public abstract class Atom extends Thing
{
  public abstract String getKind();
  
  public final String getId()
  {
    return null;
  }
  
  public static abstract class Builder extends Thing.Builder
  {
  }
}
