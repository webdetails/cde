/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta.reader.datasources;

import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.reader.IThingReader;
import pt.webdetails.cdf.dd.model.core.reader.IThingReaderFactory;

/**
 * @author dcleao
 */
public class DataSourcesObjectReaderFactory implements IThingReaderFactory
{
  public IThingReader getReader(String kind, String className, String name)
          throws UnsupportedThingException
  {
    if(KnownThingKind.MetaModel.equals(kind))
    {
      return new DataSourcesModelReader();
    }
    
    throw new UnsupportedThingException(kind, className);
  }
}
