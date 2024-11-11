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


package pt.webdetails.cdf.dd.model.meta.reader.cdexml;

import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.meta.CustomComponentType;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.meta.Resource;
import pt.webdetails.cdf.dd.model.meta.reader.cdexml.fs.XmlFsPluginThingReaderFactory;
import pt.webdetails.cdf.dd.util.Utils;

public abstract class XmlComponentTypeReader {
  private static final Log logger = LogFactory.getLog( XmlComponentTypeReader.class );

  public void read(
      ComponentType.Builder builder,
      XmlFsPluginThingReaderFactory factory, //just need property type here
      Element elem,
      String sourcePath )
    throws ThingReadException {
    //TODO: methods instead of comments for separation
    String compDir = FilenameUtils.getFullPath( sourcePath );

    String componentName = readBaseProperties( builder, elem, sourcePath );

    // model stuff and header type, better names welcome here
    readModel( builder, elem );

    readResourceDependencies( builder, elem, compDir, componentName );

    readComponentSupportType( builder, elem );

    String srcPath = Utils.getNodeText( "Contents/Implementation/Code/@src", elem );
    if ( StringUtils.isNotEmpty( srcPath ) ) {
      builder.setImplementationPath( Utils.joinPath( compDir, srcPath ) );
    }

    readCustomProperties( builder, factory, elem, sourcePath );

    readModelProperties( builder, elem );

    List<Element> attributeElems = Utils.selectElements( elem, "Metadata/*" );
    for ( Element attributeElem : attributeElems ) {
      builder.addAttribute(
          Utils.getNodeText( "@name", attributeElem ),
          Utils.getNodeText( ".", attributeElem ) );
    }
  }

  protected void readComponentSupportType( ComponentType.Builder builder, Element elem ) {
    // When loading custom components we need to be able to define if they
    // support legacy dashboards or/and new AMD ones.
    // Any other component type is supported in both dashboard versions.
    if ( builder instanceof CustomComponentType.Builder ) {
      boolean supportsLegacy = Boolean.valueOf(
        Utils.getNodeText( "Contents/Implementation/@supportsLegacy", elem ) ).booleanValue();
      boolean supportsAMD = Boolean.valueOf(
        Utils.getNodeText( "Contents/Implementation/@supportsAMD", elem ) ).booleanValue();
      // At least one value must be true
      if ( supportsAMD ) {
        builder.setSupportsAMD( true );
        if ( supportsLegacy ) {
          builder.setSupportsLegacy( true );
        } else {
          builder.setSupportsLegacy( false );
        }
      } else {
        // set default values, assume it's a legacy dashboard
        builder.setSupportsAMD( false );
        builder.setSupportsLegacy( true );
      }
    } else {
      builder.setSupportsLegacy( true );
      builder.setSupportsAMD( true );
    }
  }

  private void readModel( ComponentType.Builder builder, Element elem ) {
    String cdeModelIgnoreText = Utils.getNodeText( "Contents/Model/@ignore", elem );
    boolean cdeModelIgnore = cdeModelIgnoreText != null && cdeModelIgnoreText.toLowerCase().equals( "true" );

    builder.addAttribute( "cdeModelIgnore", cdeModelIgnore ? "true" : "false" );

    String cdeModelPrefix = Utils.getNodeText( "Contents/Model/@prefix", elem );
    if ( StringUtils.isNotEmpty( cdeModelPrefix ) ) {
      builder.addAttribute( "cdeModelPrefix", cdeModelPrefix );
    }

    String cdePalleteType = Utils.getNodeText( "Header/Type", elem );
    if ( StringUtils.isNotEmpty( cdeModelPrefix ) ) {
      builder.addAttribute( "cdePalleteType", cdePalleteType );
    }
  }


  private void readCustomProperties( ComponentType.Builder builder, XmlFsPluginThingReaderFactory factory,
                                     Element elem, String sourcePath ) {
    List<Element> propElems = Utils.selectElements( elem, "Contents/Implementation/CustomProperties/*" );
    for ( Element propElem : propElems ) {
      String className = Utils.getNodeText( "Header/Override", propElem );
      // String propName = Utils.getNodeText("Header/Name", propElem);

      if ( StringUtils.isEmpty( className ) ) {
        className = "PropertyType";
      }

      XmlPropertyTypeReader propReader = factory.getPropertyTypeReader();

      PropertyType.Builder prop = propReader.read( propElem, sourcePath );
      builder.addProperty( prop );
    }
  }


  private void readModelProperties( ComponentType.Builder builder, Element elem ) {
    // The "//" in the XPath is to catch properties inside Defintions
    List<Element> usedPropElems = Utils.selectElements( elem, "Contents/Model//Property" );
    for ( Element usedPropElem : usedPropElems ) {
      String definitionName = null;
      Element parentElem = usedPropElem.getParent();
      if ( parentElem != null && parentElem.getName().equals( "Definition" ) ) {
        definitionName = Utils.getNodeText( "@name", parentElem );
      }

      builder.useProperty(
          Utils.getNodeText( "@name", usedPropElem ), // alias
          Utils.getNodeText( ".", usedPropElem ),    // ref-name
          definitionName );
    }
  }


  private String readBaseProperties( ComponentType.Builder builder, Element elem, String sourcePath ) {
    // componentElem is <DesignerComponent>
    String componentName =
        elem.selectSingleNode( "Header/IName" ) != null ? elem.selectSingleNode( "Header/IName" ).getText() : null;
    builder
      .setName( componentName )
      .setLabel( Utils.getNodeText( "Header/Name", elem ) )
      .setTooltip( Utils.getNodeText( "Header/Description", elem ) )
      .setCategory( Utils.getNodeText( "Header/Category", elem ) )
      .setCategoryLabel( Utils.getNodeText( "Header/CatDescription", elem ) )
      .setSourcePath( sourcePath )
      .setVersion( Utils.getNodeText( "Header/Version", elem ) );

    String order = Utils.getNodeText( "Header/Order", elem );
    if ( StringUtils.isNotEmpty( order ) ) {
      try {
        int orderInt = Integer.parseInt( order );
        if ( orderInt < 0 ) {
          orderInt = 0;
        }

        builder.setOrder( orderInt );
      } catch ( NumberFormatException ex ) {
        logger.warn(
          "ComponentType '" + componentName + "' in file '" + sourcePath + "' has an invalid 'Order' value: '" + order + "'.",
          ex );
      }
    }

    String visibleText = Utils.getNodeText( "Header/Visible", elem );
    if ( StringUtils.isNotEmpty( visibleText ) ) {
      builder.setVisible( "true".equalsIgnoreCase( visibleText ) );
    }

    List<Element> legacyNamesElems = Utils.selectElements( elem, "Header/LegacyIName" );
    for ( Element legacyNameElem : legacyNamesElems ) {
      String legacyName = legacyNameElem.getStringValue();
      if ( StringUtils.isNotBlank( legacyName ) ) {
        builder.addLegacyName( legacyName );
      }
    }
    return componentName;
  }


  private void readResourceDependencies( ComponentType.Builder builder, Element compElem, String compDir,
                                         String compName ) {
    List<Element> depElems = Utils.selectElements( compElem, "Contents/Implementation/Dependencies/*" );
    for ( Element depElem : depElems ) {
      Resource.Builder resourceBuilder = createResource( Resource.Type.SCRIPT, compDir, depElem, compName );
      if ( resourceBuilder != null ) {
        builder.addResource( resourceBuilder );
      }
    }

    List<Element> styleElems = Utils.selectElements( compElem, "Contents/Implementation/Styles/*" );
    for ( Element styleElem : styleElems ) {
      Resource.Builder resourceBuilder = createResource( Resource.Type.STYLE, compDir, styleElem, compName );
      if ( resourceBuilder != null ) {
        builder.addResource( resourceBuilder );
      }
    }

    List<Element> rawElems = Utils.selectElements( compElem, "Contents/Implementation/Raw/*" );
    for ( Element rawElem : rawElems ) {
      builder.addResource(
          new Resource.Builder()
            .setType( Resource.Type.STYLE )
            .setApp( Utils.getNodeText( "@app", rawElem ) )
            .setName( Utils.getNodeText( "@name", rawElem ) )
            .setVersion( Utils.getNodeText( "@version", rawElem ) )
            .setSource( Utils.getNodeText( ".", rawElem ) ) );
    }
  }

  private Resource.Builder createResource(
      Resource.Type type,
      String compDir,
      Element resourceElement,
      String componentName ) {
    String dependencySource = Utils.getNodeText( "@src", resourceElement );
    String name = Utils.getNodeText( ".", resourceElement );
    if ( StringUtils.isEmpty( dependencySource ) ) {
      logger.error( String.format( "Dependency with empty src in component '%s'. Skipping.", componentName ) );
      return null;
    }
    if ( StringUtils.isEmpty( name ) ) {
      logger.warn( String.format( "Dependency with empty name in component '%s', src='%s'.", componentName,
          dependencySource ) );
    }
    return
        new Resource.Builder()
          .setType( type )
          .setApp( Utils.getNodeText( "@app", resourceElement ) )
          .setName( Utils.getNodeText( ".", resourceElement ) )
          .setVersion( Utils.getNodeText( "@version", resourceElement ) )
          .setSource( Utils.joinPath( compDir, dependencySource ) );
  }

}
