/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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
