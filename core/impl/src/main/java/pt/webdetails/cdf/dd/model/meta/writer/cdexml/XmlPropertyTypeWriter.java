/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cdf.dd.model.meta.writer.cdexml;

import org.dom4j.Branch;
import org.dom4j.Element;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;

public class XmlPropertyTypeWriter implements IThingWriter {
  public void write( java.lang.Object output, IThingWriteContext context, Thing t ) throws ThingWriteException {
    PropertyType prop = (PropertyType) t;
    Branch parent = (Branch) output; // Element or Document

    boolean isAdvanced = PropertyType.CAT_ADVANCED.equals( prop.getCategory() );

    Element propElem = parent.addElement( "DesignerProperty" );
    Element headerElem = propElem.addElement( "Header" );

    String defValue = prop.getDefaultValue();
    if ( "\"\"".equals( defValue ) ) {
      defValue = "";
    }

    headerElem.addElement( "Name" ).setText( prop.getName() );
    headerElem.addElement( "Parent" ).setText( prop.getBase() );
    headerElem.addElement( "DefaultValue" ).setText( defValue );
    headerElem.addElement( "Description" ).setText( prop.getLabel() );
    headerElem.addElement( "Tooltip" ).setText( prop.getTooltip() );
    headerElem.addElement( "Advanced" ).setText( isAdvanced ? "true" : "false" );
    headerElem.addElement( "InputType" ).setText( prop.getInputType() );
    headerElem.addElement( "OutputType" ).setText( prop.getValueType().toString() );
    headerElem.addElement( "Order" ).setText( String.valueOf( prop.getOrder() ) );
    headerElem.addElement( "Version" ).setText( prop.getVersion() );
    headerElem.addElement( "Visible" ).setText( prop.getVisible() ? "true" : "false" );
  }
}
