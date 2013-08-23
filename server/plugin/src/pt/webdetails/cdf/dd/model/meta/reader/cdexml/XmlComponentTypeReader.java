/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta.reader.cdexml;

import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.IThingReader;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.meta.Resource;
import pt.webdetails.cdf.dd.util.Utils;

/**
 * @author dcleao
 */
public abstract class XmlComponentTypeReader implements IThingReader
{
   public abstract ComponentType.Builder read(IThingReadContext context, java.lang.Object source, String sourcePath)
            throws ThingReadException;
//  {
//    ComponentType.Builder builder = new ComponentType.Builder();
//    this.read(builder, context, (Element)source, sourcePath);
//    return builder;
//  }
  
  public void read(
          Thing.Builder builder,
          IThingReadContext context,
          java.lang.Object source,
          String sourcePath)
          throws ThingReadException
  {
    this.read((ComponentType.Builder)builder, (XmlPluginModelReadContext)context, (Element)source, sourcePath);
  }

  public void read(
          ComponentType.Builder builder,
          XmlPluginModelReadContext context,
          Element componentElem,
          String sourcePath)
          throws ThingReadException
  {
    String compDir = FilenameUtils.getFullPath(sourcePath);
    
    // componentElem is <DesignerComponent>
    String name = XmlDom4JHelper.getNodeText("Header/IName", componentElem);
    
    builder
      .setName(name)
      .setLabel(XmlDom4JHelper.getNodeText("Header/Name", componentElem))
      .setTooltip(XmlDom4JHelper.getNodeText("Header/Description", componentElem))
      .setCategory(XmlDom4JHelper.getNodeText("Header/Category", componentElem))
      .setCategoryLabel(XmlDom4JHelper.getNodeText("Header/CatDescription", componentElem))
      .setSourcePath(sourcePath)
      .setVersion(XmlDom4JHelper.getNodeText("Header/Version", componentElem));
    
    String visibleText = XmlDom4JHelper.getNodeText("Header/Visible", componentElem);
    if(StringUtils.isNotEmpty(visibleText))
    {
      builder.setVisible("true".equalsIgnoreCase(visibleText));
    }
    
    @SuppressWarnings("unchecked")
    List<Element> legacyNamesElems = componentElem.selectNodes("Header/LegacyIName");
    for (Element legacyNameElem : legacyNamesElems)
    {
      String legacyName = legacyNameElem.getStringValue();
      if(StringUtils.isNotBlank(legacyName)) {
        builder.addLegacyName(legacyName);
      }
    }
    
    String cdeModelIgnoreText = XmlDom4JHelper.getNodeText("Contents/Model/@ignore", componentElem);
    boolean cdeModelIgnore = cdeModelIgnoreText != null &&
                        cdeModelIgnoreText.toLowerCase().equals("true");

    builder.addAttribute("cdeModelIgnore", cdeModelIgnore ? "true" : "false");
    
    String cdeModelPrefix = XmlDom4JHelper.getNodeText("Contents/Model/@prefix", componentElem);
    if(StringUtils.isNotEmpty(cdeModelPrefix))
    {
      builder.addAttribute("cdeModelPrefix", cdeModelPrefix);
    }
    
    String cdePalleteType = XmlDom4JHelper.getNodeText("Header/Type", componentElem);
    if(StringUtils.isNotEmpty(cdeModelPrefix))
    {
      builder.addAttribute("cdePalleteType", cdePalleteType);
    }
    
    @SuppressWarnings("unchecked")
    List<Element> depElems = componentElem.selectNodes("Contents/Implementation/Dependencies/*");
    for (Element depElem : depElems)
    {
      builder.addResource(
        new Resource.Builder()
             .setType(Resource.Type.SCRIPT)
             .setApp(XmlDom4JHelper.getNodeText("@app", depElem))
             .setName(XmlDom4JHelper.getNodeText(".", depElem))
             .setVersion(XmlDom4JHelper.getNodeText("@version", depElem))
             .setSource(Utils.joinPath(compDir, XmlDom4JHelper.getNodeText("@src", depElem))));
    }

    @SuppressWarnings("unchecked")
    List<Element> styleElems = componentElem.selectNodes("Contents/Implementation/Styles/*");
    for (Element styleElem : styleElems)
    {
      builder.addResource(
        new Resource.Builder()
             .setType(Resource.Type.STYLE)
             .setApp(XmlDom4JHelper.getNodeText("@app", styleElem))
             .setName(XmlDom4JHelper.getNodeText(".", styleElem))
             .setVersion(XmlDom4JHelper.getNodeText("@version", styleElem))
             .setSource(Utils.joinPath(compDir, XmlDom4JHelper.getNodeText("@src", styleElem))));
    }

    @SuppressWarnings("unchecked")
    List<Element> rawElems = componentElem.selectNodes("Contents/Implementation/Raw/*");
    for (Element rawElem : rawElems)
    {
      builder.addResource(
        new Resource.Builder()
             .setType(Resource.Type.STYLE)
             .setApp(XmlDom4JHelper.getNodeText("@app", rawElem))
             .setName(XmlDom4JHelper.getNodeText("@name", rawElem))
             .setVersion(XmlDom4JHelper.getNodeText("@version", rawElem))
             .setSource(XmlDom4JHelper.getNodeText(".", rawElem)));
    }

    String srcPath = XmlDom4JHelper.getNodeText("Contents/Implementation/Code/@src", componentElem);
    if(StringUtils.isNotEmpty(srcPath))
    {
      builder.setImplementationPath(Utils.joinPath(compDir, srcPath));
    }
    
    // -----------

    @SuppressWarnings("unchecked")
    List<Element> propElems = componentElem.selectNodes("Contents/Implementation/CustomProperties/*");
    for(Element propElem : propElems)
    {
      String className = XmlDom4JHelper.getNodeText("Header/Override", propElem);
      String propName = XmlDom4JHelper.getNodeText("Header/Name", propElem);
      
      if(StringUtils.isEmpty(className)) { className = "PropertyType"; }
      
      IThingReader propReader;
      try
      {
        propReader = context.getFactory().getReader(KnownThingKind.PropertyType, className, propName);
      }
      catch(UnsupportedThingException ex)
      {
        throw new ThingReadException(ex);
      }

      PropertyType.Builder prop = (PropertyType.Builder)propReader.read(
              context,
              propElem,
              sourcePath);

      builder.addProperty(prop);
    }
    
    // -----------
    
    // The "//" in the XPath is to catch properties inside Defintions
    List<Element> usedPropElems = componentElem.selectNodes("Contents/Model//Property");
    for(Element usedPropElem : usedPropElems)
    {
      String definitionName = null;
      Element parentElem = usedPropElem.getParent();
      if(parentElem != null && parentElem.getName().equals("Definition"))
      {
        definitionName = XmlDom4JHelper.getNodeText("@name", parentElem, null);
      }
      
      builder.useProperty(
         XmlDom4JHelper.getNodeText("@name", usedPropElem), // alias
         XmlDom4JHelper.getNodeText(".", usedPropElem),    // ref-name
         definitionName);
    }

    List<Element> attributeElems = componentElem.selectNodes("Metadata/*");
    for (Element attributeElem : attributeElems)
    {
      builder.addAttribute(
          XmlDom4JHelper.getNodeText("@name", attributeElem),
          XmlDom4JHelper.getNodeText(".", attributeElem));
    }
  }
}
