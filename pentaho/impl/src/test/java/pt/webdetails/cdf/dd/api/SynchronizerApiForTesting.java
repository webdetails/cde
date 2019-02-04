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
package pt.webdetails.cdf.dd.api;

import java.util.HashMap;
import java.util.Map;

public class SynchronizerApiForTesting extends SyncronizerApi {

  private Map<String, String> messageBundle = new HashMap<String, String>();

  public SynchronizerApiForTesting() {
  }

  public SynchronizerApiForTesting( Map<String, String> messageBundle ) {
    this.messageBundle = messageBundle;
  }

  protected String getMessage( String key ) {
    if ( messageBundle != null && messageBundle.containsKey( key ) ) {
      messageBundle.get( key );
    }
    return key;
  }

  @Override
  protected String listStyles(  ) {
    return null;
  }
}
