/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta.reader.cdexml;

import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.meta.Resource;
import pt.webdetails.cdf.dd.model.meta.reader.cdexml.fs.XmlFsPluginThingReaderFactory;
import pt.webdetails.cdf.dd.util.Utils;

/**
 * @author dcleao
 */
public abstract class XmlComponentTypeReader
{

  public void read(
          ComponentType.Builder builder,
          XmlFsPluginThingReaderFactory factory, //just need property type here
          Element elem,
          String sourcePath)
          throws ThingReadException
  {
    //TODO: methods instead of comments for separation
    String compDir = FilenameUtils.getFullPath(sourcePath);
    
    // componentElem is <DesignerComponent>
    String name = elem.selectSingleNode("Header/IName") != null ? elem.selectSingleNode("Header/IName").getText() : null;
    
    builder
      .setName(name)
      .setLabel(Utils.getNodeText("Header/Name", elem))
      .setTooltip(Utils.getNodeText("Header/Description", elem))
      .setCategory(Utils.getNodeText("Header/Category", elem))
      .setCategoryLabel(Utils.getNodeText("Header/CatDescription", elem))
      .setSourcePath(sourcePath)
      .setVersion(Utils.getNodeText("Header/Version", elem));
    
    String visibleText = Utils.getNodeText("Header/Visible", elem);
    if(StringUtils.isNotEmpty(visibleText))
    {
      builder.setVisible("true".equalsIgnoreCase(visibleText));
    }
    
    @SuppressWarnings("unchecked")
    List<Element> legacyNamesElems = elem.selectNodes("Header/LegacyIName");
    for (Element legacyNameElem : legacyNamesElems)
    {
      String legacyName = legacyNameElem.getStringValue();
      if(StringUtils.isNotBlank(legacyName)) {
        builder.addLegacyName(legacyName);
      }
    }
    
    String cdeModelIgnoreText = Utils.getNodeText("Contents/Model/@ignore", elem);
    boolean cdeModelIgnore = cdeModelIgnoreText != null && cdeModelIgnoreText.toLowerCase().equals("true");

    builder.addAttribute("cdeModelIgnore", cdeModelIgnore ? "true" : "false");
    
    String cdeModelPrefix = Utils.getNodeText("Contents/Model/@prefix", elem);
    if(StringUtils.isNotEmpty(cdeModelPrefix))
    {
      builder.addAttribute("cdeModelPrefix", cdeModelPrefix);
    }
    
    String cdePalleteType = Utils.getNodeText("Header/Type", elem);
    if(StringUtils.isNotEmpty(cdeModelPrefix))
    {
      builder.addAttribute("cdePalleteType", cdePalleteType);
    }
    
    @SuppressWarnings("unchecked")
    List<Element> depElems = elem.selectNodes("Contents/Implementation/Dependencies/*");
    for (Element depElem : depElems)
    {
      builder.addResource(
        new Resource.Builder()
             .setType(Resource.Type.SCRIPT)
             .setApp(Utils.getNodeText("@app", depElem))
             .setName(Utils.getNodeText(".", depElem))
             .setVersion(Utils.getNodeText("@version", depElem))
             .setSource(Utils.joinPath(compDir, Utils.getNodeText("@src", depElem))));
    }

    @SuppressWarnings("unchecked")
    List<Element> styleElems = elem.selectNodes("Contents/Implementation/Styles/*");
    for (Element styleElem : styleElems)
    {
      builder.addResource(
        new Resource.Builder()
             .setType(Resource.Type.STYLE)
             .setApp(Utils.getNodeText("@app", styleElem))
             .setName(Utils.getNodeText(".", styleElem))
             .setVersion(Utils.getNodeText("@version", styleElem))
             .setSource(Utils.joinPath(compDir, Utils.getNodeText("@src", styleElem))));
    }

    @SuppressWarnings("unchecked")
    List<Element> rawElems = elem.selectNodes("Contents/Implementation/Raw/*");
    for (Element rawElem : rawElems)
    {
      builder.addResource(
        new Resource.Builder()
             .setType(Resource.Type.STYLE)
             .setApp(Utils.getNodeText("@app", rawElem))
             .setName(Utils.getNodeText("@name", rawElem))
             .setVersion(Utils.getNodeText("@version", rawElem))
             .setSource(Utils.getNodeText(".", rawElem)));
    }

    String srcPath = Utils.getNodeText("Contents/Implementation/Code/@src", elem);
    if(StringUtils.isNotEmpty(srcPath))
    {
      builder.setImplementationPath(Utils.joinPath(compDir, srcPath));
    }
    
    // -----------

    @SuppressWarnings("unchecked")
    List<Element> propElems = elem.selectNodes("Contents/Implementation/CustomProperties/*");
    for(Element propElem : propElems)
    {
      String className = Utils.getNodeText("Header/Override", propElem);
//      String propName = Utils.getNodeText("Header/Name", propElem);
      
      if(StringUtils.isEmpty(className)) { className = "PropertyType"; }
      
      XmlPropertyTypeReader propReader = factory.getPropertyTypeReader();

      PropertyType.Builder prop = propReader.read(propElem, sourcePath);
      builder.addProperty(prop);
    }
    
    // -----------
    
    // The "//" in the XPath is to catch properties inside Defintions
    List<Element> usedPropElems = Utils.selectNodes(elem, "Contents/Model//Property");
    for(Element usedPropElem : usedPropElems)
    {
      String definitionName = null;
      Element parentElem = usedPropElem.getParent();
      if(parentElem != null && parentElem.getName().equals("Definition"))
      {
        definitionName = Utils.getNodeText("@name", parentElem);
      }
      
      builder.useProperty(
    		  Utils.getNodeText("@name", usedPropElem), // alias
    		  Utils.getNodeText(".", usedPropElem),    // ref-name
         definitionName);
    }

    List<Element> attributeElems = Utils.selectNodes(elem, "Metadata/*");
    for (Element attributeElem : attributeElems)
    {
      builder.addAttribute(
    		  Utils.getNodeText("@name", attributeElem),
    		  Utils.getNodeText(".", attributeElem));
    }
  }

}
