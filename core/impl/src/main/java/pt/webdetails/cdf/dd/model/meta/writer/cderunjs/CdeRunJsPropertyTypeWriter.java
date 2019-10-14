/*!
 * Copyright 2002 - 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd.model.meta.writer.cderunjs;

import static pt.webdetails.cdf.dd.CdeConstants.Writer.*;

import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.meta.LabeledValue;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.model.meta.ComponentType;

import pt.webdetails.cdf.dd.util.JsonUtils;

public class CdeRunJsPropertyTypeWriter extends JsWriterAbstract implements IThingWriter {
  public void write( Object output, IThingWriteContext context, Thing t ) throws ThingWriteException {
    PropertyType prop = (PropertyType) t;
    StringBuilder out = (StringBuilder) output;

    // ------------

    writeValuesListInputTypeRenderer( out, prop );

    // ------------

    // NOTE: property type name cannot have spaces
    // or other special chars, but this is not enforced anywhere.

    String camelName = prop.getCamelName();

    // camelName is the name by which the property will be registered
    // on the client, by means of PropertiesManager.register(...)
    String fullName = camelName;

    ComponentType owner = prop.getOwner();
    if ( owner != null ) {
      String modelId = CdeRunJsHelper.getComponentTypeModelId( owner );
      fullName = modelId + "_" + fullName;
    }

    // PropVar still not garanteed unique...
    String propVarName = prop.getName() + "Property";

    // TODO: Currently, the Property/Base property is not being taken
    // into account in the generated JS...
    out.append( NEWLINE );
    out.append( "var " ).append( propVarName ).append( " = BasePropertyType.extend({" );
    out.append( NEWLINE );

    // This is the name used by PropertiesManager.register(...) to index the property
    addJsProperty( out, "type", JsonUtils.toJsString( fullName ), INDENT1, true );
    addCommaAndLineSep( out );

    out.append( INDENT1 ).append( "stub: {" ).append( NEWLINE );
    // The local property type name is the default name used by property instances
    addJsProperty( out, "name", JsonUtils.toJsString( camelName ), INDENT2, true );
    addJsProperty( out, "value", prop.getDefaultValue(), INDENT2, false );
    addJsProperty( out, "description", JsonUtils.toJsString( prop.getLabel() ), INDENT2, false );
    addJsProperty( out, "tooltip", JsonUtils.toJsString( prop.getTooltip() ), INDENT2, false );

    // Unfortunately, this «type» attribute is the InputType...
    // This attribute is stored along with name and value attributes in the CDFDE JSON file.
    addJsProperty( out, "type", JsonUtils.toJsString( prop.getInputType() ), INDENT2, false );
    addJsProperty( out, "order", String.valueOf( prop.getOrder() ), INDENT2, false );

    // TODO: CDE editor only supports "simple" and "advanced" classTypes.
    String cat = prop.getCategory();
    if ( !PropertyType.CAT_ADVANCED.equals( cat ) ) {
      cat = "";
    }

    addJsProperty( out, "classType", JsonUtils.toJsString( cat ), INDENT2, false );
    out
      .append( NEWLINE )
      .append( INDENT1 )
      .append( "}" )
      .append( NEWLINE )
      .append( "});" )
      .append( NEWLINE );

    out
      .append( "PropertiesManager.register(new " )
      .append( propVarName )
      .append( "());" )
      .append( NEWLINE );
  }

  private void writeValuesListInputTypeRenderer( StringBuilder out, PropertyType prop ) {
    String valuesSource = prop.getPossibleValuesSource();
    if ( valuesSource != null || prop.getPossibleValueCount() > 0 ) {
      String rendererName = prop.getInputType() + "Renderer";

      out
        .append( NEWLINE )
        .append( "var " ).append( rendererName ).append( " = " )
        .append( prop.getAttribute( "BaseRenderer" ).getValue() )
        .append( ".extend({" ).append( NEWLINE )
        .append( INDENT1 ).append( "selectData: " );

      if ( valuesSource != null ) {
        out
          .append( NEWLINE )
          .append( INDENT2 ).append( valuesSource ).append( NEWLINE );
      } else {
        out.append( "{" ).append( NEWLINE );

        boolean isFirst = true;
        for ( LabeledValue labeledValue : prop.getPossibleValues() ) {
          if ( isFirst ) {
            isFirst = false;
          } else {
            addCommaAndLineSep( out );
          }
          out
            .append( INDENT2 )
            .append( JsonUtils.toJsString( labeledValue.getValue() ) )
            .append( ": " )
            .append( JsonUtils.toJsString( labeledValue.getLabel() ) );
        }
        out.append( NEWLINE ).append( INDENT1 ).append( "}" ).append( NEWLINE );
      }

      out.append( "});" ).append( NEWLINE );
    }
  }
}
