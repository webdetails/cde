/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core;

/**
 * The most abstract concept.
 * 
 * @author dcleao
 */
public abstract class Thing
{
  public abstract String getKind();
  public abstract String getId();
  
  public static abstract class Builder
  {
  }
}
