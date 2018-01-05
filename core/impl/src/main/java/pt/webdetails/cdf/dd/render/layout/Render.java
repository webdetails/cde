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

package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import pt.webdetails.cdf.dd.render.Renderer;
import pt.webdetails.cdf.dd.util.PropertyBag;
import pt.webdetails.cdf.dd.util.XPathUtils;

public abstract class Render {
  private static final String PROPERTY_VALUE = "properties/value[../name=''{0}'']";

  private JXPathContext node;
  protected static final Log logger = LogFactory.getLog( Render.class );
  private PropertyBag propertyBag;

  public Render( JXPathContext node ) {
    this.propertyBag = new PropertyBag();
    this.node = node;
  }

  public JXPathContext getNode() {
    return node;
  }

  public void setNode( JXPathContext node ) {
    this.node = node;
  }

  public String getPropertyBagString() {
    return propertyBag.getPropertiesString();
  }

  protected boolean hasProperty( String property ) {
    return XPathUtils.exists( getNode(), "properties/value[../name='" + property + "']" );
  }

  protected String getPropertyString( String property ) {
    return XPathUtils.getStringValue( getNode(), "properties/value[../name='" + property + "']" );
  }

  protected Boolean getPropertyBoolean( String property ) {
    return XPathUtils.getBooleanValue( getNode(), "properties/value[../name='" + property + "']" );
  }

  public PropertyBag getPropertyBag() {
    return propertyBag;
  }

  public void aliasId( String alias ) {
    String id = propertyBag.getId();
    if ( id != null ) {
      propertyBag.addId( Renderer.aliasName( alias, id ) );
    }
  }

  public abstract void processProperties();

  public abstract String renderStart() throws JSONException;

  public abstract String renderClose();
}
