/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.reader.cdfdejs;

import org.apache.commons.jxpath.JXPathContext;

import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.IThingReader;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.inst.LayoutComponent;
import pt.webdetails.cdf.dd.model.inst.UnresolvedPropertyBinding;
import pt.webdetails.cdf.dd.model.meta.LayoutComponentType;

/**
 * @author dcleao
 */
public class CdfdeJsLayoutComponentReader implements IThingReader
{
  private final LayoutComponentType _layoutCompType;
  
  public CdfdeJsLayoutComponentReader(LayoutComponentType layoutCompType)
  {
    if(layoutCompType == null) { throw new IllegalArgumentException("layoutCompType"); }
    this._layoutCompType = layoutCompType;
  }
  
  public LayoutComponent.Builder read(IThingReadContext context, java.lang.Object source, String sourcePath)
          throws ThingReadException
  {
    LayoutComponent.Builder builder = new LayoutComponent.Builder();
    read(builder, context, (JXPathContext)source, sourcePath);
    return builder;
  }
  
  public void read(Thing.Builder builder, IThingReadContext context, java.lang.Object source, String sourcePath) 
      throws ThingReadException
  {
    read((LayoutComponent.Builder)builder, context, (JXPathContext)source, sourcePath);
  }

  public void read(LayoutComponent.Builder builder, IThingReadContext context, JXPathContext source, String sourcePath)
  {
    builder.setMeta(this._layoutCompType);
    
    // Add a name
    builder.addPropertyBinding(
              new UnresolvedPropertyBinding.Builder()
                .setAlias("name")
                .setValue("TODO"));
    
    builder.setLayoutXPContext(source);
  }
}
