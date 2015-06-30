/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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

package pt.webdetails.cdf.dd;

import org.pentaho.platform.api.engine.IParameterProvider;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;


public class MockParameterProvider implements IParameterProvider {

  private HashMap parameters;
  public MockParameterProvider (HashMap parameters) {
    this.parameters = parameters;
  }
  public MockParameterProvider () {
    this.parameters = new HashMap<String, Object>();
  }
  @Override public String getStringParameter( String s, String s2 ) {
    if (parameters.get( s ) != null && parameters.get( s ) instanceof String) {
      return (String) parameters.get( s );
    } else {
      return s2;
    }
  }

  @Override public long getLongParameter( String s, long l ) {
    return 0;
  }

  @Override public Date getDateParameter( String s, Date date ) {
    return null;
  }

  @Override public BigDecimal getDecimalParameter( String s, BigDecimal bigDecimal ) {
    return null;
  }

  @Override public Object[] getArrayParameter( String s, Object[] objects ) {
    return new Object[0];
  }

  @Override public String[] getStringArrayParameter( String s, String[] strings ) {
    return new String[0];
  }

  @Override public Iterator getParameterNames() {
    return null;
  }

  @Override public Object getParameter( String s ) {
    return parameters.get( s );
  }

  @Override public boolean hasParameter( String s ) {
    return false;
  }

  public void setParameter(String key, Object value) {
    parameters.put( key, value );
  }

}
