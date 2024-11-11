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
