/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta.reader.cda;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.meta.DataSourceComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.MetaObject;
import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.IThingReader;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;

/**
 * Loads XML model files,
 * component types and property types,
 * from the file system,
 * of a Pentaho CDE plugin instalation.
 *
 * @author dcleao
 */
public final class CdaModelReader implements IThingReader
{
  protected static final Log _logger = LogFactory.getLog(CdaModelReader.class);

  public void read(
          Thing.Builder builder,
          IThingReadContext context,
          java.lang.Object source,
          String sourcePath)
          throws ThingReadException
  {
    this.read((MetaModel.Builder) builder, context, (JSON)source, sourcePath);
  }

  // idem
  public MetaObject.Builder read(
          IThingReadContext context,
          java.lang.Object source,
          String sourcePath)
          throws ThingReadException
  {
    MetaModel.Builder builder = new MetaModel.Builder();

    this.read(builder, context, (JSON)source, sourcePath);

    return builder;
  }
  
  public void read(MetaModel.Builder model, IThingReadContext context, JSON cdaDefs, String sourcePath) throws ThingReadException
  {
    assert model != null;

    _logger.info("Loading CDA Plugin data source components");

    final JXPathContext doc = JXPathContext.newContext(cdaDefs);

    @SuppressWarnings("unchecked")
    Iterator<Pointer> pointers = doc.iteratePointers("*");
    while (pointers.hasNext())
    {
      Pointer pointer = pointers.next();
      this.readCdaDataSourceComponent(model, pointer, sourcePath);
    }
  }

  private void readCdaDataSourceComponent(MetaModel.Builder model, Pointer pointer, String sourcePath)
  {
    DataSourceComponentType.Builder builder = new DataSourceComponentType.Builder();

    JSONObject def = (JSONObject) pointer.getNode();
    JXPathContext jctx = JXPathContext.newContext(def);

    String label = (String)jctx.getValue("metadata/name");
    
    // This specific Data Source has special treatment below
    boolean isKettleOverX = "kettle over kettleTransFromFile".equalsIgnoreCase(label);
    
    _logger.debug(String.format("\t%s", label));

    String connType = (String) jctx.getValue("metadata/conntype");
    connType = connType != null ? connType : "";

    builder
      .setName(pointer.asPath().replaceAll(".*name='(.*?)'.*", "$1"))
      .setLabel(label)
      .setTooltip(label)
      .setCategory((String) jctx.getValue("metadata/group"))
      .setCategoryLabel((String) jctx.getValue("metadata/groupdesc"))
      .setSourcePath(sourcePath)
      .addAttribute("", "CDA") // meta: "CDA"
      .addAttribute("conntype", connType)
      .addAttribute("datype", (String)jctx.getValue("metadata/datype"));

    for(String cdaPropName : this.getPropertyNames(def))
    {
      if (cdaPropName.equals("id") || cdaPropName.equals("connection")) {
        continue;
      } else if (cdaPropName.equals("columns")) {
        builder.useProperty(null, "cdacolumns");
        builder.useProperty(null, "cdacalculatedcolumns");
      } else if (cdaPropName.equals("output")) {
        builder.useProperty(null, "output");
        builder.useProperty(null, "outputMode");
      } else if (cdaPropName.equals("left")) {
        builder.useProperty(null, "left");
        builder.useProperty(null, "leftkeys");
      } else if (cdaPropName.equals("right")) {
        builder.useProperty(null, "right");
        builder.useProperty(null, "rightkeys");
      } else if(isKettleOverX && cdaPropName.equalsIgnoreCase("query")) {
        builder.useProperty(cdaPropName, "kettleQuery");
      } else {
        builder.useProperty(null, cdaPropName);
      }
      
    }

    model.addComponent(builder);
  }

  private List<String> getPropertyNames(JSONObject def)
  {
    ArrayList<String> props = new ArrayList<String>();

    JXPathContext context = JXPathContext.newContext(def);

    JSONObject connection = (JSONObject)context.getValue("definition/connection", JSONObject.class);
    if (connection != null) {
      @SuppressWarnings("unchecked")
      Set<String> keys = connection.keySet();
      props.addAll(keys);
    }

    JSONObject dataaccess = (JSONObject)context.getValue("definition/dataaccess", JSONObject.class);
    if (dataaccess != null) {
      @SuppressWarnings("unchecked") 
      Set<String> keys = dataaccess.keySet();
      props.addAll(keys);
    }
    
    return props;
  }
}