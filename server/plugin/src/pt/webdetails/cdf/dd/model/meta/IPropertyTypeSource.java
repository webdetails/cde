/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta;

/**
 * Interface that supports the building phase of component types.
 *
 * @author dcleao
 */
public interface IPropertyTypeSource
{
  PropertyType getProperty(String name);
}
